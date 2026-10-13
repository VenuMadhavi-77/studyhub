package com.studyhub.platform.modules.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyhub.platform.common.enums.AiFeature;
import com.studyhub.platform.common.enums.AiStatus;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.ai.client.AiProviderClient;
import com.studyhub.platform.modules.ai.client.GeminiProviderClient;
import com.studyhub.platform.modules.ai.dto.*;
import com.studyhub.platform.modules.ai.entity.AIInteraction;
import com.studyhub.platform.modules.ai.repository.AIInteractionRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceFilterCriteria;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.service.SearchService;
import com.studyhub.platform.modules.storage.StorageService;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Optional;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final AiProviderClient aiProviderClient;
    private final DocumentExtractorService documentExtractorService;
    private final AiRateLimiterService rateLimiterService;
    private final AIInteractionRepository aiInteractionRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final StorageService storageService;
    private final SearchService searchService;
    private final ObjectMapper objectMapper;

    public AIService(GeminiProviderClient geminiProviderClient,
                     DocumentExtractorService documentExtractorService,
                     AiRateLimiterService rateLimiterService,
                     AIInteractionRepository aiInteractionRepository,
                     ResourceRepository resourceRepository,
                     UserRepository userRepository,
                     SubjectRepository subjectRepository,
                     StorageService storageService,
                     SearchService searchService) {
        this.aiProviderClient = geminiProviderClient;
        this.documentExtractorService = documentExtractorService;
        this.rateLimiterService = rateLimiterService;
        this.aiInteractionRepository = aiInteractionRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.storageService = storageService;
        this.searchService = searchService;
        this.objectMapper = new ObjectMapper();
    }

    @Transactional
    public AiSummaryResponse summarizeResource(Long resourceId, Long userId) {
        rateLimiterService.checkAndConsumeQuota(userId);
        long startTime = System.currentTimeMillis();

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        String textToSummarize = resource.getDescription();
        if (resource.getFilePath() != null && resource.getFilePath().endsWith(".pdf")) {
            try {
                org.springframework.core.io.Resource fileRes = storageService.loadAsResource(resource.getFilePath());
                // Simple summary from available text or title
                textToSummarize = resource.getTitle() + "\n" + (resource.getDescription() != null ? resource.getDescription() : "");
            } catch (Exception e) {
                log.warn("Could not read file for AI summarization: {}", e.getMessage());
            }
        }

        try {
            AiSummaryResponse summary = aiProviderClient.summarize(textToSummarize, resource.getTitle());

            // Save summary into resource entity
            resource.setAiSummary(summary.shortSummary());
            resource.setAiKeyPoints(objectMapper.writeValueAsString(summary.keyPoints()));
            resource.setAiRevisionNotes(objectMapper.writeValueAsString(summary.examRevisionNotes()));
            resourceRepository.save(resource);

            logAiInteraction(userId, AiFeature.SUMMARIZE, (int) (System.currentTimeMillis() - startTime), AiStatus.SUCCESS, null);
            return summary;
        } catch (Exception e) {
            logAiInteraction(userId, AiFeature.SUMMARIZE, (int) (System.currentTimeMillis() - startTime), AiStatus.FAILED, e.getMessage());
            throw new RuntimeException("AI Summarization failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AiCategorizationResponse categorizeDocument(MultipartFile file, String fallbackTitle, Long userId) {
        rateLimiterService.checkAndConsumeQuota(userId);
        long startTime = System.currentTimeMillis();

        String extractedText = "";
        if (file != null && !file.isEmpty() && file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".pdf")) {
            extractedText = documentExtractorService.extractText(file);
        }

        if (extractedText.isBlank()) {
            extractedText = fallbackTitle != null ? fallbackTitle : "Academic Document";
        }

        try {
            AiCategorizationResponse response = aiProviderClient.categorize(extractedText, fallbackTitle != null ? fallbackTitle : "Study Material");
            logAiInteraction(userId, AiFeature.CATEGORIZE, (int) (System.currentTimeMillis() - startTime), AiStatus.SUCCESS, null);
            return response;
        } catch (Exception e) {
            logAiInteraction(userId, AiFeature.CATEGORIZE, (int) (System.currentTimeMillis() - startTime), AiStatus.FAILED, e.getMessage());
            throw new RuntimeException("AI Categorization failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    public AiDoubtAnswerResponse answerDoubt(AiDoubtAskRequest request, Long userId) {
        rateLimiterService.checkAndConsumeQuota(userId);
        long startTime = System.currentTimeMillis();

        String question = request != null ? request.getEffectiveQuestion() : "";
        if (question.isBlank()) {
            throw new com.studyhub.platform.common.exception.BadRequestException("Question cannot be blank");
        }
        String subject = request.getEffectiveSubject();

        try {
            String answer = aiProviderClient.answerDoubt(question, subject);
            logAiInteraction(userId, AiFeature.DOUBT_ASSIST, (int) (System.currentTimeMillis() - startTime), AiStatus.SUCCESS, null);
            
            // Build structured breakdown for rich frontend cards (directAnswer, stepByStepExplanation, keyTakeaways)
            String directAnswer = answer;
            String stepByStep = "1. Break the problem into inputs and core definitions.\n2. Analyze logic or invariant step-by-step.\n3. Validate outputs with corner cases.";
            String keyTakeaways = "Master the core invariants in " + subject + " for optimal problem solving.";
            String practiceQuestion = "Apply this concept to a sample problem and analyze its complexity.";

            return new AiDoubtAnswerResponse(
                    answer,
                    directAnswer,
                    stepByStep,
                    keyTakeaways,
                    practiceQuestion,
                    true,
                    aiProviderClient.getProviderName()
            );
        } catch (com.studyhub.platform.common.exception.BadRequestException e) {
            throw e;
        } catch (Exception e) {
            logAiInteraction(userId, AiFeature.DOUBT_ASSIST, (int) (System.currentTimeMillis() - startTime), AiStatus.FAILED, e.getMessage());
            throw new RuntimeException("AI Doubt Assistant error: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> naturalLanguageSearch(String prompt, Pageable pageable, Long userId) {
        long startTime = System.currentTimeMillis();

        try {
            AiSearchCriteriaResult criteriaResult = aiProviderClient.parseNaturalSearch(prompt);

            Long subjectId = null;
            if (criteriaResult.subjectName() != null) {
                subjectId = subjectRepository.findByCode(criteriaResult.subjectName())
                        .map(Subject::getId)
                        .orElse(null);
            }

            String joinedKeywords = criteriaResult.keywords() != null && !criteriaResult.keywords().isEmpty()
                    ? String.join(" ", criteriaResult.keywords())
                    : prompt;

            ResourceFilterCriteria filterCriteria = new ResourceFilterCriteria(
                    joinedKeywords,
                    null,
                    subjectId,
                    null,
                    criteriaResult.semester(),
                    criteriaResult.resourceType(),
                    null,
                    null,
                    null
            );

            logAiInteraction(userId, AiFeature.NATURAL_SEARCH, (int) (System.currentTimeMillis() - startTime), AiStatus.SUCCESS, null);
            return searchService.searchResources(filterCriteria, pageable, userId);
        } catch (Exception e) {
            logAiInteraction(userId, AiFeature.NATURAL_SEARCH, (int) (System.currentTimeMillis() - startTime), AiStatus.FAILED, e.getMessage());
            // Graceful fallback to standard keyword search
            return searchService.searchResources(new ResourceFilterCriteria(prompt, null, null, null, null, null, null, null, null), pageable, userId);
        }
    }

    private void logAiInteraction(Long userId, AiFeature feature, int latencyMs, AiStatus status, String errorMessage) {
        try {
            User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
            AIInteraction interaction = new AIInteraction();
            interaction.setUser(user);
            interaction.setFeature(feature);
            interaction.setProvider(aiProviderClient.getProviderName());
            interaction.setLatencyMs(latencyMs);
            interaction.setStatus(status);
            interaction.setErrorMessage(errorMessage);
            aiInteractionRepository.save(interaction);
        } catch (Exception e) {
            log.warn("Failed to log AI interaction: {}", e.getMessage());
        }
    }
}

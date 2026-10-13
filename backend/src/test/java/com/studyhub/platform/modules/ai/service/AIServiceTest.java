package com.studyhub.platform.modules.ai.service;

import com.studyhub.platform.common.enums.AiFeature;
import com.studyhub.platform.common.enums.AiStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.modules.ai.client.GeminiProviderClient;
import com.studyhub.platform.modules.ai.dto.AiCategorizationResponse;
import com.studyhub.platform.modules.ai.dto.AiDoubtAnswerResponse;
import com.studyhub.platform.modules.ai.dto.AiDoubtAskRequest;
import com.studyhub.platform.modules.ai.dto.AiSummaryResponse;
import com.studyhub.platform.modules.ai.repository.AIInteractionRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.service.SearchService;
import com.studyhub.platform.modules.storage.StorageService;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AIServiceTest {

    @Mock
    private GeminiProviderClient geminiProviderClient;

    @Mock
    private DocumentExtractorService documentExtractorService;

    @Mock
    private AiRateLimiterService rateLimiterService;

    @Mock
    private AIInteractionRepository aiInteractionRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private AIService aiService;

    private User student;
    private Resource resource;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(5L);
        student.setEmail("student@studyhub.edu");

        resource = new Resource();
        resource.setId(20L);
        resource.setTitle("Compiler Design Syntax Tree");
        resource.setDescription("Parsing algorithms and bottom up parser.");
    }

    @Test
    void summarizeResource_ShouldUpdateEntityAndLogInteraction() {
        when(resourceRepository.findById(20L)).thenReturn(Optional.of(resource));
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));

        AiSummaryResponse mockSummary = new AiSummaryResponse(
                20L,
                "Short summary of compiler bottom-up parsing",
                List.of("LR(0) parsing table", "Shift-reduce conflicts"),
                List.of("Draw parse tree for sample grammar"),
                Set.of("compiler", "parsing")
        );
        when(geminiProviderClient.summarize(any(), eq("Compiler Design Syntax Tree"))).thenReturn(mockSummary);
        when(geminiProviderClient.getProviderName()).thenReturn("GEMINI");

        AiSummaryResponse result = aiService.summarizeResource(20L, 5L);

        assertNotNull(result);
        assertEquals("Short summary of compiler bottom-up parsing", result.shortSummary());
        assertEquals("Short summary of compiler bottom-up parsing", resource.getAiSummary());
        verify(resourceRepository, times(1)).save(resource);
        verify(aiInteractionRepository, times(1)).save(any());
    }

    @Test
    void answerDoubt_ShouldReturnAiGeneratedAnswer() {
        AiDoubtAskRequest request = new AiDoubtAskRequest("What is Dijkstra's algorithm?", "Algorithms");
        when(userRepository.findById(5L)).thenReturn(Optional.of(student));
        when(geminiProviderClient.answerDoubt(any(), eq("Algorithms"))).thenReturn("Dijkstra's algorithm finds the shortest path in non-negative graphs.");
        when(geminiProviderClient.getProviderName()).thenReturn("GEMINI");

        AiDoubtAnswerResponse response = aiService.answerDoubt(request, 5L);

        assertNotNull(response);
        assertTrue(response.isAiGenerated());
        assertEquals("GEMINI", response.provider());
        assertTrue(response.answer().contains("Dijkstra's algorithm"));
        verify(aiInteractionRepository, times(1)).save(any());
    }
}

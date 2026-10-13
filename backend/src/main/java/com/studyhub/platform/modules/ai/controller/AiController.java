package com.studyhub.platform.modules.ai.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.common.dto.PagedResponse;
import com.studyhub.platform.modules.ai.dto.*;
import com.studyhub.platform.modules.ai.service.AIService;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai")
@Tag(name = "AI Subsystem", description = "AI Summarization, Categorization, Doubt Assistant, and Natural Search APIs")
public class AiController {

    private final AIService aiService;

    public AiController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/summarize/{resourceId}")
    @Operation(summary = "Generate AI summary, key concepts, and exam revision notes for a resource")
    public ResponseEntity<ApiResponse<AiSummaryResponse>> summarize(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        AiSummaryResponse summary = aiService.summarizeResource(resourceId, userId);
        return ResponseEntity.ok(ApiResponse.ok(summary, "AI Summary generated successfully"));
    }

    @PostMapping(value = "/categorize-extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Extract text from document and suggest taxonomy categorization, tags, and title")
    public ResponseEntity<ApiResponse<AiCategorizationResponse>> categorize(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "fallbackTitle", required = false) String fallbackTitle,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        AiCategorizationResponse response = aiService.categorizeDocument(file, fallbackTitle, userId);
        return ResponseEntity.ok(ApiResponse.ok(response, "Document categorized successfully"));
    }

    @PostMapping("/doubt-assistant")
    @Operation(summary = "Ask StudyHub AI doubt assistant for immediate step-by-step academic explanations")
    public ResponseEntity<ApiResponse<AiDoubtAnswerResponse>> askDoubtAssistant(
            @Valid @RequestBody AiDoubtAskRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        AiDoubtAnswerResponse response = aiService.answerDoubt(request, userId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/natural-search")
    @Operation(summary = "Query academic resources using conversational natural language")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceResponse>>> naturalLanguageSearch(
            @Valid @RequestBody AiSearchQueryRequest request,
            @PageableDefault(size = 12) Pageable pageable,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Long userId = principal != null ? principal.getId() : null;
        Page<ResourceResponse> results = aiService.naturalLanguageSearch(request.prompt(), pageable, userId);
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(results)));
    }
}

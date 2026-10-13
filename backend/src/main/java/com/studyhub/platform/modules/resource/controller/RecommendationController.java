package com.studyhub.platform.modules.resource.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.resource.dto.ResourceRecommendationResponse;
import com.studyhub.platform.modules.resource.service.RecommendationService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/recommendations", "/api/recommendations"})
@Tag(name = "Personalized Recommendations", description = "AI and engagement-based resource recommendation endpoints")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get personalized resource recommendations for the authenticated user")
    public ResponseEntity<ApiResponse<List<ResourceRecommendationResponse>>> getRecommendations(
            @AuthenticationPrincipal SecurityUserPrincipal principal
    ) {
        List<ResourceRecommendationResponse> results = recommendationService.getRecommendationsForUser(principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(results));
    }
}

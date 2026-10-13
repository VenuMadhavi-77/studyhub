package com.studyhub.platform.modules.resource.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.resource.dto.ReviewCreateRequest;
import com.studyhub.platform.modules.resource.dto.ReviewResponse;
import com.studyhub.platform.modules.resource.service.ReviewService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/resources/{resourceId}/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ReviewResponse>> createReview(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal SecurityUserPrincipal currentUser,
            @Valid @RequestBody ReviewCreateRequest request) {

        ReviewResponse response = reviewService.createReview(currentUser.getId(), resourceId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Review submitted successfully"));
    }

    @GetMapping("/resources/{resourceId}/reviews")
    public ResponseEntity<ApiResponse<List<ReviewResponse>>> getReviews(
            @PathVariable Long resourceId,
            @AuthenticationPrincipal SecurityUserPrincipal currentUser) {

        Long currentUserId = currentUser != null ? currentUser.getId() : null;
        List<ReviewResponse> response = reviewService.getReviewsForResource(resourceId, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/reviews/{reviewId}/helpful")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Map<String, Object>>> toggleHelpful(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal SecurityUserPrincipal currentUser) {

        boolean added = reviewService.toggleHelpfulVote(currentUser.getId(), reviewId);
        return ResponseEntity.ok(ApiResponse.ok(
                Map.of("voted", added),
                added ? "Helpful vote recorded" : "Helpful vote removed"
        ));
    }
}

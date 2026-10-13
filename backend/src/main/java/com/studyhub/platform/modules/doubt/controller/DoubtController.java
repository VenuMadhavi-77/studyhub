package com.studyhub.platform.modules.doubt.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.common.dto.PagedResponse;
import com.studyhub.platform.common.enums.DoubtStatus;
import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.modules.doubt.dto.*;
import com.studyhub.platform.modules.doubt.service.AnswerService;
import com.studyhub.platform.modules.doubt.service.DoubtService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Doubts", description = "Student Doubt & Discussion Forum APIs")
public class DoubtController {

    private final DoubtService doubtService;
    private final AnswerService answerService;

    public DoubtController(DoubtService doubtService, AnswerService answerService) {
        this.doubtService = doubtService;
        this.answerService = answerService;
    }

    @PostMapping("/doubts")
    @Operation(summary = "Ask a new academic doubt question")
    public ResponseEntity<ApiResponse<DoubtResponse>> createDoubt(
            @Valid @RequestBody DoubtCreateRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        DoubtResponse response = doubtService.createDoubt(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Doubt created successfully"));
    }

    @GetMapping("/doubts")
    @Operation(summary = "Browse doubts with filtering and pagination")
    public ResponseEntity<ApiResponse<PagedResponse<DoubtResponse>>> getDoubts(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) DoubtStatus status,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(doubtService.getDoubts(subjectId, status, pageable))));
    }

    @GetMapping("/doubts/{id}")
    @Operation(summary = "Get detailed doubt view with answers and user vote state")
    public ResponseEntity<ApiResponse<DoubtDetailResponse>> getDoubtDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        DoubtDetailResponse response = doubtService.getDoubtDetail(id, currentUserId);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/doubts/{id}/answers")
    @Operation(summary = "Post an answer to a doubt question")
    public ResponseEntity<ApiResponse<AnswerResponse>> postAnswer(
            @PathVariable Long id,
            @Valid @RequestBody AnswerCreateRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        AnswerResponse response = answerService.postAnswer(id, request, principal.getId(), false);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Answer submitted successfully"));
    }

    @PostMapping("/doubts/{id}/accept/{answerId}")
    @Operation(summary = "Mark an answer as accepted (Question Author only)")
    public ResponseEntity<ApiResponse<DoubtResponse>> acceptAnswer(
            @PathVariable Long id,
            @PathVariable Long answerId,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        DoubtResponse response = doubtService.acceptAnswer(id, answerId, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Answer accepted as solution"));
    }

    @PostMapping("/answers/{id}/vote")
    @Operation(summary = "Toggle upvote on an answer")
    public ResponseEntity<ApiResponse<AnswerVoteResponse>> toggleAnswerVote(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        AnswerVoteResponse response = answerService.toggleUpvote(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, response.upvoted() ? "Upvoted answer" : "Removed upvote"));
    }

    @DeleteMapping("/doubts/{id}")
    @Operation(summary = "Delete doubt (Owner or Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteDoubt(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        doubtService.deleteDoubt(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Doubt deleted successfully"));
    }

    @DeleteMapping("/answers/{id}")
    @Operation(summary = "Delete answer (Owner or Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteAnswer(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        answerService.deleteAnswer(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Answer deleted successfully"));
    }
}

package com.studyhub.platform.modules.resource.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.common.dto.PagedResponse;
import com.studyhub.platform.modules.resource.dto.BookmarkResponse;
import com.studyhub.platform.modules.resource.dto.RatingRequest;
import com.studyhub.platform.modules.resource.dto.RatingResponse;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.dto.UsefulVoteResponse;
import com.studyhub.platform.modules.resource.service.BookmarkService;
import com.studyhub.platform.modules.resource.service.RatingService;
import com.studyhub.platform.modules.resource.service.UsefulVoteService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Ratings & Bookmarks", description = "Community engagement and interaction APIs")
public class ResourceInteractionController {

    private final RatingService ratingService;
    private final UsefulVoteService usefulVoteService;
    private final BookmarkService bookmarkService;

    public ResourceInteractionController(RatingService ratingService,
                                         UsefulVoteService usefulVoteService,
                                         BookmarkService bookmarkService) {
        this.ratingService = ratingService;
        this.usefulVoteService = usefulVoteService;
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/resources/{id}/rate")
    @Operation(summary = "Submit or update 1-5 star rating on a resource")
    public ResponseEntity<ApiResponse<RatingResponse>> rateResource(
            @PathVariable Long id,
            @Valid @RequestBody RatingRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        RatingResponse response = ratingService.rateResource(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Rating recorded successfully"));
    }

    @PostMapping("/resources/{id}/useful")
    @Operation(summary = "Toggle 'Mark as Useful' vote on a resource")
    public ResponseEntity<ApiResponse<UsefulVoteResponse>> toggleUseful(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        UsefulVoteResponse response = usefulVoteService.toggleUsefulVote(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, response.votedUseful() ? "Marked as useful" : "Removed useful vote"));
    }

    @PostMapping("/resources/{id}/bookmark")
    @Operation(summary = "Toggle bookmark for saved resources")
    public ResponseEntity<ApiResponse<BookmarkResponse>> toggleBookmark(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        BookmarkResponse response = bookmarkService.toggleBookmark(id, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, response.bookmarked() ? "Resource bookmarked" : "Bookmark removed"));
    }

    @GetMapping("/bookmarks")
    @Operation(summary = "Get list of current user's bookmarked resources")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceResponse>>> getBookmarks(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(bookmarkService.getUserBookmarks(principal.getId(), pageable))));
    }
}

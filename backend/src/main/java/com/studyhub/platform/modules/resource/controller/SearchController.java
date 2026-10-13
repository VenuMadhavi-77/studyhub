package com.studyhub.platform.modules.resource.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.common.dto.PagedResponse;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.modules.resource.dto.ResourceFilterCriteria;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.service.SearchService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@Tag(name = "Search", description = "Multi-Criteria Academic Resource Search APIs")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/resources")
    @Operation(summary = "Search and filter academic resources with pagination")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceResponse>>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) Long topicId,
            @RequestParam(required = false) Integer semester,
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Double minRating,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {

        ResourceFilterCriteria criteria = new ResourceFilterCriteria(
                query, departmentId, subjectId, topicId, semester, type, tag, minRating, null
        );

        Long currentUserId = principal != null ? principal.getId() : null;
        Page<ResourceResponse> results = searchService.searchResources(criteria, pageable, currentUserId);

        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(results)));
    }
}

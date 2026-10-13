package com.studyhub.platform.modules.admin.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.common.dto.PagedResponse;
import com.studyhub.platform.common.enums.ReportStatus;
import com.studyhub.platform.modules.admin.dto.*;
import com.studyhub.platform.modules.admin.service.AdminService;
import com.studyhub.platform.modules.auth.dto.UserProfileResponse;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.taxonomy.dto.SubjectDto;
import com.studyhub.platform.modules.taxonomy.dto.TopicDto;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Platform Administration, Moderation, and Statistics APIs")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get overall platform metrics and activity counters")
    public ResponseEntity<ApiResponse<AdminStatsResponse>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getPlatformStats()));
    }

    @GetMapping("/resources/pending")
    @Operation(summary = "Get list of pending resources awaiting moderation approval")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceResponse>>> getPendingResources(
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(adminService.getPendingResources(pageable))));
    }

    @PatchMapping("/resources/{id}/moderation")
    @Operation(summary = "Approve or reject an uploaded resource with reason")
    public ResponseEntity<ApiResponse<ResourceResponse>> moderateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceModerationRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        ResourceResponse response = adminService.moderateResource(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Resource status updated"));
    }

    @GetMapping("/reports")
    @Operation(summary = "Get list of community moderation reports")
    public ResponseEntity<ApiResponse<PagedResponse<ReportResponse>>> getReports(
            @RequestParam(required = false) ReportStatus status,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(adminService.getReports(status, pageable))));
    }

    @PatchMapping("/reports/{id}/resolve")
    @Operation(summary = "Resolve or dismiss an existing moderation report")
    public ResponseEntity<ApiResponse<ReportResponse>> resolveReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportResolutionRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        ReportResponse response = adminService.resolveReport(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "Report resolved successfully"));
    }

    @GetMapping("/users")
    @Operation(summary = "List all users with optional search query")
    public ResponseEntity<ApiResponse<PagedResponse<UserProfileResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(adminService.getUsers(search, pageable))));
    }

    @PatchMapping("/users/{id}/status")
    @Operation(summary = "Suspend or reinstate a student user account")
    public ResponseEntity<ApiResponse<Void>> updateUserStatus(
            @PathVariable Long id,
            @Valid @RequestBody UserStatusUpdateRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        adminService.updateUserStatus(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok("User status updated successfully"));
    }


    @PostMapping("/resources/external")
    @Operation(summary = "Add an external curated resource to catalog")
    public ResponseEntity<ApiResponse<ResourceResponse>> createExternalResource(
            @Valid @RequestBody com.studyhub.platform.modules.resource.dto.ExternalResourceRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        ResourceResponse response = adminService.createExternalResource(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response, "External resource cataloged successfully"));
    }

    @PutMapping("/resources/{id}/external")
    @Operation(summary = "Update an external curated resource in catalog")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateExternalResource(
            @PathVariable Long id,
            @Valid @RequestBody com.studyhub.platform.modules.resource.dto.ExternalResourceRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        ResourceResponse response = adminService.updateExternalResource(id, request, principal.getId());
        return ResponseEntity.ok(ApiResponse.ok(response, "External resource updated successfully"));
    }
}

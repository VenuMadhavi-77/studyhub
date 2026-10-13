package com.studyhub.platform.modules.resource.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.common.dto.PagedResponse;
import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.modules.resource.dto.ResourceDetailResponse;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.dto.ResourceUpdateRequest;
import com.studyhub.platform.modules.resource.dto.ResourceUploadRequest;
import com.studyhub.platform.modules.resource.service.ResourceService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resources")
@Tag(name = "Resources", description = "Academic Resource CRUD and File Management APIs")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a new academic study resource with optional file attachment")
    public ResponseEntity<ApiResponse<ResourceResponse>> uploadResource(
            @Valid @RequestPart("metadata") ResourceUploadRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        ResourceResponse response = resourceService.uploadResource(request, file, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Resource uploaded successfully"));
    }

    @GetMapping("/{id:[0-9]+}")
    @Operation(summary = "Get detailed resource view with metrics and user state")
    public ResponseEntity<ApiResponse<ResourceDetailResponse>> getResource(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            HttpServletRequest servletRequest) {
        Long currentUserId = principal != null ? principal.getId() : null;
        String clientIp = servletRequest.getRemoteAddr();
        ResourceDetailResponse response = resourceService.getResourceDetail(id, currentUserId, clientIp);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PutMapping("/{id:[0-9]+}")
    @Operation(summary = "Update an existing resource (Owner or Admin)")
    public ResponseEntity<ApiResponse<ResourceResponse>> updateResource(
            @PathVariable Long id,
            @Valid @RequestBody ResourceUpdateRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        ResourceResponse response = resourceService.updateResource(id, request, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok(response, "Resource updated successfully"));
    }

    @DeleteMapping("/{id:[0-9]+}")
    @Operation(summary = "Delete resource (Owner or Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteResource(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        boolean isAdmin = principal.getRole() == Role.ADMIN;
        resourceService.deleteResource(id, principal.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.ok("Resource deleted successfully"));
    }

    @GetMapping("/{id:[0-9]+}/download")
    @Operation(summary = "Download resource attachment file")
    public ResponseEntity<Resource> downloadResource(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        Long currentUserId = principal != null ? principal.getId() : null;
        ResourceService.ResourceDownloadResult downloadResult = resourceService.getResourceForDownload(id, currentUserId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(downloadResult.mimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadResult.resourceTitle() + "\"")
                .body(downloadResult.resource());
    }

    @GetMapping("/me")
    @Operation(summary = "Get list of resources uploaded by the authenticated user")
    public ResponseEntity<ApiResponse<PagedResponse<ResourceResponse>>> getMyResources(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(PagedResponse.from(resourceService.getMyResources(principal.getId(), pageable))));
    }

    @PostMapping("/{id:[0-9]+}/view")
    @Operation(summary = "Record a view event for a resource (e.g. click-through or preview)")
    public ResponseEntity<ApiResponse<Void>> recordView(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            HttpServletRequest servletRequest) {
        Long currentUserId = principal != null ? principal.getId() : null;
        String clientIp = servletRequest.getRemoteAddr();
        resourceService.recordExternalView(id, currentUserId, clientIp);
        return ResponseEntity.ok(ApiResponse.ok("View recorded"));
    }
}

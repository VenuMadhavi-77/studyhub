package com.studyhub.platform.modules.admin.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.admin.dto.ReportCreateRequest;
import com.studyhub.platform.modules.admin.dto.ReportResponse;
import com.studyhub.platform.modules.admin.service.ReportService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports", description = "Community Moderation and Content Reporting APIs")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    @Operation(summary = "Submit a report against a resource, doubt, answer, or user")
    public ResponseEntity<ApiResponse<ReportResponse>> createReport(
            @Valid @RequestBody ReportCreateRequest request,
            @AuthenticationPrincipal SecurityUserPrincipal principal) {
        ReportResponse response = reportService.submitReport(request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Report submitted successfully for admin review"));
    }
}

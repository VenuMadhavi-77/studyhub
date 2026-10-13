package com.studyhub.platform.modules.admin.dto;

import com.studyhub.platform.common.enums.ReportStatus;
import com.studyhub.platform.common.enums.ReportTargetType;
import com.studyhub.platform.modules.admin.entity.Report;

import java.time.Instant;

public record ReportResponse(
        Long id,
        Long reporterId,
        String reporterName,
        String reporterEmail,
        ReportTargetType targetType,
        Long targetId,
        String reason,
        String details,
        ReportStatus status,
        Long resolvedByAdminId,
        String resolvedByAdminName,
        Instant createdAt,
        Instant resolvedAt
) {
    public static ReportResponse fromEntity(Report report) {
        return new ReportResponse(
                report.getId(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                report.getReporter() != null ? report.getReporter().getFullName() : null,
                report.getReporter() != null ? report.getReporter().getEmail() : null,
                report.getTargetType(),
                report.getTargetId(),
                report.getReason(),
                report.getDetails(),
                report.getStatus(),
                report.getResolvedByAdmin() != null ? report.getResolvedByAdmin().getId() : null,
                report.getResolvedByAdmin() != null ? report.getResolvedByAdmin().getFullName() : null,
                report.getCreatedAt(),
                report.getResolvedAt()
        );
    }
}

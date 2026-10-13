package com.studyhub.platform.modules.admin.dto;

import com.studyhub.platform.common.enums.ReportStatus;
import com.studyhub.platform.common.enums.ReportTargetType;
import com.studyhub.platform.modules.admin.entity.Report;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record ReportCreateRequest(
        @NotNull(message = "Target type is required")
        ReportTargetType targetType,

        @NotNull(message = "Target ID is required")
        Long targetId,

        @NotBlank(message = "Reason is required")
        @Size(max = 100, message = "Reason cannot exceed 100 characters")
        String reason,

        @Size(max = 1000, message = "Details cannot exceed 1000 characters")
        String details
) {}

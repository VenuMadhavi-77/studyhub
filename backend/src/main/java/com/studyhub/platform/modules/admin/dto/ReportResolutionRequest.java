package com.studyhub.platform.modules.admin.dto;

import com.studyhub.platform.common.enums.ReportStatus;
import jakarta.validation.constraints.NotNull;

public record ReportResolutionRequest(
        @NotNull(message = "Resolution status is required")
        ReportStatus status,

        String resolutionNotes
) {}

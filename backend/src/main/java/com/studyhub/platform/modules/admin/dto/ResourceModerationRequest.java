package com.studyhub.platform.modules.admin.dto;

import com.studyhub.platform.common.enums.ResourceStatus;
import jakarta.validation.constraints.NotNull;

public record ResourceModerationRequest(
        @NotNull(message = "Moderation status is required")
        ResourceStatus status,

        String rejectionReason
) {}

package com.studyhub.platform.modules.admin.dto;

import com.studyhub.platform.common.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "User status is required")
        UserStatus status,

        String reason
) {}

package com.studyhub.platform.modules.auth.dto;

import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "Full name cannot be blank")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
        String fullName,
        Long departmentId,
        Integer semester,
        String goals,
        ContentLanguage preferredLanguage,
        ContentLevel level
) {}


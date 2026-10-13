package com.studyhub.platform.modules.auth.dto;

import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;

import java.time.Instant;

public record UserProfileResponse(
        Long id,
        String email,
        String fullName,
        Role role,
        UserStatus status,
        Long departmentId,
        String departmentName,
        Integer semester,
        String goals,
        ContentLanguage preferredLanguage,
        ContentLevel level,
        Instant createdAt
) {}


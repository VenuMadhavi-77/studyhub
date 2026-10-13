package com.studyhub.platform.modules.admin.dto;

import com.studyhub.platform.common.enums.SourcePlatform;
import jakarta.validation.constraints.NotNull;

public record TaxonomyCreatePlatformRequest(
        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotNull(message = "Platform is required")
        SourcePlatform platform,

        Integer priority
) {}

package com.studyhub.platform.modules.taxonomy.dto;

import com.studyhub.platform.common.enums.SourcePlatform;

public record DepartmentPlatformDto(
        Long id,
        Long departmentId,
        SourcePlatform platform,
        Integer priority
) {}

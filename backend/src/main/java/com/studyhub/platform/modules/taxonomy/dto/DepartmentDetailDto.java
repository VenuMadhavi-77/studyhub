package com.studyhub.platform.modules.taxonomy.dto;

import java.util.List;

public record DepartmentDetailDto(
        Long id,
        String name,
        String code,
        boolean active,
        List<SubjectDto> subjects,
        List<GoalDto> goals,
        List<DepartmentPlatformDto> platforms,
        List<DepartmentToolDto> tools
) {}

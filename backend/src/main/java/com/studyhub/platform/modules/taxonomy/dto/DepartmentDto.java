package com.studyhub.platform.modules.taxonomy.dto;

public record DepartmentDto(
        Long id,
        String name,
        String code,
        boolean active
) {}

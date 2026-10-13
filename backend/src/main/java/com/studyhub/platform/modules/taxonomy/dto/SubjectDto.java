package com.studyhub.platform.modules.taxonomy.dto;

public record SubjectDto(
        Long id,
        Long departmentId,
        String departmentName,
        String name,
        String code,
        Integer semester,
        boolean active
) {}

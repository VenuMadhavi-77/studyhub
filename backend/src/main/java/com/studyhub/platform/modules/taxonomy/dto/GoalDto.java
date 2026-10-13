package com.studyhub.platform.modules.taxonomy.dto;

public record GoalDto(
        Long id,
        Long departmentId,
        String goalKey,
        String label,
        String description,
        boolean active
) {}

package com.studyhub.platform.modules.taxonomy.dto;

public record TopicDto(
        Long id,
        Long subjectId,
        String subjectName,
        String name,
        boolean active
) {}

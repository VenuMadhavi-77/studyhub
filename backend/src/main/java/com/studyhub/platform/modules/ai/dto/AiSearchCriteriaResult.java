package com.studyhub.platform.modules.ai.dto;

import com.studyhub.platform.common.enums.ResourceType;

import java.util.List;

public record AiSearchCriteriaResult(
        String subjectName,
        String topicName,
        Integer semester,
        ResourceType resourceType,
        List<String> keywords,
        String difficulty
) {}

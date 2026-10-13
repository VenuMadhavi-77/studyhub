package com.studyhub.platform.modules.ai.dto;

import com.studyhub.platform.common.enums.ResourceType;

import java.util.Set;

public record AiCategorizationResponse(
        String suggestedTitle,
        String suggestedSubject,
        String suggestedTopic,
        Integer suggestedSemester,
        ResourceType suggestedType,
        Set<String> suggestedTags,
        String extractedSnippet
) {}

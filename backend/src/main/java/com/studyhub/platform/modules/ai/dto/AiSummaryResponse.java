package com.studyhub.platform.modules.ai.dto;

import java.util.List;
import java.util.Set;

public record AiSummaryResponse(
        Long resourceId,
        String shortSummary,
        List<String> keyPoints,
        List<String> examRevisionNotes,
        Set<String> suggestedTags
) {}

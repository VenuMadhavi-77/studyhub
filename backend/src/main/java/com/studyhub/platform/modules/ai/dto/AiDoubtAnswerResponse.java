package com.studyhub.platform.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiDoubtAnswerResponse(
        String answer,
        String directAnswer,
        String stepByStepExplanation,
        String keyTakeaways,
        String practiceQuestion,
        boolean isAiGenerated,
        String provider
) {
    public AiDoubtAnswerResponse(String answer, boolean isAiGenerated, String provider) {
        this(answer, answer, null, null, null, isAiGenerated, provider);
    }
}

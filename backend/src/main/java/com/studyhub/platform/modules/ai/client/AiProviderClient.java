package com.studyhub.platform.modules.ai.client;

import com.studyhub.platform.modules.ai.dto.AiCategorizationResponse;
import com.studyhub.platform.modules.ai.dto.AiSearchCriteriaResult;
import com.studyhub.platform.modules.ai.dto.AiSummaryResponse;

public interface AiProviderClient {
    String getProviderName();
    AiSummaryResponse summarize(String documentText, String resourceTitle);
    AiCategorizationResponse categorize(String documentText, String fallbackTitle);
    String answerDoubt(String question, String subjectContext);
    AiSearchCriteriaResult parseNaturalSearch(String naturalQuery);
}

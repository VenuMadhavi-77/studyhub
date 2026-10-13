package com.studyhub.platform.modules.ai.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiDoubtAskRequest(
        @JsonAlias({"content", "title"})
        String question,

        @JsonAlias({"subjectName"})
        String subjectContext,

        String title,
        String content,
        String subjectName
) {
    public AiDoubtAskRequest(String question, String subjectContext) {
        this(question, subjectContext, null, null, null);
    }

    public String getEffectiveQuestion() {
        if (question != null && !question.isBlank()) return question.trim();
        if (content != null && !content.isBlank()) return content.trim();
        if (title != null && !title.isBlank()) return title.trim();
        return "";
    }

    public String getEffectiveSubject() {
        if (subjectContext != null && !subjectContext.isBlank()) return subjectContext.trim();
        if (subjectName != null && !subjectName.isBlank()) return subjectName.trim();
        return "Academic Studies";
    }
}

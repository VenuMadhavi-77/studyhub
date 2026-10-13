package com.studyhub.platform.modules.doubt.dto;

import com.studyhub.platform.modules.doubt.entity.Answer;

import java.time.Instant;

public record AnswerResponse(
        Long id,
        Long doubtId,
        Long userId,
        String userName,
        String content,
        boolean isAiGenerated,
        Integer upvoteCount,
        boolean upvotedByCurrentUser,
        Instant createdAt,
        Instant updatedAt
) {
    public static AnswerResponse fromEntity(Answer answer, boolean upvoted) {
        return new AnswerResponse(
                answer.getId(),
                answer.getDoubt() != null ? answer.getDoubt().getId() : null,
                answer.getUser() != null ? answer.getUser().getId() : null,
                answer.getUser() != null ? answer.getUser().getFullName() : null,
                answer.getContent(),
                answer.isAiGenerated(),
                answer.getUpvoteCount(),
                upvoted,
                answer.getCreatedAt(),
                answer.getUpdatedAt()
        );
    }
}

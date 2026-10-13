package com.studyhub.platform.modules.doubt.dto;

import com.studyhub.platform.common.enums.DoubtStatus;
import com.studyhub.platform.modules.doubt.entity.Doubt;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record DoubtResponse(
        Long id,
        String title,
        String content,
        Long subjectId,
        String subjectName,
        Long userId,
        String userName,
        DoubtStatus status,
        Long acceptedAnswerId,
        int answerCount,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static DoubtResponse fromEntity(Doubt doubt) {
        return new DoubtResponse(
                doubt.getId(),
                doubt.getTitle(),
                doubt.getContent(),
                doubt.getSubject() != null ? doubt.getSubject().getId() : null,
                doubt.getSubject() != null ? doubt.getSubject().getName() : null,
                doubt.getUser() != null ? doubt.getUser().getId() : null,
                doubt.getUser() != null ? doubt.getUser().getFullName() : null,
                doubt.getStatus(),
                doubt.getAcceptedAnswerId(),
                doubt.getAnswers() != null ? doubt.getAnswers().size() : 0,
                doubt.getTags() != null ? doubt.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()) : Set.of(),
                doubt.getCreatedAt(),
                doubt.getUpdatedAt()
        );
    }
}

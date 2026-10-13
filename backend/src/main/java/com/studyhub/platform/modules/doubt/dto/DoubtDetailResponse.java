package com.studyhub.platform.modules.doubt.dto;

import com.studyhub.platform.common.enums.DoubtStatus;
import com.studyhub.platform.modules.doubt.entity.Doubt;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record DoubtDetailResponse(
        Long id,
        String title,
        String content,
        Long subjectId,
        String subjectName,
        Long userId,
        String userName,
        DoubtStatus status,
        Long acceptedAnswerId,
        Set<String> tags,
        List<AnswerResponse> answers,
        Instant createdAt,
        Instant updatedAt
) {
    public static DoubtDetailResponse fromEntity(Doubt doubt, List<AnswerResponse> answers) {
        return new DoubtDetailResponse(
                doubt.getId(),
                doubt.getTitle(),
                doubt.getContent(),
                doubt.getSubject() != null ? doubt.getSubject().getId() : null,
                doubt.getSubject() != null ? doubt.getSubject().getName() : null,
                doubt.getUser() != null ? doubt.getUser().getId() : null,
                doubt.getUser() != null ? doubt.getUser().getFullName() : null,
                doubt.getStatus(),
                doubt.getAcceptedAnswerId(),
                doubt.getTags() != null ? doubt.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()) : Set.of(),
                answers,
                doubt.getCreatedAt(),
                doubt.getUpdatedAt()
        );
    }
}

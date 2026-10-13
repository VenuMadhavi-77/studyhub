package com.studyhub.platform.modules.resource.dto;

import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.modules.resource.entity.Resource;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record ResourceDetailResponse(
        Long id,
        String title,
        String description,
        ResourceType type,
        String filePath,
        String fileMimeType,
        Long fileSizeBytes,
        String externalUrl,
        Long departmentId,
        String departmentName,
        Long subjectId,
        String subjectName,
        Long topicId,
        String topicName,
        Integer semester,
        Long uploaderId,
        String uploaderName,
        ResourceStatus status,
        String rejectionReason,
        String aiSummary,
        String aiKeyPoints,
        String aiRevisionNotes,
        Double avgRating,
        Integer ratingCount,
        Integer usefulCount,
        Integer viewCount,
        Integer downloadCount,
        boolean bookmarkedByCurrentUser,
        boolean votedUsefulByCurrentUser,
        Integer userRatingScore,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResourceDetailResponse fromEntity(Resource resource, boolean bookmarked, boolean usefulVoted, Integer userScore) {
        return new ResourceDetailResponse(
                resource.getId(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getType(),
                resource.getFilePath(),
                resource.getFileMimeType(),
                resource.getFileSizeBytes(),
                resource.getExternalUrl(),
                resource.getDepartment() != null ? resource.getDepartment().getId() : null,
                resource.getDepartment() != null ? resource.getDepartment().getName() : null,
                resource.getSubject() != null ? resource.getSubject().getId() : null,
                resource.getSubject() != null ? resource.getSubject().getName() : null,
                resource.getTopic() != null ? resource.getTopic().getId() : null,
                resource.getTopic() != null ? resource.getTopic().getName() : null,
                resource.getSemester(),
                resource.getUploader() != null ? resource.getUploader().getId() : null,
                resource.getUploader() != null ? resource.getUploader().getFullName() : null,
                resource.getStatus(),
                resource.getRejectionReason(),
                resource.getAiSummary(),
                resource.getAiKeyPoints(),
                resource.getAiRevisionNotes(),
                resource.getAvgRating(),
                resource.getRatingCount(),
                resource.getUsefulCount(),
                resource.getViewCount(),
                resource.getDownloadCount(),
                bookmarked,
                usefulVoted,
                userScore,
                resource.getTags() != null ? resource.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()) : Set.of(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}

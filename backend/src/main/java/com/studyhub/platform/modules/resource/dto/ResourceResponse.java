package com.studyhub.platform.modules.resource.dto;

import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.common.enums.SourcePlatform;
import com.studyhub.platform.modules.resource.entity.Resource;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record ResourceResponse(
        Long id,
        String title,
        String description,
        ResourceType type,
        SourcePlatform sourcePlatform,
        ContentLevel level,
        ContentLanguage language,
        boolean isFree,
        String bestFor,
        boolean verified,
        String verifiedBy,
        Instant verifiedAt,
        Double reliabilityScore,
        String reliabilityLabel,
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
        Double avgRating,
        Integer ratingCount,
        Integer usefulCount,
        Integer viewCount,
        Integer downloadCount,
        String aiSummary,
        String aiKeyPoints,
        String aiRevisionNotes,
        Set<String> tags,
        Instant createdAt,
        Instant updatedAt
) {
    public static ResourceResponse fromEntity(Resource resource) {
        return new ResourceResponse(
                resource.getId(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getType(),
                resource.getSourcePlatform(),
                resource.getLevel(),
                resource.getLanguage(),
                resource.isFree(),
                resource.getBestFor(),
                resource.isVerified(),
                resource.getVerifiedBy(),
                resource.getVerifiedAt(),
                resource.getReliabilityScore(),
                resource.getReliabilityLabel(),
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
                resource.getAvgRating(),
                resource.getRatingCount(),
                resource.getUsefulCount(),
                resource.getViewCount(),
                resource.getDownloadCount(),
                resource.getAiSummary(),
                resource.getAiKeyPoints(),
                resource.getAiRevisionNotes(),
                resource.getTags() != null ? resource.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()) : Set.of(),
                resource.getCreatedAt(),
                resource.getUpdatedAt()
        );
    }
}


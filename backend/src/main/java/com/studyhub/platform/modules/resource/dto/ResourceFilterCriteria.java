package com.studyhub.platform.modules.resource.dto;

import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.ResourceType;

public record ResourceFilterCriteria(
        String query,
        Long departmentId,
        Long subjectId,
        Long topicId,
        Integer semester,
        ResourceType type,
        String tag,
        Double minRating,
        ResourceStatus status
) {
    public ResourceFilterCriteria withDefaultStatus() {
        return new ResourceFilterCriteria(
                query,
                departmentId,
                subjectId,
                topicId,
                semester,
                type,
                tag,
                minRating,
                status != null ? status : ResourceStatus.APPROVED
        );
    }
}

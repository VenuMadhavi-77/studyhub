package com.studyhub.platform.modules.resource.dto;

import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.common.enums.SourcePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ExternalResourceRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Resource type is required")
        ResourceType type,

        @NotNull(message = "Source platform is required")
        SourcePlatform sourcePlatform,

        @NotNull(message = "Content level is required")
        ContentLevel level,

        @NotNull(message = "Content language is required")
        ContentLanguage language,

        Boolean isFree,

        @Size(max = 255, message = "Best for text must not exceed 255 characters")
        String bestFor,

        Boolean verified,

        @NotBlank(message = "External URL is required")
        @Size(max = 1000, message = "URL must not exceed 1000 characters")
        String externalUrl,

        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotNull(message = "Subject ID is required")
        Long subjectId,

        Long topicId,

        @NotNull(message = "Semester is required")
        Integer semester,

        Set<String> tags
) {}

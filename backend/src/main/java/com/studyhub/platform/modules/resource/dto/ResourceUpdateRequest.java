package com.studyhub.platform.modules.resource.dto;

import com.studyhub.platform.common.enums.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ResourceUpdateRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
        String title,

        String description,

        @NotNull(message = "Resource type is required")
        ResourceType type,

        Long topicId,

        Integer semester,

        String externalUrl,

        Set<String> tags
) {}

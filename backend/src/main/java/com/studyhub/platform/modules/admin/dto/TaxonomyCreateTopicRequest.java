package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaxonomyCreateTopicRequest(
        @NotNull(message = "Subject ID is required")
        Long subjectId,

        @NotBlank(message = "Topic name is required")
        String name
) {}

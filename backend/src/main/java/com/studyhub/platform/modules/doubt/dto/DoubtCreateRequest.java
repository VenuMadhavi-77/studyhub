package com.studyhub.platform.modules.doubt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record DoubtCreateRequest(
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
        String title,

        @NotBlank(message = "Content cannot be blank")
        String content,

        @NotNull(message = "Subject is required")
        Long subjectId,

        Set<String> tags
) {}

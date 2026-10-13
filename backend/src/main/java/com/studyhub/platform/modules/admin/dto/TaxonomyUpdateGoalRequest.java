package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaxonomyUpdateGoalRequest(
        Long departmentId, // Nullable = shared

        @NotBlank(message = "Goal label is required")
        @Size(max = 150, message = "Goal label must not exceed 150 characters")
        String label,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @NotNull(message = "Active status is required")
        Boolean active
) {}

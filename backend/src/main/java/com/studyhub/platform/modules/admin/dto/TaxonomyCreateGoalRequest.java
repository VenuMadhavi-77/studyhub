package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaxonomyCreateGoalRequest(
        Long departmentId, // Nullable = shared across all departments

        @NotBlank(message = "Goal key is required")
        @Size(max = 100, message = "Goal key must not exceed 100 characters")
        String goalKey,

        @NotBlank(message = "Goal label is required")
        @Size(max = 150, message = "Goal label must not exceed 150 characters")
        String label,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description
) {}

package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaxonomyCreateToolRequest(
        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotBlank(message = "Tool name is required")
        @Size(max = 100, message = "Tool name must not exceed 100 characters")
        String name
) {}

package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaxonomyUpdateDepartmentRequest(
        @NotBlank(message = "Department name is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Department code is required")
        @Size(max = 30, message = "Code must not exceed 30 characters")
        String code,

        @NotNull(message = "Active status is required")
        Boolean active
) {}

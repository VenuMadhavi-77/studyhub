package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TaxonomyUpdateSubjectRequest(
        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotBlank(message = "Subject name is required")
        @Size(max = 150, message = "Subject name must not exceed 150 characters")
        String name,

        @NotBlank(message = "Subject code is required")
        @Size(max = 50, message = "Subject code must not exceed 50 characters")
        String code,

        @NotNull(message = "Semester is required")
        Integer semester,

        @NotNull(message = "Active status is required")
        Boolean active
) {}

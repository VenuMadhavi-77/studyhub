package com.studyhub.platform.modules.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TaxonomyCreateSubjectRequest(
        @NotNull(message = "Department ID is required")
        Long departmentId,

        @NotBlank(message = "Subject name is required")
        String name,

        @NotBlank(message = "Subject code is required")
        String code,

        @NotNull(message = "Semester is required")
        Integer semester
) {}

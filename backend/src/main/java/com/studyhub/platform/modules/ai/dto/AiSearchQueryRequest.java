package com.studyhub.platform.modules.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AiSearchQueryRequest(
        @NotBlank(message = "Natural language search prompt cannot be blank")
        String prompt
) {}

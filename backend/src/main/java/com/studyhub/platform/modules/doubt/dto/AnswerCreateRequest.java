package com.studyhub.platform.modules.doubt.dto;

import jakarta.validation.constraints.NotBlank;

public record AnswerCreateRequest(
        @NotBlank(message = "Answer content cannot be blank")
        String content
) {}

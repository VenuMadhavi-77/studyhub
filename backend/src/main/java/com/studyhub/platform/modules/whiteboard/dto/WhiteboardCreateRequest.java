package com.studyhub.platform.modules.whiteboard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WhiteboardCreateRequest(
        @NotBlank(message = "Session title cannot be blank")
        @Size(min = 3, max = 150, message = "Session title must be between 3 and 150 characters")
        String title
) {}

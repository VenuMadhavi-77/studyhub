package com.studyhub.platform.modules.focus.dto;

import com.studyhub.platform.common.enums.FocusMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FocusStartRequest(
        Long subjectId,

        @NotNull(message = "Focus mode is required")
        FocusMode mode,

        @Min(value = 1, message = "Planned minutes must be at least 1")
        @Max(value = 480, message = "Planned minutes cannot exceed 480")
        Integer plannedMinutes
) {}

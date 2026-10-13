package com.studyhub.platform.modules.whiteboard.dto;

import jakarta.validation.constraints.NotBlank;

public record WhiteboardSnapshotRequest(
        @NotBlank(message = "Snapshot state JSON cannot be blank")
        String stateJson
) {}

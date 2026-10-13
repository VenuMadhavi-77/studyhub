package com.studyhub.platform.modules.whiteboard.dto;

import java.time.Instant;

public record WhiteboardSnapshotDto(
        Long id,
        Long sessionId,
        String sessionCode,
        String stateJson,
        Integer sequenceNumber,
        Instant createdAt
) {}

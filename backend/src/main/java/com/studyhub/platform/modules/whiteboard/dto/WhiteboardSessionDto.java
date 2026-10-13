package com.studyhub.platform.modules.whiteboard.dto;

import java.time.Instant;

public record WhiteboardSessionDto(
        Long id,
        String sessionCode,
        String title,
        Long hostId,
        String hostName,
        boolean active,
        Instant createdAt
) {}

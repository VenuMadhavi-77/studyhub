package com.studyhub.platform.modules.whiteboard.dto;

import java.util.Map;

public record WhiteboardDrawEvent(
        String type, // DRAW_LINE, DRAW_SHAPE, ADD_TEXT, CLEAR_CANVAS, CURSOR_MOVE
        String sessionCode,
        Long userId,
        String userName,
        Long sequenceId,
        Map<String, Object> payload,
        long timestamp
) {}

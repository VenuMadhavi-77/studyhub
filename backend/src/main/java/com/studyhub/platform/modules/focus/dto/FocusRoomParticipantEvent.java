package com.studyhub.platform.modules.focus.dto;

public record FocusRoomParticipantEvent(
        String roomCode,
        Long userId,
        String userName,
        String avatar,
        String status, // "FOCUSING", "ON_BREAK", "LEFT"
        String currentSubject,
        int elapsedMinutes,
        long timestamp
) {}

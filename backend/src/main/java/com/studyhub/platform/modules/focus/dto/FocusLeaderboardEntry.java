package com.studyhub.platform.modules.focus.dto;

public record FocusLeaderboardEntry(
        int rank,
        Long userId,
        String fullName,
        String departmentName,
        int totalMinutes,
        int streakDays,
        int sessionsCount
) {}

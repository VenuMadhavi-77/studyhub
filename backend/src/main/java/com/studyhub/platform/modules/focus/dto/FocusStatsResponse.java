package com.studyhub.platform.modules.focus.dto;

import java.util.Map;

public record FocusStatsResponse(
        int todayMinutes,
        int thisWeekMinutes,
        int totalMinutes,
        int streakDays,
        int completedSessionsCount,
        Map<String, Integer> subjectBreakdownMinutes,
        FocusSessionResponse activeSession
) {}

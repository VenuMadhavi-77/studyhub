package com.studyhub.platform.modules.focus.dto;

import com.studyhub.platform.common.enums.FocusMode;
import com.studyhub.platform.common.enums.FocusStatus;
import com.studyhub.platform.modules.focus.entity.FocusSession;

import java.time.Instant;

public record FocusSessionResponse(
        Long id,
        Long userId,
        String userName,
        Long subjectId,
        String subjectName,
        FocusMode mode,
        Integer plannedMinutes,
        Integer actualMinutes,
        Instant startedAt,
        Instant endedAt,
        FocusStatus status
) {
    public static FocusSessionResponse fromEntity(FocusSession session) {
        return new FocusSessionResponse(
                session.getId(),
                session.getUser().getId(),
                session.getUser().getFullName(),
                session.getSubject() != null ? session.getSubject().getId() : null,
                session.getSubject() != null ? session.getSubject().getName() : null,
                session.getMode(),
                session.getPlannedMinutes(),
                session.getActualMinutes(),
                session.getStartedAt(),
                session.getEndedAt(),
                session.getStatus()
        );
    }
}

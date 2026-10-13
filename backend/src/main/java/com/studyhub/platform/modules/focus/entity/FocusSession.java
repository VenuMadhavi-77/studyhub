package com.studyhub.platform.modules.focus.entity;

import com.studyhub.platform.common.enums.FocusMode;
import com.studyhub.platform.common.enums.FocusStatus;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "focus_sessions",
        indexes = {
                @Index(name = "idx_focus_user_started", columnList = "user_id, started_at")
        }
)
public class FocusSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FocusMode mode = FocusMode.POMODORO;

    @Column(name = "planned_minutes", nullable = false)
    private Integer plannedMinutes = 25;

    @Column(name = "actual_minutes", nullable = false)
    private Integer actualMinutes = 0;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "ended_at")
    private Instant endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FocusStatus status = FocusStatus.IN_PROGRESS;

    public FocusSession() {}

    public FocusSession(User user, Subject subject, FocusMode mode, Integer plannedMinutes) {
        this.user = user;
        this.subject = subject;
        this.mode = mode;
        this.plannedMinutes = plannedMinutes;
        this.actualMinutes = 0;
        this.startedAt = Instant.now();
        this.status = FocusStatus.IN_PROGRESS;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public FocusMode getMode() { return mode; }
    public void setMode(FocusMode mode) { this.mode = mode; }

    public Integer getPlannedMinutes() { return plannedMinutes; }
    public void setPlannedMinutes(Integer plannedMinutes) { this.plannedMinutes = plannedMinutes; }

    public Integer getActualMinutes() { return actualMinutes; }
    public void setActualMinutes(Integer actualMinutes) { this.actualMinutes = actualMinutes; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getEndedAt() { return endedAt; }
    public void setEndedAt(Instant endedAt) { this.endedAt = endedAt; }

    public FocusStatus getStatus() { return status; }
    public void setStatus(FocusStatus status) { this.status = status; }
}

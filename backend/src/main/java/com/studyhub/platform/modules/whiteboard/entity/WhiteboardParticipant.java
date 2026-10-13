package com.studyhub.platform.modules.whiteboard.entity;

import com.studyhub.platform.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "whiteboard_participants",
        uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "user_id"})
)
public class WhiteboardParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private WhiteboardSession session;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private Instant joinedAt = Instant.now();

    public WhiteboardParticipant() {}

    public WhiteboardParticipant(WhiteboardSession session, User user) {
        this.session = session;
        this.user = user;
        this.joinedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WhiteboardSession getSession() { return session; }
    public void setSession(WhiteboardSession session) { this.session = session; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getJoinedAt() { return joinedAt; }
    public void setJoinedAt(Instant joinedAt) { this.joinedAt = joinedAt; }
}

package com.studyhub.platform.modules.whiteboard.entity;

import com.studyhub.platform.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "whiteboard_sessions")
public class WhiteboardSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_code", nullable = false, unique = true, length = 50)
    private String sessionCode;

    @Column(nullable = false, length = 150)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<WhiteboardSnapshot> snapshots = new ArrayList<>();

    public WhiteboardSession() {}

    public WhiteboardSession(String sessionCode, String title, User host) {
        this.sessionCode = sessionCode;
        this.title = title;
        this.host = host;
        this.active = true;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSessionCode() { return sessionCode; }
    public void setSessionCode(String sessionCode) { this.sessionCode = sessionCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public User getHost() { return host; }
    public void setHost(User host) { this.host = host; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public List<WhiteboardSnapshot> getSnapshots() { return snapshots; }
    public void setSnapshots(List<WhiteboardSnapshot> snapshots) { this.snapshots = snapshots; }
}

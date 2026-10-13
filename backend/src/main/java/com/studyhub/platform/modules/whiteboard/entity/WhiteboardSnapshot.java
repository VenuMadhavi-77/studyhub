package com.studyhub.platform.modules.whiteboard.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "whiteboard_snapshots")
public class WhiteboardSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private WhiteboardSession session;

    @Column(name = "state_json", columnDefinition = "LONGTEXT", nullable = false)
    private String stateJson;

    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public WhiteboardSnapshot() {}

    public WhiteboardSnapshot(WhiteboardSession session, String stateJson, Integer sequenceNumber) {
        this.session = session;
        this.stateJson = stateJson;
        this.sequenceNumber = sequenceNumber;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public WhiteboardSession getSession() { return session; }
    public void setSession(WhiteboardSession session) { this.session = session; }

    public String getStateJson() { return stateJson; }
    public void setStateJson(String stateJson) { this.stateJson = stateJson; }

    public Integer getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(Integer sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

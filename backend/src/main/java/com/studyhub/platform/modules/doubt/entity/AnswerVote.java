package com.studyhub.platform.modules.doubt.entity;

import com.studyhub.platform.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "answer_votes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "answer_id"})
)
public class AnswerVote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "answer_id", nullable = false)
    private Answer answer;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public AnswerVote() {}

    public AnswerVote(User user, Answer answer) {
        this.user = user;
        this.answer = answer;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Answer getAnswer() { return answer; }
    public void setAnswer(Answer answer) { this.answer = answer; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

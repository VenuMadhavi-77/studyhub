package com.studyhub.platform.modules.resource.entity;

import com.studyhub.platform.modules.auth.entity.User;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "view_histories")
public class ViewHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "viewed_at", nullable = false, updatable = false)
    private Instant viewedAt = Instant.now();

    public ViewHistory() {}

    public ViewHistory(User user, Resource resource, String ipAddress) {
        this.user = user;
        this.resource = resource;
        this.ipAddress = ipAddress;
        this.viewedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Resource getResource() { return resource; }
    public void setResource(Resource resource) { this.resource = resource; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public Instant getViewedAt() { return viewedAt; }
    public void setViewedAt(Instant viewedAt) { this.viewedAt = viewedAt; }
}

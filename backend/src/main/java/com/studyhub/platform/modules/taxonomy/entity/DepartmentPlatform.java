package com.studyhub.platform.modules.taxonomy.entity;

import com.studyhub.platform.common.enums.SourcePlatform;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "department_platforms")
public class DepartmentPlatform {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SourcePlatform platform;

    @Column(nullable = false)
    private Integer priority = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public DepartmentPlatform() {}

    public DepartmentPlatform(Department department, SourcePlatform platform, Integer priority) {
        this.department = department;
        this.platform = platform;
        this.priority = priority != null ? priority : 0;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public SourcePlatform getPlatform() { return platform; }
    public void setPlatform(SourcePlatform platform) { this.platform = platform; }

    public Integer getPriority() { return priority; }
    public void setPriority(Integer priority) { this.priority = priority; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

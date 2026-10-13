package com.studyhub.platform.modules.taxonomy.entity;

import com.studyhub.platform.common.entity.BaseAuditableEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "goals")
public class Goal extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department; // NULL = shared across all departments

    @Column(name = "goal_key", nullable = false, length = 100)
    private String goalKey;

    @Column(nullable = false, length = 150)
    private String label;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    public Goal() {}

    public Goal(Department department, String goalKey, String label, String description) {
        this.department = department;
        this.goalKey = goalKey;
        this.label = label;
        this.description = description;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public String getGoalKey() { return goalKey; }
    public void setGoalKey(String goalKey) { this.goalKey = goalKey; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

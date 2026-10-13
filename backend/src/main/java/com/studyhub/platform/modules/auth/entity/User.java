package com.studyhub.platform.modules.auth.entity;

import com.studyhub.platform.common.entity.BaseAuditableEntity;
import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@SQLDelete(sql = "UPDATE users SET deleted = true WHERE id = ?")
@SQLRestriction("deleted = false")
public class User extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Role role = Role.STUDENT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserStatus status = UserStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column
    private Integer semester;

    @Column(length = 255)
    private String goals;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_language", length = 30)
    private com.studyhub.platform.common.enums.ContentLanguage preferredLanguage = com.studyhub.platform.common.enums.ContentLanguage.ENGLISH;

    @Enumerated(EnumType.STRING)
    @Column(name = "level", length = 30)
    private com.studyhub.platform.common.enums.ContentLevel level = com.studyhub.platform.common.enums.ContentLevel.BEGINNER;

    @Column(nullable = false)
    private boolean deleted = false;

    public User() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getGoals() { return goals; }
    public void setGoals(String goals) { this.goals = goals; }

    public com.studyhub.platform.common.enums.ContentLanguage getPreferredLanguage() { return preferredLanguage; }
    public void setPreferredLanguage(com.studyhub.platform.common.enums.ContentLanguage preferredLanguage) { this.preferredLanguage = preferredLanguage; }

    public com.studyhub.platform.common.enums.ContentLevel getLevel() { return level; }
    public void setLevel(com.studyhub.platform.common.enums.ContentLevel level) { this.level = level; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
}

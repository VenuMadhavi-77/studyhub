package com.studyhub.platform.modules.taxonomy.repository;

import com.studyhub.platform.modules.taxonomy.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoalRepository extends JpaRepository<Goal, Long> {

    @Query("SELECT g FROM Goal g WHERE (g.department.id = :departmentId OR g.department IS NULL) AND g.active = true")
    List<Goal> findAvailableForDepartment(@Param("departmentId") Long departmentId);

    List<Goal> findByDepartmentIsNullAndActiveTrue();

    List<Goal> findByDepartmentIdAndActiveTrue(Long departmentId);

    Optional<Goal> findByGoalKeyIgnoreCase(String goalKey);
}

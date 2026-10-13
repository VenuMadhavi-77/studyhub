package com.studyhub.platform.modules.taxonomy.repository;

import com.studyhub.platform.modules.taxonomy.entity.DepartmentTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartmentToolRepository extends JpaRepository<DepartmentTool, Long> {
    List<DepartmentTool> findByDepartmentIdOrderByNameAsc(Long departmentId);
    boolean existsByDepartmentIdAndNameIgnoreCase(Long departmentId, String name);
}

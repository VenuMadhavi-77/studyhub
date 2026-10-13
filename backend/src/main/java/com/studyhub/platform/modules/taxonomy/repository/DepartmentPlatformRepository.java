package com.studyhub.platform.modules.taxonomy.repository;

import com.studyhub.platform.common.enums.SourcePlatform;
import com.studyhub.platform.modules.taxonomy.entity.DepartmentPlatform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentPlatformRepository extends JpaRepository<DepartmentPlatform, Long> {
    List<DepartmentPlatform> findByDepartmentIdOrderByPriorityDesc(Long departmentId);
    boolean existsByDepartmentIdAndPlatform(Long departmentId, SourcePlatform platform);
    Optional<DepartmentPlatform> findByDepartmentIdAndPlatform(Long departmentId, SourcePlatform platform);
}

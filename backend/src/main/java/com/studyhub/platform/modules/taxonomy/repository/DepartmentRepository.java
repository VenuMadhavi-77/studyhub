package com.studyhub.platform.modules.taxonomy.repository;

import com.studyhub.platform.modules.taxonomy.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByCode(String code);
    Optional<Department> findByNameIgnoreCase(String name);
    boolean existsByCode(String code);
    List<Department> findAllByActiveTrueOrderByNameAsc();
}

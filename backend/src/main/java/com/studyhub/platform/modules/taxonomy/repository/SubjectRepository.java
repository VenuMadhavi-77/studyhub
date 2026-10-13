package com.studyhub.platform.modules.taxonomy.repository;

import com.studyhub.platform.modules.taxonomy.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByCode(String code);
    Optional<Subject> findByCodeIgnoreCase(String code);
    Optional<Subject> findByNameIgnoreCaseAndDepartmentId(String name, Long departmentId);
    boolean existsByCodeIgnoreCase(String code);
    List<Subject> findByDepartmentIdAndActiveTrueOrderByNameAsc(Long departmentId);
    List<Subject> findByDepartmentIdAndSemesterAndActiveTrueOrderByNameAsc(Long departmentId, Integer semester);
}

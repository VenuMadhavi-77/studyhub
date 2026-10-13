package com.studyhub.platform.modules.resource.repository;

import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.modules.resource.entity.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Long>, JpaSpecificationExecutor<Resource> {

    @EntityGraph(attributePaths = {"department", "subject", "topic", "uploader", "tags"})
    Optional<Resource> findWithDetailsById(Long id);

    @Override
    @EntityGraph(attributePaths = {"department", "subject", "topic", "uploader", "tags"})
    Page<Resource> findAll(Specification<Resource> spec, Pageable pageable);

    Page<Resource> findByUploaderIdOrderByCreatedAtDesc(Long uploaderId, Pageable pageable);

    Page<Resource> findByStatusOrderByCreatedAtDesc(ResourceStatus status, Pageable pageable);

    @Query("SELECT COUNT(r) FROM Resource r WHERE r.status = :status AND r.deleted = false")
    long countByStatus(ResourceStatus status);

    @EntityGraph(attributePaths = {"department", "subject", "topic", "uploader", "tags"})
    List<Resource> findTop10ByStatusOrderByAvgRatingDescDownloadCountDesc(ResourceStatus status);

    @EntityGraph(attributePaths = {"department", "subject", "topic", "uploader", "tags"})
    List<Resource> findTop10ByStatusAndDepartmentIdOrderByAvgRatingDescDownloadCountDesc(ResourceStatus status, Long departmentId);

    @EntityGraph(attributePaths = {"department", "subject", "topic", "uploader", "tags"})
    List<Resource> findTop10ByStatusAndDepartmentIdAndSemesterOrderByAvgRatingDescDownloadCountDesc(ResourceStatus status, Long departmentId, Integer semester);

    @EntityGraph(attributePaths = {"department", "subject", "topic", "uploader", "tags"})
    List<Resource> findTop10ByStatusAndSubjectIdOrderByAvgRatingDescDownloadCountDesc(ResourceStatus status, Long subjectId);
}

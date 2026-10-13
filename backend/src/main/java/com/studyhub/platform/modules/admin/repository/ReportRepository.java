package com.studyhub.platform.modules.admin.repository;

import com.studyhub.platform.common.enums.ReportStatus;
import com.studyhub.platform.modules.admin.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    @EntityGraph(attributePaths = {"reporter", "resolvedByAdmin"})
    Page<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"reporter", "resolvedByAdmin"})
    Page<Report> findAllByOrderByCreatedAtDesc(Pageable pageable);

    long countByStatus(ReportStatus status);

    long countByTargetTypeAndTargetIdAndStatus(com.studyhub.platform.common.enums.ReportTargetType targetType, Long targetId, ReportStatus status);
}


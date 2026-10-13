package com.studyhub.platform.modules.admin.repository;

import com.studyhub.platform.modules.admin.entity.ModerationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ModerationLogRepository extends JpaRepository<ModerationLog, Long> {

    @EntityGraph(attributePaths = {"admin"})
    Page<ModerationLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

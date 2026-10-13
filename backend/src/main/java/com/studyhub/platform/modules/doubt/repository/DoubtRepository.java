package com.studyhub.platform.modules.doubt.repository;

import com.studyhub.platform.common.enums.DoubtStatus;
import com.studyhub.platform.modules.doubt.entity.Doubt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoubtRepository extends JpaRepository<Doubt, Long> {

    @EntityGraph(attributePaths = {"user", "subject", "tags"})
    Optional<Doubt> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"user", "subject", "tags"})
    Page<Doubt> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = {"user", "subject", "tags"})
    Page<Doubt> findBySubjectIdOrderByCreatedAtDesc(Long subjectId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "subject", "tags"})
    Page<Doubt> findByStatusOrderByCreatedAtDesc(DoubtStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "subject", "tags"})
    Page<Doubt> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}

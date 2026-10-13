package com.studyhub.platform.modules.focus.repository;

import com.studyhub.platform.common.enums.FocusStatus;
import com.studyhub.platform.modules.focus.entity.FocusSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    Optional<FocusSession> findTopByUserIdAndStatusOrderByStartedAtDesc(Long userId, FocusStatus status);

    boolean existsByUserIdAndStatus(Long userId, FocusStatus status);

    List<FocusSession> findByUserIdAndStartedAtBetweenOrderByStartedAtAsc(Long userId, Instant start, Instant end);

    List<FocusSession> findByUserIdAndStatusOrderByStartedAtDesc(Long userId, FocusStatus status);

    @Query("SELECT s FROM FocusSession s WHERE s.startedAt >= :start AND s.status = 'COMPLETED'")
    List<FocusSession> findCompletedSessionsSince(@Param("start") Instant start);

    @Query("SELECT s FROM FocusSession s WHERE s.user.department.id = :deptId AND s.startedAt >= :start AND s.status = 'COMPLETED'")
    List<FocusSession> findCompletedSessionsByDepartmentSince(@Param("deptId") Long deptId, @Param("start") Instant start);
}

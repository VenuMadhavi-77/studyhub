package com.studyhub.platform.modules.resource.repository;

import com.studyhub.platform.modules.resource.entity.ViewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ViewHistoryRepository extends JpaRepository<ViewHistory, Long> {
    Optional<ViewHistory> findTopByResourceIdAndUserIdAndViewedAtAfterOrderByViewedAtDesc(
            Long resourceId, Long userId, Instant afterTime
    );

    Optional<ViewHistory> findTopByResourceIdAndIpAddressAndViewedAtAfterOrderByViewedAtDesc(
            Long resourceId, String ipAddress, Instant afterTime
    );

    java.util.List<ViewHistory> findByUserId(Long userId);
}


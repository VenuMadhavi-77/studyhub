package com.studyhub.platform.modules.ai.repository;

import com.studyhub.platform.common.enums.AiFeature;
import com.studyhub.platform.modules.ai.entity.AIInteraction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;

@Repository
public interface AIInteractionRepository extends JpaRepository<AIInteraction, Long> {
    Page<AIInteraction> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query("SELECT COUNT(a) FROM AIInteraction a WHERE a.user.id = :userId AND a.createdAt >= :since")
    long countDailyUserRequests(Long userId, Instant since);

    long countByFeature(AiFeature feature);
}

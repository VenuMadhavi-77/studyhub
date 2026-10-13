package com.studyhub.platform.modules.resource.repository;

import com.studyhub.platform.modules.resource.entity.UsefulVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsefulVoteRepository extends JpaRepository<UsefulVote, Long> {
    Optional<UsefulVote> findByUserIdAndResourceId(Long userId, Long resourceId);
    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);
    void deleteByUserIdAndResourceId(Long userId, Long resourceId);
    long countByResourceId(Long resourceId);
}

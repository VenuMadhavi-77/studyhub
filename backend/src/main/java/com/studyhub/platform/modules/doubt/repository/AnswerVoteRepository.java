package com.studyhub.platform.modules.doubt.repository;

import com.studyhub.platform.modules.doubt.entity.AnswerVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerVoteRepository extends JpaRepository<AnswerVote, Long> {
    Optional<AnswerVote> findByUserIdAndAnswerId(Long userId, Long answerId);
    boolean existsByUserIdAndAnswerId(Long userId, Long answerId);
    void deleteByUserIdAndAnswerId(Long userId, Long answerId);
    long countByAnswerId(Long answerId);
}

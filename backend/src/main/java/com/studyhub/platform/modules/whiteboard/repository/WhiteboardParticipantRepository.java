package com.studyhub.platform.modules.whiteboard.repository;

import com.studyhub.platform.modules.whiteboard.entity.WhiteboardParticipant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WhiteboardParticipantRepository extends JpaRepository<WhiteboardParticipant, Long> {
    Optional<WhiteboardParticipant> findBySessionIdAndUserId(Long sessionId, Long userId);
    boolean existsBySessionIdAndUserId(Long sessionId, Long userId);

    @EntityGraph(attributePaths = {"user"})
    List<WhiteboardParticipant> findBySessionId(Long sessionId);

    void deleteBySessionIdAndUserId(Long sessionId, Long userId);
}

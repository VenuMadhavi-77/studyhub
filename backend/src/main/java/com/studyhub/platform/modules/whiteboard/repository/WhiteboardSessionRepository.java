package com.studyhub.platform.modules.whiteboard.repository;

import com.studyhub.platform.modules.whiteboard.entity.WhiteboardSession;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhiteboardSessionRepository extends JpaRepository<WhiteboardSession, Long> {

    @EntityGraph(attributePaths = {"host"})
    Optional<WhiteboardSession> findBySessionCode(String sessionCode);

    boolean existsBySessionCode(String sessionCode);
}

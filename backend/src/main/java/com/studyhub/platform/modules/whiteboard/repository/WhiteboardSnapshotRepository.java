package com.studyhub.platform.modules.whiteboard.repository;

import com.studyhub.platform.modules.whiteboard.entity.WhiteboardSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WhiteboardSnapshotRepository extends JpaRepository<WhiteboardSnapshot, Long> {
    Optional<WhiteboardSnapshot> findTopBySessionIdOrderBySequenceNumberDesc(Long sessionId);
}

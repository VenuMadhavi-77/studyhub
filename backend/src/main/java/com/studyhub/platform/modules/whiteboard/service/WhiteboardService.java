package com.studyhub.platform.modules.whiteboard.service;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardCreateRequest;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardSessionDto;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardSnapshotDto;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardSnapshotRequest;
import com.studyhub.platform.modules.whiteboard.entity.WhiteboardSession;
import com.studyhub.platform.modules.whiteboard.entity.WhiteboardSnapshot;
import com.studyhub.platform.modules.whiteboard.repository.WhiteboardSessionRepository;
import com.studyhub.platform.modules.whiteboard.repository.WhiteboardSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class WhiteboardService {

    private final WhiteboardSessionRepository sessionRepository;
    private final WhiteboardSnapshotRepository snapshotRepository;
    private final UserRepository userRepository;
    private final SecureRandom random = new SecureRandom();

    public WhiteboardService(
            WhiteboardSessionRepository sessionRepository,
            WhiteboardSnapshotRepository snapshotRepository,
            UserRepository userRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.snapshotRepository = snapshotRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public WhiteboardSessionDto createSession(Long hostId, WhiteboardCreateRequest request) {
        User host = userRepository.findById(hostId)
                .orElseThrow(() -> new ResourceNotFoundException("Host user not found with id: " + hostId));

        String sessionCode;
        int attempts = 0;
        do {
            sessionCode = generateSessionCode();
            attempts++;
            if (attempts > 10) {
                throw new IllegalStateException("Failed to generate unique whiteboard session code");
            }
        } while (sessionRepository.existsBySessionCode(sessionCode));

        WhiteboardSession session = new WhiteboardSession(sessionCode, request.title().trim(), host);
        WhiteboardSession saved = sessionRepository.save(session);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public WhiteboardSessionDto getSession(String sessionCode) {
        WhiteboardSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Whiteboard session not found: " + sessionCode));
        return mapToDto(session);
    }

    @Transactional
    public WhiteboardSnapshotDto saveSnapshot(String sessionCode, Long userId, WhiteboardSnapshotRequest request) {
        WhiteboardSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Whiteboard session not found: " + sessionCode));

        if (!session.isActive()) {
            throw new BadRequestException("Cannot save snapshot to an inactive/closed whiteboard session");
        }

        int nextSequence = snapshotRepository.findTopBySessionIdOrderBySequenceNumberDesc(session.getId())
                .map(s -> s.getSequenceNumber() + 1)
                .orElse(1);

        WhiteboardSnapshot snapshot = new WhiteboardSnapshot(session, request.stateJson(), nextSequence);
        WhiteboardSnapshot saved = snapshotRepository.save(snapshot);

        return new WhiteboardSnapshotDto(
                saved.getId(),
                session.getId(),
                session.getSessionCode(),
                saved.getStateJson(),
                saved.getSequenceNumber(),
                saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public Optional<WhiteboardSnapshotDto> getLatestSnapshot(String sessionCode) {
        WhiteboardSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Whiteboard session not found: " + sessionCode));

        return snapshotRepository.findTopBySessionIdOrderBySequenceNumberDesc(session.getId())
                .map(s -> new WhiteboardSnapshotDto(
                        s.getId(),
                        session.getId(),
                        session.getSessionCode(),
                        s.getStateJson(),
                        s.getSequenceNumber(),
                        s.getCreatedAt()
                ));
    }

    @Transactional
    public WhiteboardSessionDto endSession(String sessionCode, Long currentUserId) {
        WhiteboardSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new ResourceNotFoundException("Whiteboard session not found: " + sessionCode));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + currentUserId));

        boolean isHost = session.getHost().getId().equals(currentUserId);
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isHost && !isAdmin) {
            throw new UnauthorizedException("Only the whiteboard session host or an administrator can end this session");
        }

        session.setActive(false);
        WhiteboardSession updated = sessionRepository.save(session);
        return mapToDto(updated);
    }

    private WhiteboardSessionDto mapToDto(WhiteboardSession s) {
        return new WhiteboardSessionDto(
                s.getId(),
                s.getSessionCode(),
                s.getTitle(),
                s.getHost().getId(),
                s.getHost().getFullName(),
                s.isActive(),
                s.getCreatedAt()
        );
    }

    private String generateSessionCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("WB-");
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}

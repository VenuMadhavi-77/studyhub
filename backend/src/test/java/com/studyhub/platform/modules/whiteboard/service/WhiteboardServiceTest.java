package com.studyhub.platform.modules.whiteboard.service;

import com.studyhub.platform.common.enums.Role;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WhiteboardServiceTest {

    @Mock
    private WhiteboardSessionRepository sessionRepository;

    @Mock
    private WhiteboardSnapshotRepository snapshotRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WhiteboardService whiteboardService;

    private User host;
    private User otherUser;

    @BeforeEach
    void setUp() {
        host = new User();
        host.setId(1L);
        host.setFullName("Host User");
        host.setEmail("host@studyhub.edu");
        host.setRole(Role.STUDENT);

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setFullName("Other User");
        otherUser.setEmail("other@studyhub.edu");
        otherUser.setRole(Role.STUDENT);
    }

    @Test
    void createSession_ShouldGenerateCodeAndSave() {
        WhiteboardCreateRequest req = new WhiteboardCreateRequest("Data Structures Study Room");
        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        when(sessionRepository.existsBySessionCode(anyString())).thenReturn(false);

        WhiteboardSession session = new WhiteboardSession("WB-TEST1234", "Data Structures Study Room", host);
        session.setId(10L);
        when(sessionRepository.save(any(WhiteboardSession.class))).thenReturn(session);

        WhiteboardSessionDto result = whiteboardService.createSession(1L, req);

        assertNotNull(result);
        assertEquals(10L, result.id());
        assertEquals("Data Structures Study Room", result.title());
        assertEquals(1L, result.hostId());
        assertTrue(result.active());
        verify(sessionRepository, times(1)).save(any(WhiteboardSession.class));
    }

    @Test
    void saveSnapshot_ShouldIncrementSequenceNumber() {
        WhiteboardSession session = new WhiteboardSession("WB-TEST1234", "Study Room", host);
        session.setId(10L);

        when(sessionRepository.findBySessionCode("WB-TEST1234")).thenReturn(Optional.of(session));

        WhiteboardSnapshot existingSnapshot = new WhiteboardSnapshot(session, "{\"lines\":[]}", 2);
        when(snapshotRepository.findTopBySessionIdOrderBySequenceNumberDesc(10L))
                .thenReturn(Optional.of(existingSnapshot));

        WhiteboardSnapshot savedSnapshot = new WhiteboardSnapshot(session, "{\"lines\":[{\"x\":1}]}", 3);
        savedSnapshot.setId(101L);
        when(snapshotRepository.save(any(WhiteboardSnapshot.class))).thenReturn(savedSnapshot);

        WhiteboardSnapshotDto dto = whiteboardService.saveSnapshot(
                "WB-TEST1234",
                1L,
                new WhiteboardSnapshotRequest("{\"lines\":[{\"x\":1}]}")
        );

        assertNotNull(dto);
        assertEquals(3, dto.sequenceNumber());
        assertEquals("WB-TEST1234", dto.sessionCode());
        verify(snapshotRepository, times(1)).save(any(WhiteboardSnapshot.class));
    }

    @Test
    void endSession_ShouldThrowUnauthorized_WhenNonHostNonAdmin() {
        WhiteboardSession session = new WhiteboardSession("WB-TEST1234", "Study Room", host);
        session.setId(10L);

        when(sessionRepository.findBySessionCode("WB-TEST1234")).thenReturn(Optional.of(session));
        when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser));

        assertThrows(UnauthorizedException.class, () -> whiteboardService.endSession("WB-TEST1234", 2L));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void endSession_ShouldSucceed_WhenHostRequests() {
        WhiteboardSession session = new WhiteboardSession("WB-TEST1234", "Study Room", host);
        session.setId(10L);

        when(sessionRepository.findBySessionCode("WB-TEST1234")).thenReturn(Optional.of(session));
        when(userRepository.findById(1L)).thenReturn(Optional.of(host));
        when(sessionRepository.save(any(WhiteboardSession.class))).thenReturn(session);

        WhiteboardSessionDto result = whiteboardService.endSession("WB-TEST1234", 1L);

        assertNotNull(result);
        assertFalse(session.isActive());
        verify(sessionRepository, times(1)).save(session);
    }
}

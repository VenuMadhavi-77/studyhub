package com.studyhub.platform.modules.focus.service;

import com.studyhub.platform.common.enums.FocusMode;
import com.studyhub.platform.common.enums.FocusStatus;
import com.studyhub.platform.common.exception.ConflictException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.focus.dto.FocusSessionResponse;
import com.studyhub.platform.modules.focus.dto.FocusStartRequest;
import com.studyhub.platform.modules.focus.entity.FocusSession;
import com.studyhub.platform.modules.focus.repository.FocusSessionRepository;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FocusServiceTest {

    @Mock
    private FocusSessionRepository focusSessionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @InjectMocks
    private FocusService focusService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(5L);
        user.setFullName("Focus Tester");
    }

    @Test
    @DisplayName("Overlap rejection: Starting a new session while another is active throws ConflictException")
    void testSessionOverlapRejected() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(focusSessionRepository.existsByUserIdAndStatus(user.getId(), FocusStatus.IN_PROGRESS)).thenReturn(true);

        FocusStartRequest request = new FocusStartRequest(null, FocusMode.POMODORO, 25);

        ConflictException ex = assertThrows(ConflictException.class, () ->
                focusService.startSession(user.getId(), request)
        );

        assertTrue(ex.getMessage().contains("already in progress"));
        verify(focusSessionRepository, never()).save(any(FocusSession.class));
    }

    @Test
    @DisplayName("Ending session calculates actualMinutes from server timestamps")
    void testEndSessionComputesServerDuration() {
        FocusSession session = new FocusSession(user, null, FocusMode.POMODORO, 25);
        session.setId(101L);
        session.setStartedAt(Instant.now().minus(25, ChronoUnit.MINUTES));
        session.setStatus(FocusStatus.IN_PROGRESS);

        when(focusSessionRepository.findById(101L)).thenReturn(Optional.of(session));
        when(focusSessionRepository.save(any(FocusSession.class))).thenAnswer(inv -> inv.getArgument(0));

        FocusSessionResponse response = focusService.endSession(user.getId(), 101L);

        assertNotNull(response);
        assertEquals(FocusStatus.COMPLETED, response.status());
        assertTrue(response.actualMinutes() >= 24 && response.actualMinutes() <= 26);
    }

    @Test
    @DisplayName("Streak calculation rule: Only days with >= 10 minutes count towards streak")
    void testStreakCalculationThreshold() {
        LocalDate today = LocalDate.of(2026, 10, 7);
        Map<LocalDate, Integer> dailyMinutes = new HashMap<>();

        // Day 1 (today): 25 mins -> counts
        dailyMinutes.put(today, 25);
        // Day 2 (yesterday): 15 mins -> counts
        dailyMinutes.put(today.minusDays(1), 15);
        // Day 3 (2 days ago): 5 mins -> DOES NOT COUNT (below 10 min threshold)
        dailyMinutes.put(today.minusDays(2), 5);
        // Day 4 (3 days ago): 40 mins -> broken streak, ignored
        dailyMinutes.put(today.minusDays(3), 40);

        int streak = FocusService.calculateStreak(dailyMinutes, today);
        assertEquals(2, streak, "Streak should terminate when encountering a day with < 10 minutes");
    }

    @Test
    @DisplayName("Streak keeps active if student studied yesterday but hasn't finished 10 mins today yet")
    void testStreakGracePeriodToday() {
        LocalDate today = LocalDate.of(2026, 10, 7);
        Map<LocalDate, Integer> dailyMinutes = new HashMap<>();

        // Day 1 (today): 0 mins yet
        dailyMinutes.put(today, 0);
        // Day 2 (yesterday): 30 mins -> counts
        dailyMinutes.put(today.minusDays(1), 30);
        // Day 3 (2 days ago): 45 mins -> counts
        dailyMinutes.put(today.minusDays(2), 45);

        int streak = FocusService.calculateStreak(dailyMinutes, today);
        assertEquals(2, streak, "Streak remains alive based on yesterday's completion");
    }
}

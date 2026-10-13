package com.studyhub.platform.modules.focus.service;

import com.studyhub.platform.common.enums.FocusMode;
import com.studyhub.platform.common.enums.FocusStatus;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ConflictException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.focus.dto.FocusLeaderboardEntry;
import com.studyhub.platform.modules.focus.dto.FocusSessionResponse;
import com.studyhub.platform.modules.focus.dto.FocusStartRequest;
import com.studyhub.platform.modules.focus.dto.FocusStatsResponse;
import com.studyhub.platform.modules.focus.entity.FocusSession;
import com.studyhub.platform.modules.focus.repository.FocusSessionRepository;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FocusService {

    public static final int STREAK_DAILY_MINIMUM_MINUTES = 10;

    private final FocusSessionRepository focusSessionRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    public FocusService(FocusSessionRepository focusSessionRepository,
                        UserRepository userRepository,
                        SubjectRepository subjectRepository) {
        this.focusSessionRepository = focusSessionRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    public FocusSessionResponse startSession(Long userId, FocusStartRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Validation: No overlapping active sessions
        if (focusSessionRepository.existsByUserIdAndStatus(userId, FocusStatus.IN_PROGRESS)) {
            throw new ConflictException("An active focus session is already in progress. Please end your ongoing session before starting a new one.");
        }

        Subject subject = null;
        if (request.subjectId() != null) {
            subject = subjectRepository.findById(request.subjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.subjectId()));
        }

        int planned = (request.plannedMinutes() != null && request.plannedMinutes() > 0)
                ? request.plannedMinutes()
                : (request.mode() == FocusMode.POMODORO ? 25 : 60);

        FocusSession session = new FocusSession(user, subject, request.mode(), planned);
        FocusSession saved = focusSessionRepository.save(session);

        return FocusSessionResponse.fromEntity(saved);
    }

    @Transactional
    public FocusSessionResponse endSession(Long userId, Long sessionId) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("FocusSession", "id", sessionId));

        if (!session.getUser().getId().equals(userId)) {
            throw new BadRequestException("You cannot modify another student's focus session");
        }

        if (session.getStatus() != FocusStatus.IN_PROGRESS) {
            return FocusSessionResponse.fromEntity(session);
        }

        Instant now = Instant.now();
        session.setEndedAt(now);

        // Calculate actual duration strictly from server timestamps
        long elapsedSeconds = Duration.between(session.getStartedAt(), now).getSeconds();
        int actualMinutes = (int) Math.max(0, elapsedSeconds / 60);

        if (actualMinutes < 1 && elapsedSeconds >= 30) {
            actualMinutes = 1; // Round up to 1 min if at least 30 seconds
        }

        session.setActualMinutes(actualMinutes);
        session.setStatus(actualMinutes > 0 ? FocusStatus.COMPLETED : FocusStatus.CANCELLED);

        FocusSession saved = focusSessionRepository.save(session);
        return FocusSessionResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public FocusStatsResponse getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        List<FocusSession> completedSessions = focusSessionRepository
                .findByUserIdAndStatusOrderByStartedAtDesc(userId, FocusStatus.COMPLETED);

        ZoneId zone = ZoneOffset.UTC;
        LocalDate today = LocalDate.now(zone);
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        int todayMinutes = 0;
        int thisWeekMinutes = 0;
        int totalMinutes = 0;
        Map<String, Integer> subjectBreakdown = new LinkedHashMap<>();

        // Group total minutes by date for streak calculation
        Map<LocalDate, Integer> dailyMinutesMap = new HashMap<>();

        for (FocusSession s : completedSessions) {
            int mins = s.getActualMinutes();
            totalMinutes += mins;

            LocalDate sessionDate = s.getStartedAt().atZone(zone).toLocalDate();
            dailyMinutesMap.put(sessionDate, dailyMinutesMap.getOrDefault(sessionDate, 0) + mins);

            if (sessionDate.equals(today)) {
                todayMinutes += mins;
            }
            if (!sessionDate.isBefore(startOfWeek)) {
                thisWeekMinutes += mins;
            }

            String subjectName = (s.getSubject() != null) ? s.getSubject().getName() : "General Studies";
            subjectBreakdown.put(subjectName, subjectBreakdown.getOrDefault(subjectName, 0) + mins);
        }

        int streak = calculateStreak(dailyMinutesMap, today);

        // Check for active session
        FocusSessionResponse activeSession = focusSessionRepository
                .findTopByUserIdAndStatusOrderByStartedAtDesc(userId, FocusStatus.IN_PROGRESS)
                .map(FocusSessionResponse::fromEntity)
                .orElse(null);

        return new FocusStatsResponse(
                todayMinutes,
                thisWeekMinutes,
                totalMinutes,
                streak,
                completedSessions.size(),
                subjectBreakdown,
                activeSession
        );
    }

    @Transactional(readOnly = true)
    public List<FocusLeaderboardEntry> getWeeklyLeaderboard(Long departmentId) {
        ZoneId zone = ZoneOffset.UTC;
        LocalDate startOfWeek = LocalDate.now(zone).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Instant startInstant = startOfWeek.atStartOfDay(zone).toInstant();

        List<FocusSession> sessions;
        if (departmentId != null) {
            sessions = focusSessionRepository.findCompletedSessionsByDepartmentSince(departmentId, startInstant);
        } else {
            sessions = focusSessionRepository.findCompletedSessionsSince(startInstant);
        }

        // Group by user
        Map<User, List<FocusSession>> userSessionMap = sessions.stream()
                .collect(Collectors.groupingBy(FocusSession::getUser));

        List<FocusLeaderboardEntry> entries = new ArrayList<>();
        LocalDate today = LocalDate.now(zone);

        for (Map.Entry<User, List<FocusSession>> entry : userSessionMap.entrySet()) {
            User u = entry.getKey();
            List<FocusSession> userSessions = entry.getValue();

            int weeklyTotal = userSessions.stream().mapToInt(FocusSession::getActualMinutes).sum();

            // Calculate user streak
            Map<LocalDate, Integer> dailyMins = new HashMap<>();
            for (FocusSession s : userSessions) {
                LocalDate d = s.getStartedAt().atZone(zone).toLocalDate();
                dailyMins.put(d, dailyMins.getOrDefault(d, 0) + s.getActualMinutes());
            }
            int streak = calculateStreak(dailyMins, today);

            String deptName = u.getDepartment() != null ? u.getDepartment().getName() : "General";

            entries.add(new FocusLeaderboardEntry(
                    0,
                    u.getId(),
                    u.getFullName(),
                    deptName,
                    weeklyTotal,
                    streak,
                    userSessions.size()
            ));
        }

        // Sort by total minutes desc
        entries.sort((a, b) -> Integer.compare(b.totalMinutes(), a.totalMinutes()));

        // Assign ranks
        List<FocusLeaderboardEntry> ranked = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            FocusLeaderboardEntry e = entries.get(i);
            ranked.add(new FocusLeaderboardEntry(
                    i + 1,
                    e.userId(),
                    e.fullName(),
                    e.departmentName(),
                    e.totalMinutes(),
                    e.streakDays(),
                    e.sessionsCount()
            ));
        }

        return ranked;
    }

    public static int calculateStreak(Map<LocalDate, Integer> dailyMinutesMap, LocalDate today) {
        int streak = 0;
        LocalDate current = today;

        // If today has reached 10 minutes, streak includes today
        if (dailyMinutesMap.getOrDefault(current, 0) >= STREAK_DAILY_MINIMUM_MINUTES) {
            streak++;
            current = current.minusDays(1);
        } else {
            // Check yesterday to keep existing streak alive if student hasn't studied today yet
            current = current.minusDays(1);
        }

        while (dailyMinutesMap.getOrDefault(current, 0) >= STREAK_DAILY_MINIMUM_MINUTES) {
            streak++;
            current = current.minusDays(1);
        }

        return streak;
    }
}

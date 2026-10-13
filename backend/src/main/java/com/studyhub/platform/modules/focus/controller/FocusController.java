package com.studyhub.platform.modules.focus.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.focus.dto.FocusLeaderboardEntry;
import com.studyhub.platform.modules.focus.dto.FocusSessionResponse;
import com.studyhub.platform.modules.focus.dto.FocusStartRequest;
import com.studyhub.platform.modules.focus.dto.FocusStatsResponse;
import com.studyhub.platform.modules.focus.service.FocusService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/focus")
public class FocusController {

    private final FocusService focusService;

    public FocusController(FocusService focusService) {
        this.focusService = focusService;
    }

    @PostMapping("/sessions/start")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> startSession(
            @AuthenticationPrincipal SecurityUserPrincipal currentUser,
            @Valid @RequestBody FocusStartRequest request) {

        FocusSessionResponse response = focusService.startSession(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(response, "Focus session started"));
    }

    @PostMapping("/sessions/{id}/end")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> endSession(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUserPrincipal currentUser) {

        FocusSessionResponse response = focusService.endSession(currentUser.getId(), id);
        return ResponseEntity.ok(ApiResponse.ok(response, "Focus session concluded"));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<ApiResponse<FocusStatsResponse>> getStats(
            @AuthenticationPrincipal SecurityUserPrincipal currentUser) {

        FocusStatsResponse response = focusService.getUserStats(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<ApiResponse<List<FocusLeaderboardEntry>>> getLeaderboard(
            @RequestParam(required = false) Long departmentId) {

        List<FocusLeaderboardEntry> leaderboard = focusService.getWeeklyLeaderboard(departmentId);
        return ResponseEntity.ok(ApiResponse.ok(leaderboard));
    }
}

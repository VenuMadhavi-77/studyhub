package com.studyhub.platform.modules.whiteboard.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardCreateRequest;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardDrawEvent;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardSessionDto;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardSnapshotDto;
import com.studyhub.platform.modules.whiteboard.dto.WhiteboardSnapshotRequest;
import com.studyhub.platform.modules.whiteboard.service.WhiteboardService;
import com.studyhub.platform.security.SecurityUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/whiteboard")
@Tag(name = "Collaborative Whiteboard", description = "Real-time collaborative study sessions and whiteboard state synchronization")
public class WhiteboardController {

    private final WhiteboardService whiteboardService;
    private final SimpMessagingTemplate messagingTemplate;

    public WhiteboardController(WhiteboardService whiteboardService, SimpMessagingTemplate messagingTemplate) {
        this.whiteboardService = whiteboardService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/sessions")
    @Operation(summary = "Create a new collaborative whiteboard session")
    public ResponseEntity<ApiResponse<WhiteboardSessionDto>> createSession(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @Valid @RequestBody WhiteboardCreateRequest request
    ) {
        WhiteboardSessionDto session = whiteboardService.createSession(principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(session, "Whiteboard session created successfully"));
    }

    @GetMapping("/sessions/{sessionCode}")
    @Operation(summary = "Get whiteboard session details by session code")
    public ResponseEntity<ApiResponse<WhiteboardSessionDto>> getSession(
            @PathVariable String sessionCode
    ) {
        WhiteboardSessionDto session = whiteboardService.getSession(sessionCode);
        return ResponseEntity.ok(ApiResponse.ok(session));
    }

    @PostMapping("/sessions/{sessionCode}/snapshots")
    @Operation(summary = "Save snapshot of the whiteboard canvas")
    public ResponseEntity<ApiResponse<WhiteboardSnapshotDto>> saveSnapshot(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String sessionCode,
            @Valid @RequestBody WhiteboardSnapshotRequest request
    ) {
        WhiteboardSnapshotDto snapshot = whiteboardService.saveSnapshot(sessionCode, principal.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(snapshot, "Whiteboard snapshot saved"));
    }

    @GetMapping("/sessions/{sessionCode}/snapshots/latest")
    @Operation(summary = "Get the latest whiteboard snapshot")
    public ResponseEntity<ApiResponse<WhiteboardSnapshotDto>> getLatestSnapshot(
            @PathVariable String sessionCode
    ) {
        Optional<WhiteboardSnapshotDto> snapshot = whiteboardService.getLatestSnapshot(sessionCode);
        return ResponseEntity.ok(ApiResponse.ok(snapshot.orElse(null)));
    }

    @PostMapping("/sessions/{sessionCode}/end")
    @Operation(summary = "End/close an active collaborative whiteboard session")
    public ResponseEntity<ApiResponse<WhiteboardSessionDto>> endSession(
            @AuthenticationPrincipal SecurityUserPrincipal principal,
            @PathVariable String sessionCode
    ) {
        WhiteboardSessionDto session = whiteboardService.endSession(sessionCode, principal.getId());
        
        // Notify all connected clients in this session that the host ended the room
        messagingTemplate.convertAndSend(
                "/topic/whiteboard." + sessionCode,
                new WhiteboardDrawEvent(
                        "SESSION_ENDED",
                        sessionCode,
                        principal.getId(),
                        principal.getFullName(),
                        0L,
                        java.util.Map.of("message", "The session has been ended by the host"),
                        System.currentTimeMillis()
                )
        );

        return ResponseEntity.ok(ApiResponse.ok(session, "Whiteboard session ended"));
    }

    // ==========================================
    // STOMP WebSocket Message Handlers
    // ==========================================

    @MessageMapping("/whiteboard.draw")
    public void broadcastDraw(@Payload WhiteboardDrawEvent event) {
        if (event != null && event.sessionCode() != null) {
            messagingTemplate.convertAndSend("/topic/whiteboard." + event.sessionCode(), event);
        }
    }

    @MessageMapping("/whiteboard.cursor")
    public void broadcastCursor(@Payload WhiteboardDrawEvent event) {
        if (event != null && event.sessionCode() != null) {
            messagingTemplate.convertAndSend("/topic/whiteboard." + event.sessionCode() + ".cursor", event);
        }
    }

    @MessageMapping("/whiteboard.join")
    public void broadcastJoin(@Payload WhiteboardDrawEvent event) {
        if (event != null && event.sessionCode() != null) {
            messagingTemplate.convertAndSend("/topic/whiteboard." + event.sessionCode(), event);
        }
    }
}

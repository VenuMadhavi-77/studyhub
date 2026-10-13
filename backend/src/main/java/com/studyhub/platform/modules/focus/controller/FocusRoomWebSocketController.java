package com.studyhub.platform.modules.focus.controller;

import com.studyhub.platform.modules.focus.dto.FocusRoomParticipantEvent;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class FocusRoomWebSocketController {

    @MessageMapping("/focus-room/{roomCode}/status")
    @SendTo("/topic/focus-room/{roomCode}")
    public FocusRoomParticipantEvent broadcastParticipantStatus(
            @DestinationVariable String roomCode,
            @Payload FocusRoomParticipantEvent event) {
        return event;
    }
}

package com.studyhub.platform.modules.notification.service;

import com.studyhub.platform.common.enums.NotificationType;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.notification.dto.NotificationResponse;
import com.studyhub.platform.modules.notification.entity.Notification;
import com.studyhub.platform.modules.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    private User recipient;

    @BeforeEach
    void setUp() {
        recipient = new User();
        recipient.setId(10L);
        recipient.setEmail("student@studyhub.edu");
    }

    @Test
    void sendNotification_ShouldSaveAndPushToStompUserQueue() {
        when(userRepository.findById(10L)).thenReturn(Optional.of(recipient));

        Notification notification = new Notification(recipient, "New Answer", "Someone answered your question", NotificationType.DOUBT_ANSWER, "/doubts/5");
        notification.setId(100L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        NotificationResponse response = notificationService.sendNotification(
                10L, "New Answer", "Someone answered your question", NotificationType.DOUBT_ANSWER, "/doubts/5"
        );

        assertNotNull(response);
        assertEquals("New Answer", response.title());
        assertEquals(10L, response.userId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(eq("student@studyhub.edu"), eq("/queue/notifications"), any(NotificationResponse.class));
    }
}

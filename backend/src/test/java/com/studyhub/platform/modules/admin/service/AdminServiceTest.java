package com.studyhub.platform.modules.admin.service;

import com.studyhub.platform.common.enums.NotificationType;
import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.modules.admin.dto.ResourceModerationRequest;
import com.studyhub.platform.modules.admin.repository.ModerationLogRepository;
import com.studyhub.platform.modules.admin.repository.ReportRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.doubt.repository.DoubtRepository;
import com.studyhub.platform.modules.notification.service.NotificationService;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.repository.TopicRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private DoubtRepository doubtRepository;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private ModerationLogRepository moderationLogRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AdminService adminService;

    private User admin;
    private User uploader;
    private Resource pendingResource;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setEmail("admin@studyhub.edu");
        admin.setRole(Role.ADMIN);

        uploader = new User();
        uploader.setId(2L);
        uploader.setEmail("uploader@studyhub.edu");
        uploader.setRole(Role.STUDENT);
        uploader.setStatus(UserStatus.ACTIVE);

        pendingResource = new Resource();
        pendingResource.setId(50L);
        pendingResource.setTitle("Pending Material");
        pendingResource.setStatus(ResourceStatus.PENDING);
        pendingResource.setUploader(uploader);
    }

    @Test
    void moderateResource_ShouldApproveAndSendNotification() {
        ResourceModerationRequest request = new ResourceModerationRequest(ResourceStatus.APPROVED, null);

        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        when(resourceRepository.findById(50L)).thenReturn(Optional.of(pendingResource));
        when(resourceRepository.save(any(Resource.class))).thenReturn(pendingResource);

        ResourceResponse response = adminService.moderateResource(50L, request, 1L);

        assertNotNull(response);
        assertEquals(ResourceStatus.APPROVED, pendingResource.getStatus());
        verify(moderationLogRepository, times(1)).save(any());
        verify(notificationService, times(1)).sendNotification(
                eq(2L), any(), any(), eq(NotificationType.RESOURCE_STATUS), any()
        );
    }
}

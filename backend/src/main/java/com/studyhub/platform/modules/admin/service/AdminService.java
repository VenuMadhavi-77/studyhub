package com.studyhub.platform.modules.admin.service;

import com.studyhub.platform.common.enums.*;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.admin.dto.*;
import com.studyhub.platform.modules.admin.entity.ModerationLog;
import com.studyhub.platform.modules.admin.entity.Report;
import com.studyhub.platform.modules.admin.repository.ModerationLogRepository;
import com.studyhub.platform.modules.admin.repository.ReportRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.doubt.repository.DoubtRepository;
import com.studyhub.platform.modules.notification.service.NotificationService;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.taxonomy.dto.SubjectDto;
import com.studyhub.platform.modules.taxonomy.dto.TopicDto;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import com.studyhub.platform.modules.taxonomy.entity.Topic;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.repository.TopicRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final DoubtRepository doubtRepository;
    private final ReportRepository reportRepository;
    private final ModerationLogRepository moderationLogRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final com.studyhub.platform.modules.taxonomy.repository.TagRepository tagRepository;
    private final NotificationService notificationService;

    public AdminService(UserRepository userRepository,
                        ResourceRepository resourceRepository,
                        DoubtRepository doubtRepository,
                        ReportRepository reportRepository,
                        ModerationLogRepository moderationLogRepository,
                        DepartmentRepository departmentRepository,
                        SubjectRepository subjectRepository,
                        TopicRepository topicRepository,
                        com.studyhub.platform.modules.taxonomy.repository.TagRepository tagRepository,
                        NotificationService notificationService) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.doubtRepository = doubtRepository;
        this.reportRepository = reportRepository;
        this.moderationLogRepository = moderationLogRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public AdminStatsResponse getPlatformStats() {
        long totalUsers = userRepository.count();
        long totalResources = resourceRepository.count();
        long pendingResources = resourceRepository.countByStatus(ResourceStatus.PENDING);
        long totalDoubts = doubtRepository.count();
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);

        return new AdminStatsResponse(
                totalUsers,
                totalResources,
                pendingResources,
                totalDoubts,
                totalReports,
                pendingReports,
                0,
                0
        );
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> getPendingResources(Pageable pageable) {
        return resourceRepository.findByStatusOrderByCreatedAtDesc(ResourceStatus.PENDING, pageable)
                .map(ResourceResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.studyhub.platform.modules.auth.dto.UserProfileResponse> getUsers(String search, Pageable pageable) {
        org.springframework.data.domain.Page<User> usersPage;
        if (search != null && !search.isBlank()) {
            usersPage = userRepository.findAllBySearch(search.trim(), pageable);
        } else {
            usersPage = userRepository.findAllByDeletedFalse(pageable);
        }
        return usersPage.map(u -> new com.studyhub.platform.modules.auth.dto.UserProfileResponse(
                u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus(),
                u.getDepartment() != null ? u.getDepartment().getId() : null,
                u.getDepartment() != null ? u.getDepartment().getName() : null,
                u.getSemester(), u.getGoals(), u.getPreferredLanguage(), u.getLevel(), u.getCreatedAt()
        ));
    }

    @Transactional
    public ResourceResponse moderateResource(Long resourceId, ResourceModerationRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        resource.setStatus(request.status());
        resource.setRejectionReason(request.rejectionReason());
        Resource updated = resourceRepository.save(resource);

        // Audit Log
        ModerationAction action = request.status() == ResourceStatus.APPROVED
                ? ModerationAction.APPROVE_RESOURCE
                : ModerationAction.REJECT_RESOURCE;

        ModerationLog logEntry = new ModerationLog(admin, ReportTargetType.RESOURCE, resourceId, action, request.rejectionReason());
        moderationLogRepository.save(logEntry);

        // Send In-App & Real-Time Notification to Uploader
        String notifMsg = request.status() == ResourceStatus.APPROVED
                ? "Your resource '" + resource.getTitle() + "' has been approved and published!"
                : "Your resource '" + resource.getTitle() + "' was rejected. Reason: " + request.rejectionReason();

        notificationService.sendNotification(
                resource.getUploader().getId(),
                "Resource Moderation Update",
                notifMsg,
                NotificationType.RESOURCE_STATUS,
                "/resources/" + resource.getId()
        );

        return ResourceResponse.fromEntity(updated);
    }

    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(ReportStatus status, Pageable pageable) {
        if (status != null) {
            return reportRepository.findByStatusOrderByCreatedAtDesc(status, pageable).map(ReportResponse::fromEntity);
        }
        return reportRepository.findAllByOrderByCreatedAtDesc(pageable).map(ReportResponse::fromEntity);
    }

    @Transactional
    public ReportResponse resolveReport(Long reportId, ReportResolutionRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "id", reportId));

        report.setStatus(request.status());
        report.setResolvedByAdmin(admin);
        report.setResolvedAt(Instant.now());
        Report updated = reportRepository.save(report);

        ModerationAction action = request.status() == ReportStatus.RESOLVED
                ? ModerationAction.RESOLVE_REPORT
                : ModerationAction.DISMISS_REPORT;

        ModerationLog logEntry = new ModerationLog(admin, report.getTargetType(), report.getTargetId(), action, request.resolutionNotes());
        moderationLogRepository.save(logEntry);

        return ReportResponse.fromEntity(updated);
    }

    @Transactional
    public void updateUserStatus(Long targetUserId, UserStatusUpdateRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", targetUserId));

        if (targetUser.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot alter status of another administrator account");
        }

        targetUser.setStatus(request.status());
        userRepository.save(targetUser);

        ModerationAction action = request.status() == UserStatus.SUSPENDED
                ? ModerationAction.SUSPEND_USER
                : ModerationAction.UNSUSPEND_USER;

        ModerationLog logEntry = new ModerationLog(admin, ReportTargetType.USER, targetUserId, action, request.reason());
        moderationLogRepository.save(logEntry);
    }

    @Transactional
    public SubjectDto createSubject(TaxonomyCreateSubjectRequest request) {
        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));

        Subject subject = new Subject();
        subject.setDepartment(dept);
        subject.setName(request.name().trim());
        subject.setCode(request.code().trim().toUpperCase());
        subject.setSemester(request.semester());
        subject.setActive(true);

        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), dept.getId(), dept.getName(), saved.getName(), saved.getCode(), saved.getSemester(), saved.isActive());
    }

    @Transactional
    public TopicDto createTopic(TaxonomyCreateTopicRequest request) {
        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.subjectId()));

        Topic topic = new Topic(subject, request.name().trim());
        Topic saved = topicRepository.save(topic);
        return new TopicDto(saved.getId(), subject.getId(), subject.getName(), saved.getName(), saved.isActive());
    }

    @Transactional
    public ResourceResponse createExternalResource(com.studyhub.platform.modules.resource.dto.ExternalResourceRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.subjectId()));

        Topic topic = null;
        if (request.topicId() != null) {
            topic = topicRepository.findById(request.topicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.topicId()));
        }

        Resource resource = new Resource();
        resource.setTitle(request.title().trim());
        resource.setDescription(request.description() != null ? request.description().trim() : null);
        resource.setType(request.type());
        resource.setSourcePlatform(request.sourcePlatform());
        resource.setLevel(request.level());
        resource.setLanguage(request.language());
        resource.setFree(request.isFree() != null ? request.isFree() : true);
        resource.setBestFor(request.bestFor() != null ? request.bestFor().trim() : null);
        resource.setExternalUrl(request.externalUrl().trim());
        resource.setDepartment(dept);
        resource.setSubject(subject);
        resource.setTopic(topic);
        resource.setSemester(request.semester());
        resource.setUploader(admin);
        resource.setStatus(ResourceStatus.APPROVED);

        boolean isVerified = request.verified() != null ? request.verified() : true;
        resource.setVerified(isVerified);
        if (isVerified) {
            resource.setVerifiedBy(admin.getFullName());
            resource.setVerifiedAt(Instant.now());
        }
        resource.setReliabilityScore(isVerified ? 3.8 : 0.0);
        resource.setReliabilityLabel("New - not enough reviews");

        // Process tags
        if (request.tags() != null && !request.tags().isEmpty()) {
            java.util.Set<Tag> tags = new java.util.HashSet<>();
            for (String tagName : request.tags()) {
                String cleanName = tagName.trim().toLowerCase();
                if (!cleanName.isBlank()) {
                    Tag tag = tagRepository.findByNameIgnoreCase(cleanName)
                            .orElseGet(() -> tagRepository.save(new Tag(cleanName)));
                    tags.add(tag);
                }
            }
            resource.setTags(tags);
        }

        Resource saved = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(saved);
    }

    @Transactional
    public ResourceResponse updateExternalResource(Long id, com.studyhub.platform.modules.resource.dto.ExternalResourceRequest request, Long adminId) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminId));

        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        Department dept = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.departmentId()));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.subjectId()));

        Topic topic = null;
        if (request.topicId() != null) {
            topic = topicRepository.findById(request.topicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.topicId()));
        }

        resource.setTitle(request.title().trim());
        resource.setDescription(request.description() != null ? request.description().trim() : null);
        resource.setType(request.type());
        resource.setSourcePlatform(request.sourcePlatform());
        resource.setLevel(request.level());
        resource.setLanguage(request.language());
        resource.setFree(request.isFree() != null ? request.isFree() : true);
        resource.setBestFor(request.bestFor() != null ? request.bestFor().trim() : null);
        resource.setExternalUrl(request.externalUrl().trim());
        resource.setDepartment(dept);
        resource.setSubject(subject);
        resource.setTopic(topic);
        resource.setSemester(request.semester());

        if (request.verified() != null) {
            resource.setVerified(request.verified());
            if (request.verified()) {
                resource.setVerifiedBy(admin.getFullName());
                resource.setVerifiedAt(Instant.now());
            } else {
                resource.setVerifiedBy(null);
                resource.setVerifiedAt(null);
            }
        }

        // Process tags
        if (request.tags() != null) {
            java.util.Set<Tag> tags = new java.util.HashSet<>();
            for (String tagName : request.tags()) {
                String cleanName = tagName.trim().toLowerCase();
                if (!cleanName.isBlank()) {
                    Tag tag = tagRepository.findByNameIgnoreCase(cleanName)
                            .orElseGet(() -> tagRepository.save(new Tag(cleanName)));
                    tags.add(tag);
                }
            }
            resource.setTags(tags);
        }

        Resource updated = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(updated);
    }
}

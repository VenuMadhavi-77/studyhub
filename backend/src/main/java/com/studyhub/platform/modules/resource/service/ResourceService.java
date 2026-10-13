package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceDetailResponse;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.dto.ResourceUpdateRequest;
import com.studyhub.platform.modules.resource.dto.ResourceUploadRequest;
import com.studyhub.platform.modules.resource.entity.Rating;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.BookmarkRepository;
import com.studyhub.platform.modules.resource.repository.RatingRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.UsefulVoteRepository;
import com.studyhub.platform.modules.storage.StorageService;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import com.studyhub.platform.modules.taxonomy.entity.Topic;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.repository.TopicRepository;
import com.studyhub.platform.modules.taxonomy.service.TaxonomyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class ResourceService {

    private static final Logger log = LoggerFactory.getLogger(ResourceService.class);

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final TaxonomyService taxonomyService;
    private final StorageService storageService;
    private final BookmarkRepository bookmarkRepository;
    private final UsefulVoteRepository usefulVoteRepository;
    private final RatingRepository ratingRepository;
    private final CounterDeDuplicationService counterService;

    public ResourceService(ResourceRepository resourceRepository,
                           UserRepository userRepository,
                           DepartmentRepository departmentRepository,
                           SubjectRepository subjectRepository,
                           TopicRepository topicRepository,
                           TaxonomyService taxonomyService,
                           StorageService storageService,
                           BookmarkRepository bookmarkRepository,
                           UsefulVoteRepository usefulVoteRepository,
                           RatingRepository ratingRepository,
                           CounterDeDuplicationService counterService) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.taxonomyService = taxonomyService;
        this.storageService = storageService;
        this.bookmarkRepository = bookmarkRepository;
        this.usefulVoteRepository = usefulVoteRepository;
        this.ratingRepository = ratingRepository;
        this.counterService = counterService;
    }

    @Transactional
    public ResourceResponse uploadResource(ResourceUploadRequest request, MultipartFile file, Long uploaderId) {
        User uploader = userRepository.findById(uploaderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", uploaderId));

        Department department = departmentRepository.findById(request.departmentId())
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
        resource.setDescription(request.description());
        resource.setType(request.type());
        resource.setDepartment(department);
        resource.setSubject(subject);
        resource.setTopic(topic);
        resource.setSemester(request.semester());
        resource.setUploader(uploader);
        resource.setStatus(ResourceStatus.APPROVED); // Default auto-approve per college platform design

        // File or URL handling
        if (request.type() == ResourceType.YOUTUBE_LINK || request.type() == ResourceType.WEBSITE_URL) {
            if (request.externalUrl() == null || request.externalUrl().isBlank()) {
                throw new BadRequestException("External URL is required for resource type " + request.type());
            }
            validateExternalUrl(request.externalUrl());
            resource.setExternalUrl(request.externalUrl().trim());
        } else {
            if (file == null || file.isEmpty()) {
                throw new BadRequestException("A document file is required for resource type " + request.type());
            }
            StorageService.StoredFileMetadata fileMeta = storageService.store(file, "resources");
            resource.setFilePath(fileMeta.storagePath());
            resource.setFileMimeType(fileMeta.mimeType());
            resource.setFileSizeBytes(fileMeta.sizeBytes());
        }

        // Tags linkage
        Set<Tag> tags = taxonomyService.findOrCreateTags(request.tags());
        resource.setTags(tags);

        Resource saved = resourceRepository.save(resource);
        log.info("Resource uploaded successfully with ID: {} by user: {}", saved.getId(), uploader.getEmail());
        return ResourceResponse.fromEntity(saved);
    }

    @Transactional
    public ResourceResponse updateResource(Long id, ResourceUpdateRequest request, Long requesterId, boolean isAdmin) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        if (!resource.getUploader().getId().equals(requesterId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to edit this resource");
        }

        resource.setTitle(request.title().trim());
        resource.setDescription(request.description());
        resource.setType(request.type());

        if (request.topicId() != null) {
            Topic topic = topicRepository.findById(request.topicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Topic", "id", request.topicId()));
            resource.setTopic(topic);
        } else {
            resource.setTopic(null);
        }

        if (request.semester() != null) {
            resource.setSemester(request.semester());
        }

        if (request.externalUrl() != null) {
            validateExternalUrl(request.externalUrl());
            resource.setExternalUrl(request.externalUrl().trim());
        }

        if (request.tags() != null) {
            Set<Tag> tags = taxonomyService.findOrCreateTags(request.tags());
            resource.setTags(tags);
        }

        Resource updated = resourceRepository.save(resource);
        return ResourceResponse.fromEntity(updated);
    }

    @Transactional
    public void deleteResource(Long id, Long requesterId, boolean isAdmin) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        if (!resource.getUploader().getId().equals(requesterId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this resource");
        }

        // Soft delete
        resource.setDeleted(true);
        resourceRepository.save(resource);
        log.info("Soft-deleted resource ID: {} by requester: {}", id, requesterId);
    }

    @Transactional(readOnly = true)
    public ResourceDetailResponse getResourceDetail(Long id, Long currentUserId, String clientIp) {
        Resource resource = resourceRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        // Record view asynchronously with de-duplication
        counterService.recordView(id, currentUserId, clientIp);

        boolean bookmarked = false;
        boolean usefulVoted = false;
        Integer userRatingScore = null;

        if (currentUserId != null) {
            bookmarked = bookmarkRepository.existsByUserIdAndResourceId(currentUserId, id);
            usefulVoted = usefulVoteRepository.existsByUserIdAndResourceId(currentUserId, id);
            userRatingScore = ratingRepository.findByUserIdAndResourceId(currentUserId, id)
                    .map(Rating::getScore)
                    .orElse(null);
        }

        return ResourceDetailResponse.fromEntity(resource, bookmarked, usefulVoted, userRatingScore);
    }

    @Transactional
    public ResourceDownloadResult getResourceForDownload(Long id, Long currentUserId) {
        Resource resource = resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", id));

        if (resource.getFilePath() == null || resource.getFilePath().isBlank()) {
            throw new BadRequestException("This resource does not have an attached downloadable file");
        }

        counterService.recordDownload(id);
        org.springframework.core.io.Resource fileResource = storageService.loadAsResource(resource.getFilePath());

        return new ResourceDownloadResult(fileResource, resource.getTitle(), resource.getFileMimeType());
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> getMyResources(Long uploaderId, Pageable pageable) {
        return resourceRepository.findByUploaderIdOrderByCreatedAtDesc(uploaderId, pageable)
                .map(ResourceResponse::fromEntity);
    }

    @Transactional
    public void recordExternalView(Long resourceId, Long currentUserId, String clientIp) {
        resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));
        counterService.recordView(resourceId, currentUserId, clientIp);
    }

    public static void validateExternalUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BadRequestException("External URL cannot be blank");
        }
        String lower = url.trim().toLowerCase();
        if (lower.startsWith("javascript:") || lower.startsWith("data:") || lower.startsWith("vbscript:")) {
            throw new BadRequestException("Invalid URL protocol: Script and data URLs are strictly rejected");
        }
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) {
            throw new BadRequestException("External URL must begin with http:// or https://");
        }
    }

    public record ResourceDownloadResult(
            org.springframework.core.io.Resource resource,
            String resourceTitle,
            String mimeType
    ) {}
}

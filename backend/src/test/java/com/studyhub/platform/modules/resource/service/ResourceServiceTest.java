package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.common.enums.UserStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.dto.ResourceUploadRequest;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.BookmarkRepository;
import com.studyhub.platform.modules.resource.repository.RatingRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.UsefulVoteRepository;
import com.studyhub.platform.modules.storage.StorageService;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.repository.TopicRepository;
import com.studyhub.platform.modules.taxonomy.service.TaxonomyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TopicRepository topicRepository;

    @Mock
    private TaxonomyService taxonomyService;

    @Mock
    private StorageService storageService;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private UsefulVoteRepository usefulVoteRepository;

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private CounterDeDuplicationService counterService;

    @InjectMocks
    private ResourceService resourceService;

    private User uploader;
    private Department department;
    private Subject subject;

    @BeforeEach
    void setUp() {
        uploader = new User();
        uploader.setId(10L);
        uploader.setEmail("uploader@test.edu");
        uploader.setFullName("Uploader User");
        uploader.setRole(Role.STUDENT);
        uploader.setStatus(UserStatus.ACTIVE);

        department = new Department("Computer Science", "CSE");
        department.setId(1L);

        subject = new Subject();
        subject.setId(2L);
        subject.setName("DBMS");
        subject.setCode("CS401");
        subject.setDepartment(department);
        subject.setSemester(4);
    }

    @Test
    void uploadResource_ShouldSaveAndReturnResponse_WhenValidDocument() {
        ResourceUploadRequest request = new ResourceUploadRequest(
                "DBMS Lecture Notes",
                "Normalization and Transactions",
                ResourceType.PDF,
                1L,
                2L,
                null,
                4,
                null,
                Set.of("dbms", "sql")
        );

        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.pdf", "application/pdf", "%PDF-1.4 dummy content".getBytes()
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(uploader));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(department));
        when(subjectRepository.findById(2L)).thenReturn(Optional.of(subject));
        when(storageService.store(any(), any())).thenReturn(
                new StorageService.StoredFileMetadata("uuid.pdf", "notes.pdf", "application/pdf", 1024L, "2026/10/uuid.pdf")
        );
        when(taxonomyService.findOrCreateTags(any())).thenReturn(Set.of());

        Resource savedResource = new Resource();
        savedResource.setId(100L);
        savedResource.setTitle("DBMS Lecture Notes");
        savedResource.setType(ResourceType.PDF);
        savedResource.setDepartment(department);
        savedResource.setSubject(subject);
        savedResource.setSemester(4);
        savedResource.setUploader(uploader);

        when(resourceRepository.save(any(Resource.class))).thenReturn(savedResource);

        ResourceResponse response = resourceService.uploadResource(request, file, 10L);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("DBMS Lecture Notes", response.title());
        verify(storageService, times(1)).store(file, "resources");
        verify(resourceRepository, times(1)).save(any(Resource.class));
    }

    @Test
    void deleteResource_ShouldThrowUnauthorized_WhenNonOwnerAndNonAdmin() {
        Resource resource = new Resource();
        resource.setId(100L);
        resource.setUploader(uploader); // Owner is 10L

        when(resourceRepository.findById(100L)).thenReturn(Optional.of(resource));

        // Requester is 99L (not owner, not admin)
        assertThrows(UnauthorizedException.class, () -> resourceService.deleteResource(100L, 99L, false));
        verify(resourceRepository, never()).save(any());
    }
}

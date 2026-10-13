package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ConflictException;
import com.studyhub.platform.modules.admin.repository.ReportRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ReviewCreateRequest;
import com.studyhub.platform.modules.resource.dto.ReviewResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.Review;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.ReviewHelpfulVoteRepository;
import com.studyhub.platform.modules.resource.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewHelpfulVoteRepository helpfulVoteRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User student;
    private User uploader;
    private Resource resource;

    @BeforeEach
    void setUp() {
        student = new User();
        student.setId(10L);
        student.setFullName("Student One");

        uploader = new User();
        uploader.setId(20L);
        uploader.setFullName("Uploader User");

        resource = new Resource();
        resource.setId(100L);
        resource.setTitle("DSA Master Notes");
        resource.setUploader(uploader);
    }

    @Test
    @DisplayName("Self-review rejection: Uploader cannot review their own uploaded resource")
    void testSelfReviewRejected() {
        when(userRepository.findById(uploader.getId())).thenReturn(Optional.of(uploader));
        when(resourceRepository.findById(resource.getId())).thenReturn(Optional.of(resource));

        ReviewCreateRequest request = new ReviewCreateRequest(5, "This is an outstanding note with great depth and accuracy.");

        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                reviewService.createReview(uploader.getId(), resource.getId(), request)
        );

        assertTrue(ex.getMessage().contains("cannot review your own"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Duplicate review rejection: Student cannot review the same resource twice")
    void testDuplicateReviewRejected() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(resourceRepository.findById(resource.getId())).thenReturn(Optional.of(resource));
        when(reviewRepository.existsByResourceIdAndUserId(resource.getId(), student.getId())).thenReturn(true);

        ReviewCreateRequest request = new ReviewCreateRequest(5, "This is an outstanding note with great depth and accuracy.");

        ConflictException ex = assertThrows(ConflictException.class, () ->
                reviewService.createReview(student.getId(), resource.getId(), request)
        );

        assertTrue(ex.getMessage().contains("already submitted a review"));
        verify(reviewRepository, never()).save(any(Review.class));
    }

    @Test
    @DisplayName("Valid review creation succeeds and updates resource metrics")
    void testValidReviewCreation() {
        when(userRepository.findById(student.getId())).thenReturn(Optional.of(student));
        when(resourceRepository.findById(resource.getId())).thenReturn(Optional.of(resource));
        when(reviewRepository.existsByResourceIdAndUserId(resource.getId(), student.getId())).thenReturn(false);

        Review mockSavedReview = new Review(resource, student, 5, "This is an outstanding note with great depth and accuracy.");
        mockSavedReview.setId(1L);

        when(reviewRepository.save(any(Review.class))).thenReturn(mockSavedReview);
        when(reviewRepository.countByResourceId(resource.getId())).thenReturn(1L);
        when(reviewRepository.calculateAverageRating(resource.getId())).thenReturn(5.0);

        ReviewCreateRequest request = new ReviewCreateRequest(5, "This is an outstanding note with great depth and accuracy.");
        ReviewResponse response = reviewService.createReview(student.getId(), resource.getId(), request);

        assertNotNull(response);
        assertEquals(5, response.rating());
        assertEquals("This is an outstanding note with great depth and accuracy.", response.text());
        verify(resourceRepository, times(1)).save(resource);
    }
}

package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.RatingRequest;
import com.studyhub.platform.modules.resource.dto.RatingResponse;
import com.studyhub.platform.modules.resource.entity.Rating;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.RatingRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
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
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    private User uploader;
    private User reviewer;
    private Resource resource;

    @BeforeEach
    void setUp() {
        uploader = new User();
        uploader.setId(1L);

        reviewer = new User();
        reviewer.setId(2L);

        resource = new Resource();
        resource.setId(10L);
        resource.setUploader(uploader);
    }

    @Test
    void rateResource_ShouldThrowBadRequest_WhenUserAttemptsToRateOwnResource() {
        RatingRequest request = new RatingRequest(5, "Excellent work");
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));

        // uploader (ID 1) tries to rate own resource
        assertThrows(BadRequestException.class, () -> ratingService.rateResource(10L, request, 1L));
        verify(ratingRepository, never()).save(any());
    }

    @Test
    void rateResource_ShouldSaveAndRecalculate_WhenReviewerRates() {
        RatingRequest request = new RatingRequest(4, "Good content");
        when(resourceRepository.findById(10L)).thenReturn(Optional.of(resource));
        when(userRepository.findById(2L)).thenReturn(Optional.of(reviewer));
        when(ratingRepository.findByUserIdAndResourceId(2L, 10L)).thenReturn(Optional.empty());

        Rating savedRating = new Rating(reviewer, resource, 4, "Good content");
        savedRating.setId(50L);
        when(ratingRepository.save(any(Rating.class))).thenReturn(savedRating);
        when(ratingRepository.calculateAverageRating(10L)).thenReturn(4.0);
        when(ratingRepository.countRatingsByResourceId(10L)).thenReturn(1);

        RatingResponse response = ratingService.rateResource(10L, request, 2L);

        assertNotNull(response);
        assertEquals(4, response.score());
        assertEquals(4.0, response.resourceAvgRating());
        assertEquals(1, response.resourceRatingCount());
        verify(resourceRepository, times(1)).save(resource);
    }
}

package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.RatingRequest;
import com.studyhub.platform.modules.resource.dto.RatingResponse;
import com.studyhub.platform.modules.resource.entity.Rating;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.RatingRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class RatingService {

    private final RatingRepository ratingRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public RatingService(RatingRepository ratingRepository,
                         ResourceRepository resourceRepository,
                         UserRepository userRepository) {
        this.ratingRepository = ratingRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public RatingResponse rateResource(Long resourceId, RatingRequest request, Long userId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        // Anti-abuse: Prevent self-rating
        if (resource.getUploader().getId().equals(userId)) {
            throw new BadRequestException("You cannot rate your own uploaded resource");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<Rating> existingOpt = ratingRepository.findByUserIdAndResourceId(userId, resourceId);
        Rating rating;

        if (existingOpt.isPresent()) {
            rating = existingOpt.get();
            rating.setScore(request.score());
            rating.setComment(request.comment());
        } else {
            rating = new Rating(user, resource, request.score(), request.comment());
        }

        Rating savedRating = ratingRepository.save(rating);

        // Recalculate average rating and count
        Double avgRating = ratingRepository.calculateAverageRating(resourceId);
        Integer ratingCount = ratingRepository.countRatingsByResourceId(resourceId);

        resource.setAvgRating(avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);
        resource.setRatingCount(ratingCount != null ? ratingCount : 0);
        resourceRepository.save(resource);

        return new RatingResponse(
                savedRating.getId(),
                resource.getId(),
                user.getId(),
                savedRating.getScore(),
                savedRating.getComment(),
                resource.getAvgRating(),
                resource.getRatingCount(),
                savedRating.getCreatedAt()
        );
    }
}

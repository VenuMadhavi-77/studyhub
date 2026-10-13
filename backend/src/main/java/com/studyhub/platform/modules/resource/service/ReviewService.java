package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.enums.ReportStatus;
import com.studyhub.platform.common.enums.ReportTargetType;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ConflictException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.admin.repository.ReportRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ReviewCreateRequest;
import com.studyhub.platform.modules.resource.dto.ReviewResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.Review;
import com.studyhub.platform.modules.resource.entity.ReviewHelpfulVote;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.ReviewHelpfulVoteRepository;
import com.studyhub.platform.modules.resource.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    public static final double BAYESIAN_M = 5.0; // Minimum review weight
    public static final double BAYESIAN_C = 3.5; // Prior mean rating
    public static final double VERIFIED_BONUS = 0.3;
    public static final double REPORT_PENALTY_PER_OPEN = 0.5;

    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulVoteRepository helpfulVoteRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewHelpfulVoteRepository helpfulVoteRepository,
                         ResourceRepository resourceRepository,
                         UserRepository userRepository,
                         ReportRepository reportRepository) {
        this.reviewRepository = reviewRepository;
        this.helpfulVoteRepository = helpfulVoteRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional
    public ReviewResponse createReview(Long userId, Long resourceId, ReviewCreateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        // 1. Validate no self-review
        if (resource.getUploader() != null && resource.getUploader().getId().equals(userId)) {
            throw new BadRequestException("You cannot review your own uploaded resource");
        }

        // 2. Validate one review per user per resource
        if (reviewRepository.existsByResourceIdAndUserId(resourceId, userId)) {
            throw new ConflictException("You have already submitted a review for this resource");
        }

        Review review = new Review(resource, user, request.rating(), request.text().trim());
        Review saved = reviewRepository.save(review);

        // 3. Recalculate resource metrics and reliability
        updateResourceReliability(resource);

        return ReviewResponse.fromEntity(saved, false);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForResource(Long resourceId, Long currentUserId) {
        List<Review> reviews = reviewRepository.findByResourceIdOrderByHelpfulCountDescCreatedAtDesc(resourceId);

        Set<Long> userVotedReviewIds = Collections.emptySet();
        if (currentUserId != null) {
            userVotedReviewIds = helpfulVoteRepository.findByUserId(currentUserId).stream()
                    .map(v -> v.getReview().getId())
                    .collect(Collectors.toSet());
        }

        final Set<Long> votedSet = userVotedReviewIds;
        return reviews.stream()
                .map(r -> ReviewResponse.fromEntity(r, votedSet.contains(r.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean toggleHelpfulVote(Long userId, Long reviewId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        Optional<ReviewHelpfulVote> existing = helpfulVoteRepository.findByReviewIdAndUserId(reviewId, userId);
        boolean addedVote;
        if (existing.isPresent()) {
            helpfulVoteRepository.delete(existing.get());
            review.setHelpfulCount(Math.max(0, review.getHelpfulCount() - 1));
            addedVote = false;
        } else {
            helpfulVoteRepository.save(new ReviewHelpfulVote(review, user));
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            addedVote = true;
        }

        reviewRepository.save(review);
        return addedVote;
    }

    @Transactional
    public void updateResourceReliability(Resource resource) {
        long reviewCount = reviewRepository.countByResourceId(resource.getId());
        Double avgRating = reviewRepository.calculateAverageRating(resource.getId());
        if (avgRating == null) {
            avgRating = 0.0;
        }

        long openReportsCount = reportRepository.countByTargetTypeAndTargetIdAndStatus(
                ReportTargetType.RESOURCE, resource.getId(), ReportStatus.PENDING
        );

        double score = computeReliabilityScore(reviewCount, avgRating, resource.isVerified(), openReportsCount);
        String label = computeReliabilityLabel(reviewCount, score);

        // Update resource aggregate ratings
        resource.setAvgRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP).doubleValue());
        resource.setRatingCount((int) reviewCount);
        resource.setReliabilityScore(BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP).doubleValue());
        resource.setReliabilityLabel(label);

        resourceRepository.save(resource);
    }

    public static double computeReliabilityScore(long reviewCount, Double avgRating, boolean isVerified, long openReportsCount) {
        if (reviewCount == 0) {
            double base = isVerified ? 3.5 + VERIFIED_BONUS - (openReportsCount * REPORT_PENALTY_PER_OPEN) : 0.0;
            return Math.max(0.0, Math.min(5.0, base));
        }

        double r = (avgRating != null) ? avgRating : 0.0;
        double v = (double) reviewCount;

        // Bayesian formula: (v * r + m * C) / (v + m)
        double bayesianAvg = (v * r + BAYESIAN_M * BAYESIAN_C) / (v + BAYESIAN_M);

        double bonus = isVerified ? VERIFIED_BONUS : 0.0;
        double penalty = openReportsCount * REPORT_PENALTY_PER_OPEN;

        double finalScore = bayesianAvg + bonus - penalty;
        return Math.max(1.0, Math.min(5.0, finalScore));
    }

    public static String computeReliabilityLabel(long reviewCount, double reliabilityScore) {
        if (reviewCount < 3) {
            return "New - not enough reviews";
        }
        if (reliabilityScore >= 4.2) {
            return "Highly reliable";
        }
        if (reliabilityScore >= 3.5) {
            return "Good";
        }
        return "Mixed";
    }
}

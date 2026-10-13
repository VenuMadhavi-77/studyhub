package com.studyhub.platform.modules.resource.dto;

import com.studyhub.platform.common.enums.Role;
import com.studyhub.platform.modules.resource.entity.Review;

import java.time.Instant;

public record ReviewResponse(
        Long id,
        Long resourceId,
        Long userId,
        String userName,
        Role userRole,
        Integer rating,
        String text,
        Integer helpfulCount,
        boolean votedHelpfulByCurrentUser,
        Instant createdAt
) {
    public static ReviewResponse fromEntity(Review review, boolean votedHelpful) {
        return new ReviewResponse(
                review.getId(),
                review.getResource().getId(),
                review.getUser().getId(),
                review.getUser().getFullName(),
                review.getUser().getRole(),
                review.getRating(),
                review.getText(),
                review.getHelpfulCount(),
                votedHelpful,
                review.getCreatedAt()
        );
    }
}

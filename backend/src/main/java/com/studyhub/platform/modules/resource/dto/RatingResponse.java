package com.studyhub.platform.modules.resource.dto;

import java.time.Instant;

public record RatingResponse(
        Long id,
        Long resourceId,
        Long userId,
        Integer score,
        String comment,
        Double resourceAvgRating,
        Integer resourceRatingCount,
        Instant createdAt
) {}

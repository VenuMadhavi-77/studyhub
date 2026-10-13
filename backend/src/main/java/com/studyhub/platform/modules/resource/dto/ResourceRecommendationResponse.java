package com.studyhub.platform.modules.resource.dto;

public record ResourceRecommendationResponse(
        ResourceResponse resource,
        String reason
) {}

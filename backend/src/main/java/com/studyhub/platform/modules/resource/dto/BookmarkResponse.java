package com.studyhub.platform.modules.resource.dto;

public record BookmarkResponse(
        Long resourceId,
        boolean bookmarked
) {}

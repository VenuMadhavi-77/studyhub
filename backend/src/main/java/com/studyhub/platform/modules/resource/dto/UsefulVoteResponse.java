package com.studyhub.platform.modules.resource.dto;

public record UsefulVoteResponse(
        Long resourceId,
        boolean votedUseful,
        int totalUsefulCount
) {}

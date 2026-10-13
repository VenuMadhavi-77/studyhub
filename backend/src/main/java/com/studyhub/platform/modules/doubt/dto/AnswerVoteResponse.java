package com.studyhub.platform.modules.doubt.dto;

public record AnswerVoteResponse(
        Long answerId,
        boolean upvoted,
        long upvoteCount
) {}

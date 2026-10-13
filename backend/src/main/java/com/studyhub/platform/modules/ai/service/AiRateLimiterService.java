package com.studyhub.platform.modules.ai.service;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.config.ApplicationProperties;
import com.studyhub.platform.modules.ai.repository.AIInteractionRepository;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRateLimiterService {

    private final AIInteractionRepository aiInteractionRepository;
    private final ApplicationProperties applicationProperties;
    private final Map<Long, Bucket> userRateLimitBuckets = new ConcurrentHashMap<>();

    public AiRateLimiterService(AIInteractionRepository aiInteractionRepository,
                                ApplicationProperties applicationProperties) {
        this.aiInteractionRepository = aiInteractionRepository;
        this.applicationProperties = applicationProperties;
    }

    public void checkAndConsumeQuota(Long userId) {
        if (userId == null) {
            return;
        }

        // Per-minute rate limit via Bucket4j
        int perMinuteLimit = applicationProperties.getAi().getRateLimitPerMinute();
        Bucket bucket = userRateLimitBuckets.computeIfAbsent(userId, k -> {
            Bandwidth limit = Bandwidth.classic(perMinuteLimit, Refill.greedy(perMinuteLimit, Duration.ofMinutes(1)));
            return Bucket.builder().addLimit(limit).build();
        });

        if (!bucket.tryConsume(1)) {
            throw new BadRequestException("AI request limit reached. Please wait a moment before sending another request.");
        }

        // Daily quota check via DB interactions
        Instant startOfDay = Instant.now().truncatedTo(ChronoUnit.DAYS);
        long dailyCount = aiInteractionRepository.countDailyUserRequests(userId, startOfDay);

        int dailyQuota = applicationProperties.getAi().getDailyQuota();
        if (dailyCount >= dailyQuota) {
            throw new BadRequestException("Daily AI quota of " + dailyQuota + " requests reached for today.");
        }
    }
}

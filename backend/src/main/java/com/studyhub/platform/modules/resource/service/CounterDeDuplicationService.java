package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.ViewHistory;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.ViewHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class CounterDeDuplicationService {

    private static final Logger log = LoggerFactory.getLogger(CounterDeDuplicationService.class);
    private static final int DEDUPLICATION_WINDOW_HOURS = 1;

    private final ViewHistoryRepository viewHistoryRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public CounterDeDuplicationService(ViewHistoryRepository viewHistoryRepository,
                                       ResourceRepository resourceRepository,
                                       UserRepository userRepository) {
        this.viewHistoryRepository = viewHistoryRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Async
    @Transactional
    public void recordView(Long resourceId, Long userId, String ipAddress) {
        Instant windowStart = Instant.now().minus(DEDUPLICATION_WINDOW_HOURS, ChronoUnit.HOURS);

        boolean duplicate = false;
        if (userId != null) {
            duplicate = viewHistoryRepository.findTopByResourceIdAndUserIdAndViewedAtAfterOrderByViewedAtDesc(
                    resourceId, userId, windowStart).isPresent();
        } else if (ipAddress != null) {
            duplicate = viewHistoryRepository.findTopByResourceIdAndIpAddressAndViewedAtAfterOrderByViewedAtDesc(
                    resourceId, ipAddress, windowStart).isPresent();
        }

        if (!duplicate) {
            Optional<Resource> resourceOpt = resourceRepository.findById(resourceId);
            if (resourceOpt.isPresent()) {
                Resource resource = resourceOpt.get();
                resource.setViewCount(resource.getViewCount() + 1);
                resourceRepository.save(resource);

                User user = userId != null ? userRepository.findById(userId).orElse(null) : null;
                ViewHistory viewHistory = new ViewHistory(user, resource, ipAddress);
                viewHistoryRepository.save(viewHistory);
                log.debug("Incremented view count for resource: {}", resourceId);
            }
        }
    }

    @Transactional
    public void recordDownload(Long resourceId) {
        resourceRepository.findById(resourceId).ifPresent(resource -> {
            resource.setDownloadCount(resource.getDownloadCount() + 1);
            resourceRepository.save(resource);
        });
    }
}

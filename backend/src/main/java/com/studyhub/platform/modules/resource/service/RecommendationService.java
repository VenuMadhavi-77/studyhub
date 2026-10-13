package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceRecommendationResponse;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Bookmark;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.ViewHistory;
import com.studyhub.platform.modules.resource.repository.BookmarkRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.ViewHistoryRepository;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ViewHistoryRepository viewHistoryRepository;

    public RecommendationService(UserRepository userRepository,
                                 ResourceRepository resourceRepository,
                                 BookmarkRepository bookmarkRepository,
                                 ViewHistoryRepository viewHistoryRepository) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.viewHistoryRepository = viewHistoryRepository;
    }

    @Transactional(readOnly = true)
    public List<ResourceRecommendationResponse> getRecommendationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // 1. Collect seen resource IDs (from bookmarks and view history) to exclude
        Set<Long> seenResourceIds = new HashSet<>();

        List<Bookmark> userBookmarks = bookmarkRepository
                .findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 50))
                .getContent();
        for (Bookmark b : userBookmarks) {
            if (b.getResource() != null) {
                seenResourceIds.add(b.getResource().getId());
            }
        }

        List<ViewHistory> userViews = viewHistoryRepository.findByUserId(userId);
        for (ViewHistory v : userViews) {
            if (v.getResource() != null) {
                seenResourceIds.add(v.getResource().getId());
            }
        }

        // 2. Discover user interests from bookmarks & views
        Set<Long> interestedSubjectIds = new HashSet<>();
        for (Bookmark b : userBookmarks) {
            if (b.getResource() != null && b.getResource().getSubject() != null) {
                interestedSubjectIds.add(b.getResource().getSubject().getId());
            }
        }
        for (ViewHistory v : userViews) {
            if (v.getResource() != null && v.getResource().getSubject() != null) {
                interestedSubjectIds.add(v.getResource().getSubject().getId());
            }
        }

        // Parse user goals
        Set<String> userGoals = new HashSet<>();
        if (user.getGoals() != null && !user.getGoals().isBlank()) {
            for (String g : user.getGoals().toLowerCase().split(",")) {
                if (!g.trim().isEmpty()) {
                    userGoals.add(g.trim());
                }
            }
        }

        // 3. Score all approved resources
        List<Resource> allApproved = resourceRepository.findAll().stream()
                .filter(r -> r.getStatus() == ResourceStatus.APPROVED && !seenResourceIds.contains(r.getId()))
                .collect(Collectors.toList());

        List<ScoredResource> scoredList = new ArrayList<>();

        for (Resource r : allApproved) {
            double score = 0.0;
            List<String> reasons = new ArrayList<>();

            // A. Goal match (+25)
            boolean goalMatched = false;
            String resourceBestFor = (r.getBestFor() != null ? r.getBestFor().toLowerCase() : "");
            Set<String> resourceTagNames = r.getTags() != null
                    ? r.getTags().stream().map(Tag::getName).map(String::toLowerCase).collect(Collectors.toSet())
                    : Collections.emptySet();

            for (String goal : userGoals) {
                if (resourceBestFor.contains(goal) || resourceTagNames.contains(goal)) {
                    score += 25.0;
                    reasons.add("Matches your goal: " + goal);
                    goalMatched = true;
                    break;
                }
            }

            // Placements goal heuristic
            if (!goalMatched && (userGoals.contains("placements") || userGoals.contains("placement"))) {
                if (resourceBestFor.contains("placement") || resourceBestFor.contains("interview") ||
                        resourceTagNames.contains("placement") || resourceTagNames.contains("dsa") ||
                        resourceTagNames.contains("interview")) {
                    score += 25.0;
                    reasons.add("Best for placements preparation");
                    goalMatched = true;
                }
            }

            // B. Subject / Interest match (+30)
            if (r.getSubject() != null && interestedSubjectIds.contains(r.getSubject().getId())) {
                score += 30.0;
                reasons.add("Because you explored " + r.getSubject().getName());
            }

            // C. Department & Semester match (+20)
            if (user.getDepartment() != null && r.getDepartment() != null &&
                    user.getDepartment().getId().equals(r.getDepartment().getId())) {
                if (user.getSemester() != null && user.getSemester().equals(r.getSemester())) {
                    score += 20.0;
                    reasons.add("Tailored for Semester " + user.getSemester() + " " + user.getDepartment().getCode());
                } else {
                    score += 10.0;
                    reasons.add("Popular in your department (" + user.getDepartment().getCode() + ")");
                }
            }

            // D. Level match (+15)
            if (user.getLevel() != null && r.getLevel() != null && user.getLevel() == r.getLevel()) {
                score += 15.0;
                reasons.add("Matches your " + user.getLevel().name().toLowerCase() + " level");
            }

            // E. Language match (+15)
            if (user.getPreferredLanguage() != null && r.getLanguage() != null &&
                    user.getPreferredLanguage() == r.getLanguage()) {
                score += 15.0;
            }

            // F. Reliability score (+ reliabilityScore * 5)
            if (r.getReliabilityScore() != null && r.getReliabilityScore() > 0) {
                score += (r.getReliabilityScore() * 5.0);
                if (r.getReliabilityScore() >= 4.2) {
                    reasons.add("Highly reliable community rating (" + r.getReliabilityScore() + "★)");
                }
            } else if (r.getAvgRating() != null && r.getAvgRating() > 0) {
                score += (r.getAvgRating() * 4.0);
            }

            // Verified bonus (+10)
            if (r.isVerified()) {
                score += 10.0;
            }

            String primaryReason = !reasons.isEmpty() ? reasons.get(0) : "Recommended study resource";
            scoredList.add(new ScoredResource(r, score, primaryReason));
        }

        // Sort by score descending and pick top 12
        scoredList.sort((a, b) -> Double.compare(b.score, a.score));

        return scoredList.stream()
                .limit(12)
                .map(sr -> new ResourceRecommendationResponse(
                        ResourceResponse.fromEntity(sr.resource),
                        sr.reason
                ))
                .collect(Collectors.toList());
    }

    private static class ScoredResource {
        final Resource resource;
        final double score;
        final String reason;

        ScoredResource(Resource resource, double score, String reason) {
            this.resource = resource;
            this.score = score;
            this.reason = reason;
        }
    }
}

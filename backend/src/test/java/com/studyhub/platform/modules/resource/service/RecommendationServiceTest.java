package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.enums.ContentLanguage;
import com.studyhub.platform.common.enums.ContentLevel;
import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceRecommendationResponse;
import com.studyhub.platform.modules.resource.entity.Bookmark;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.ViewHistory;
import com.studyhub.platform.modules.resource.repository.BookmarkRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.ViewHistoryRepository;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private BookmarkRepository bookmarkRepository;

    @Mock
    private ViewHistoryRepository viewHistoryRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    private User user;
    private Resource seenResource;
    private Resource unseenResource;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setFullName("Recommendation Tester");
        user.setGoals("placements,competitive coding");
        user.setLevel(ContentLevel.ADVANCED);
        user.setPreferredLanguage(ContentLanguage.ENGLISH);

        Subject dsa = new Subject();
        dsa.setId(10L);
        dsa.setName("Data Structures");

        seenResource = new Resource();
        seenResource.setId(101L);
        seenResource.setTitle("Seen DSA Cheat Sheet");
        seenResource.setStatus(ResourceStatus.APPROVED);
        seenResource.setType(ResourceType.NOTE);
        seenResource.setSubject(dsa);

        unseenResource = new Resource();
        unseenResource.setId(102L);
        unseenResource.setTitle("Unseen LeetCode Top 150");
        unseenResource.setStatus(ResourceStatus.APPROVED);
        unseenResource.setType(ResourceType.EXTERNAL_LINK);
        unseenResource.setBestFor("Placements preparation and coding interviews");
        unseenResource.setLevel(ContentLevel.ADVANCED);
        unseenResource.setLanguage(ContentLanguage.ENGLISH);
        unseenResource.setSubject(dsa);
        unseenResource.setReliabilityScore(4.8);
    }

    @Test
    @DisplayName("Seen-item exclusion: Bookmarked and viewed items are never recommended again")
    void testSeenItemsExcludedFromRecommendations() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Bookmark bookmark = new Bookmark(user, seenResource);
        when(bookmarkRepository.findByUserIdOrderByCreatedAtDesc(eq(user.getId()), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(bookmark)));

        when(viewHistoryRepository.findByUserId(user.getId())).thenReturn(List.of());

        when(resourceRepository.findAll()).thenReturn(List.of(seenResource, unseenResource));

        List<ResourceRecommendationResponse> recommendations = recommendationService.getRecommendationsForUser(user.getId());

        assertNotNull(recommendations);
        assertEquals(1, recommendations.size());
        assertEquals(unseenResource.getId(), recommendations.get(0).resource().id());
        assertFalse(recommendations.stream().anyMatch(r -> r.resource().id().equals(seenResource.getId())));
    }
}

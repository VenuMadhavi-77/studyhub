package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.enums.ResourceType;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceFilterCriteria;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock
    private ResourceRepository resourceRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchService searchService;

    @Test
    void searchResources_ShouldReturnPagedResults_WhenCriteriaProvided() {
        ResourceFilterCriteria criteria = new ResourceFilterCriteria(
                "operating systems", 1L, null, null, 4, ResourceType.PDF, null, 4.0, null
        );
        Pageable pageable = PageRequest.of(0, 10);

        Resource res = new Resource();
        res.setId(10L);
        res.setTitle("Operating Systems Notes");
        res.setType(ResourceType.PDF);
        res.setSemester(4);

        Page<Resource> page = new PageImpl<>(List.of(res), pageable, 1);
        when(resourceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<ResourceResponse> result = searchService.searchResources(criteria, pageable, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Operating Systems Notes", result.getContent().get(0).title());
        verify(resourceRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }
}

package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.ResourceFilterCriteria;
import com.studyhub.platform.modules.resource.dto.ResourceResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.specification.ResourceSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SearchService {

    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public SearchService(ResourceRepository resourceRepository, UserRepository userRepository) {
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Page<ResourceResponse> searchResources(ResourceFilterCriteria criteria, Pageable pageable, Long userId) {
        Specification<Resource> spec = ResourceSpecification.buildSpecification(criteria.withDefaultStatus());
        Page<Resource> page = resourceRepository.findAll(spec, pageable);
        return page.map(ResourceResponse::fromEntity);
    }
}

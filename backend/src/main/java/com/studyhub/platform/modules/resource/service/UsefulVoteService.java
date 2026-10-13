package com.studyhub.platform.modules.resource.service;

import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.resource.dto.UsefulVoteResponse;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.UsefulVote;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.UsefulVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsefulVoteService {

    private final UsefulVoteRepository usefulVoteRepository;
    private final ResourceRepository resourceRepository;
    private final UserRepository userRepository;

    public UsefulVoteService(UsefulVoteRepository usefulVoteRepository,
                             ResourceRepository resourceRepository,
                             UserRepository userRepository) {
        this.usefulVoteRepository = usefulVoteRepository;
        this.resourceRepository = resourceRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UsefulVoteResponse toggleUsefulVote(Long resourceId, Long userId) {
        Resource resource = resourceRepository.findById(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource", "id", resourceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<UsefulVote> voteOpt = usefulVoteRepository.findByUserIdAndResourceId(userId, resourceId);
        boolean votedUseful;

        if (voteOpt.isPresent()) {
            usefulVoteRepository.deleteByUserIdAndResourceId(userId, resourceId);
            votedUseful = false;
        } else {
            UsefulVote newVote = new UsefulVote(user, resource);
            usefulVoteRepository.save(newVote);
            votedUseful = true;
        }

        long totalCount = usefulVoteRepository.countByResourceId(resourceId);
        resource.setUsefulCount((int) totalCount);
        resourceRepository.save(resource);

        return new UsefulVoteResponse(resourceId, votedUseful, (int) totalCount);
    }
}

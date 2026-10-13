package com.studyhub.platform.modules.doubt.service;

import com.studyhub.platform.common.enums.DoubtStatus;
import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.doubt.dto.AnswerResponse;
import com.studyhub.platform.modules.doubt.dto.DoubtCreateRequest;
import com.studyhub.platform.modules.doubt.dto.DoubtDetailResponse;
import com.studyhub.platform.modules.doubt.dto.DoubtResponse;
import com.studyhub.platform.modules.doubt.entity.Answer;
import com.studyhub.platform.modules.doubt.entity.Doubt;
import com.studyhub.platform.modules.doubt.repository.AnswerRepository;
import com.studyhub.platform.modules.doubt.repository.AnswerVoteRepository;
import com.studyhub.platform.modules.doubt.repository.DoubtRepository;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.service.TaxonomyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
public class DoubtService {

    private final DoubtRepository doubtRepository;
    private final AnswerRepository answerRepository;
    private final AnswerVoteRepository answerVoteRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;
    private final TaxonomyService taxonomyService;

    public DoubtService(DoubtRepository doubtRepository,
                        AnswerRepository answerRepository,
                        AnswerVoteRepository answerVoteRepository,
                        UserRepository userRepository,
                        SubjectRepository subjectRepository,
                        TaxonomyService taxonomyService) {
        this.doubtRepository = doubtRepository;
        this.answerRepository = answerRepository;
        this.answerVoteRepository = answerVoteRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
        this.taxonomyService = taxonomyService;
    }

    @Transactional
    public DoubtResponse createDoubt(DoubtCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Subject subject = subjectRepository.findById(request.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", request.subjectId()));

        Doubt doubt = new Doubt();
        doubt.setTitle(request.title().trim());
        doubt.setContent(request.content().trim());
        doubt.setUser(user);
        doubt.setSubject(subject);
        doubt.setStatus(DoubtStatus.OPEN);

        Set<Tag> tags = taxonomyService.findOrCreateTags(request.tags());
        doubt.setTags(tags);

        Doubt saved = doubtRepository.save(doubt);
        return DoubtResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public Page<DoubtResponse> getDoubts(Long subjectId, DoubtStatus status, Pageable pageable) {
        Page<Doubt> doubts;
        if (subjectId != null) {
            doubts = doubtRepository.findBySubjectIdOrderByCreatedAtDesc(subjectId, pageable);
        } else if (status != null) {
            doubts = doubtRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            doubts = doubtRepository.findAllByOrderByCreatedAtDesc(pageable);
        }
        return doubts.map(DoubtResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public DoubtDetailResponse getDoubtDetail(Long id, Long currentUserId) {
        Doubt doubt = doubtRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doubt", "id", id));

        List<Answer> answers = answerRepository.findByDoubtIdOrderByUpvoteCountDescCreatedAtAsc(id);
        List<AnswerResponse> answerResponses = answers.stream().map(a -> {
            boolean upvoted = currentUserId != null && answerVoteRepository.existsByUserIdAndAnswerId(currentUserId, a.getId());
            return AnswerResponse.fromEntity(a, upvoted);
        }).toList();

        return DoubtDetailResponse.fromEntity(doubt, answerResponses);
    }

    @Transactional
    public DoubtResponse acceptAnswer(Long doubtId, Long answerId, Long requesterId) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new ResourceNotFoundException("Doubt", "id", doubtId));

        if (!doubt.getUser().getId().equals(requesterId)) {
            throw new UnauthorizedException("Only the author of this question can accept an answer");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", answerId));

        if (!answer.getDoubt().getId().equals(doubtId)) {
            throw new BadRequestException("Answer does not belong to this doubt");
        }

        doubt.setAcceptedAnswerId(answerId);
        doubt.setStatus(DoubtStatus.RESOLVED);
        Doubt saved = doubtRepository.save(doubt);
        return DoubtResponse.fromEntity(saved);
    }

    @Transactional
    public void deleteDoubt(Long id, Long requesterId, boolean isAdmin) {
        Doubt doubt = doubtRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doubt", "id", id));

        if (!doubt.getUser().getId().equals(requesterId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this doubt");
        }

        doubt.setDeleted(true);
        doubtRepository.save(doubt);
    }
}

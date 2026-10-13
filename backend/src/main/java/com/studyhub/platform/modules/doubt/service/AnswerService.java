package com.studyhub.platform.modules.doubt.service;

import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.doubt.dto.AnswerCreateRequest;
import com.studyhub.platform.modules.doubt.dto.AnswerResponse;
import com.studyhub.platform.modules.doubt.dto.AnswerVoteResponse;
import com.studyhub.platform.modules.doubt.entity.Answer;
import com.studyhub.platform.modules.doubt.entity.AnswerVote;
import com.studyhub.platform.modules.doubt.entity.Doubt;
import com.studyhub.platform.modules.doubt.repository.AnswerRepository;
import com.studyhub.platform.modules.doubt.repository.AnswerVoteRepository;
import com.studyhub.platform.modules.doubt.repository.DoubtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final DoubtRepository doubtRepository;
    private final UserRepository userRepository;
    private final AnswerVoteRepository answerVoteRepository;

    public AnswerService(AnswerRepository answerRepository,
                         DoubtRepository doubtRepository,
                         UserRepository userRepository,
                         AnswerVoteRepository answerVoteRepository) {
        this.answerRepository = answerRepository;
        this.doubtRepository = doubtRepository;
        this.userRepository = userRepository;
        this.answerVoteRepository = answerVoteRepository;
    }

    @Transactional
    public AnswerResponse postAnswer(Long doubtId, AnswerCreateRequest request, Long userId, boolean isAi) {
        Doubt doubt = doubtRepository.findById(doubtId)
                .orElseThrow(() -> new ResourceNotFoundException("Doubt", "id", doubtId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Answer answer = new Answer();
        answer.setDoubt(doubt);
        answer.setUser(user);
        answer.setContent(request.content().trim());
        answer.setAiGenerated(isAi);
        answer.setUpvoteCount(0);

        Answer saved = answerRepository.save(answer);
        return AnswerResponse.fromEntity(saved, false);
    }

    @Transactional
    public AnswerVoteResponse toggleUpvote(Long answerId, Long userId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", answerId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Optional<AnswerVote> voteOpt = answerVoteRepository.findByUserIdAndAnswerId(userId, answerId);
        boolean upvoted;

        if (voteOpt.isPresent()) {
            answerVoteRepository.deleteByUserIdAndAnswerId(userId, answerId);
            upvoted = false;
        } else {
            AnswerVote vote = new AnswerVote(user, answer);
            answerVoteRepository.save(vote);
            upvoted = true;
        }

        long count = answerVoteRepository.countByAnswerId(answerId);
        answer.setUpvoteCount((int) count);
        answerRepository.save(answer);

        return new AnswerVoteResponse(answerId, upvoted, count);
    }

    @Transactional
    public void deleteAnswer(Long answerId, Long requesterId, boolean isAdmin) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new ResourceNotFoundException("Answer", "id", answerId));

        if (!answer.getUser().getId().equals(requesterId) && !isAdmin) {
            throw new UnauthorizedException("You are not authorized to delete this answer");
        }

        answer.setDeleted(true);
        answerRepository.save(answer);
    }
}

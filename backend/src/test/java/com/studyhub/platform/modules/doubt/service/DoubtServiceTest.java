package com.studyhub.platform.modules.doubt.service;

import com.studyhub.platform.common.enums.DoubtStatus;
import com.studyhub.platform.common.exception.UnauthorizedException;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.doubt.dto.DoubtCreateRequest;
import com.studyhub.platform.modules.doubt.dto.DoubtResponse;
import com.studyhub.platform.modules.doubt.entity.Answer;
import com.studyhub.platform.modules.doubt.entity.Doubt;
import com.studyhub.platform.modules.doubt.repository.AnswerRepository;
import com.studyhub.platform.modules.doubt.repository.AnswerVoteRepository;
import com.studyhub.platform.modules.doubt.repository.DoubtRepository;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.service.TaxonomyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoubtServiceTest {

    @Mock
    private DoubtRepository doubtRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private AnswerVoteRepository answerVoteRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SubjectRepository subjectRepository;

    @Mock
    private TaxonomyService taxonomyService;

    @InjectMocks
    private DoubtService doubtService;

    private User author;
    private Subject subject;

    @BeforeEach
    void setUp() {
        author = new User();
        author.setId(10L);
        author.setEmail("student@test.edu");
        author.setFullName("Student Author");

        subject = new Subject();
        subject.setId(1L);
        subject.setName("Computer Networks");
    }

    @Test
    void createDoubt_ShouldSaveAndReturnResponse() {
        DoubtCreateRequest request = new DoubtCreateRequest(
                "How does TCP 3-way handshake work?",
                "Need detailed steps with SYN and ACK flags.",
                1L,
                Set.of("networking", "tcp")
        );

        when(userRepository.findById(10L)).thenReturn(Optional.of(author));
        when(subjectRepository.findById(1L)).thenReturn(Optional.of(subject));
        when(taxonomyService.findOrCreateTags(any())).thenReturn(Set.of());

        Doubt saved = new Doubt();
        saved.setId(100L);
        saved.setTitle(request.title());
        saved.setContent(request.content());
        saved.setUser(author);
        saved.setSubject(subject);
        saved.setStatus(DoubtStatus.OPEN);

        when(doubtRepository.save(any(Doubt.class))).thenReturn(saved);

        DoubtResponse response = doubtService.createDoubt(request, 10L);

        assertNotNull(response);
        assertEquals(100L, response.id());
        assertEquals("How does TCP 3-way handshake work?", response.title());
        verify(doubtRepository, times(1)).save(any(Doubt.class));
    }

    @Test
    void acceptAnswer_ShouldThrowUnauthorized_WhenRequesterIsNotAuthor() {
        Doubt doubt = new Doubt();
        doubt.setId(100L);
        doubt.setUser(author); // Author is 10L

        when(doubtRepository.findById(100L)).thenReturn(Optional.of(doubt));

        // Requester 99L is not the author
        assertThrows(UnauthorizedException.class, () -> doubtService.acceptAnswer(100L, 50L, 99L));
        verify(answerRepository, never()).findById(any());
    }

    @Test
    void acceptAnswer_ShouldAcceptAndResolve_WhenRequesterIsAuthor() {
        Doubt doubt = new Doubt();
        doubt.setId(100L);
        doubt.setUser(author);
        doubt.setStatus(DoubtStatus.OPEN);

        Answer answer = new Answer();
        answer.setId(50L);
        answer.setDoubt(doubt);

        when(doubtRepository.findById(100L)).thenReturn(Optional.of(doubt));
        when(answerRepository.findById(50L)).thenReturn(Optional.of(answer));
        when(doubtRepository.save(any(Doubt.class))).thenReturn(doubt);

        DoubtResponse response = doubtService.acceptAnswer(100L, 50L, 10L);

        assertNotNull(response);
        assertEquals(50L, doubt.getAcceptedAnswerId());
        assertEquals(DoubtStatus.RESOLVED, doubt.getStatus());
        verify(doubtRepository, times(1)).save(doubt);
    }
}

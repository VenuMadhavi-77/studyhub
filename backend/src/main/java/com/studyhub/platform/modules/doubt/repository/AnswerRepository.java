package com.studyhub.platform.modules.doubt.repository;

import com.studyhub.platform.modules.doubt.entity.Answer;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {

    @EntityGraph(attributePaths = {"user"})
    List<Answer> findByDoubtIdOrderByUpvoteCountDescCreatedAtAsc(Long doubtId);
}

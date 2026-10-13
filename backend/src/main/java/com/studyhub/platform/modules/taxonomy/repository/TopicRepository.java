package com.studyhub.platform.modules.taxonomy.repository;

import com.studyhub.platform.modules.taxonomy.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findBySubjectIdAndActiveTrueOrderByNameAsc(Long subjectId);
}

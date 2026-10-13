package com.studyhub.platform.modules.resource.repository;

import com.studyhub.platform.modules.resource.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByResourceIdOrderByHelpfulCountDescCreatedAtDesc(Long resourceId);

    Optional<Review> findByResourceIdAndUserId(Long resourceId, Long userId);

    boolean existsByResourceIdAndUserId(Long resourceId, Long userId);

    long countByResourceId(Long resourceId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.resource.id = :resourceId")
    Double calculateAverageRating(@Param("resourceId") Long resourceId);
}

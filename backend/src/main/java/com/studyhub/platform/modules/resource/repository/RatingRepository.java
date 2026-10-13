package com.studyhub.platform.modules.resource.repository;

import com.studyhub.platform.modules.resource.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByUserIdAndResourceId(Long userId, Long resourceId);
    boolean existsByUserIdAndResourceId(Long userId, Long resourceId);

    @Query("SELECT AVG(r.score) FROM Rating r WHERE r.resource.id = :resourceId")
    Double calculateAverageRating(Long resourceId);

    @Query("SELECT COUNT(r) FROM Rating r WHERE r.resource.id = :resourceId")
    Integer countRatingsByResourceId(Long resourceId);
}

package com.studyhub.platform.modules.resource.specification;

import com.studyhub.platform.common.enums.ResourceStatus;
import com.studyhub.platform.modules.resource.dto.ResourceFilterCriteria;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ResourceSpecification {

    public static Specification<Resource> buildSpecification(ResourceFilterCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Exclude soft-deleted resources
            predicates.add(criteriaBuilder.isFalse(root.get("deleted")));

            // Resource status filter (default: APPROVED)
            ResourceStatus status = criteria.status() != null ? criteria.status() : ResourceStatus.APPROVED;
            predicates.add(criteriaBuilder.equal(root.get("status"), status));

            // Text search in title or description
            if (StringUtils.hasText(criteria.query())) {
                String searchPattern = "%" + criteria.query().trim().toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchPattern);
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), searchPattern);
                predicates.add(criteriaBuilder.or(titleLike, descLike));
            }

            // Department filter
            if (criteria.departmentId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("department").get("id"), criteria.departmentId()));
            }

            // Subject filter
            if (criteria.subjectId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("subject").get("id"), criteria.subjectId()));
            }

            // Topic filter
            if (criteria.topicId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("topic").get("id"), criteria.topicId()));
            }

            // Semester filter
            if (criteria.semester() != null) {
                predicates.add(criteriaBuilder.equal(root.get("semester"), criteria.semester()));
            }

            // Resource Type filter
            if (criteria.type() != null) {
                predicates.add(criteriaBuilder.equal(root.get("type"), criteria.type()));
            }

            // Minimum Average Rating filter
            if (criteria.minRating() != null && criteria.minRating() > 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("avgRating"), criteria.minRating()));
            }

            // Tag filter
            if (StringUtils.hasText(criteria.tag())) {
                Join<Resource, Tag> tagJoin = root.join("tags", JoinType.INNER);
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(tagJoin.get("name")), criteria.tag().trim().toLowerCase()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

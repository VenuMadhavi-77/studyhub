-- =========================================================
-- StudyHub Database Schema - V3__catalog_reviews_focus.sql
-- Catalog, Personalization, Reviews, Bayesian Reliability & Focus Sessions
-- =========================================================

-- 1. Users Table Personalization Enhancements
ALTER TABLE users ADD COLUMN goals VARCHAR(255) NULL;
ALTER TABLE users ADD COLUMN preferred_language VARCHAR(30) DEFAULT 'ENGLISH';
ALTER TABLE users ADD COLUMN level VARCHAR(30) DEFAULT 'BEGINNER';

-- 2. Resources Table Catalog & Reliability Enhancements
ALTER TABLE resources ADD COLUMN source_platform VARCHAR(50) DEFAULT 'STUDYHUB';
ALTER TABLE resources ADD COLUMN level VARCHAR(30) DEFAULT 'BEGINNER';
ALTER TABLE resources ADD COLUMN language VARCHAR(30) DEFAULT 'ENGLISH';
ALTER TABLE resources ADD COLUMN is_free BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE resources ADD COLUMN best_for VARCHAR(255) NULL;
ALTER TABLE resources ADD COLUMN verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE resources ADD COLUMN verified_by VARCHAR(100) NULL;
ALTER TABLE resources ADD COLUMN verified_at TIMESTAMP NULL;
ALTER TABLE resources ADD COLUMN reliability_score DOUBLE NOT NULL DEFAULT 0.0;
ALTER TABLE resources ADD COLUMN reliability_label VARCHAR(50) DEFAULT 'New - not enough reviews';

-- 3. Resource Reviews Table
CREATE TABLE IF NOT EXISTS resource_reviews (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    resource_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT NOT NULL,
    review_text VARCHAR(2000) NOT NULL,
    helpful_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_resource FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_reviews_resource_user UNIQUE (resource_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Review Helpful Votes Table
CREATE TABLE IF NOT EXISTS review_helpful_votes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    review_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_helpful_review FOREIGN KEY (review_id) REFERENCES resource_reviews(id) ON DELETE CASCADE,
    CONSTRAINT fk_helpful_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uk_helpful_review_user UNIQUE (review_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Focus Sessions Table
CREATE TABLE IF NOT EXISTS focus_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    subject_id BIGINT NULL,
    mode VARCHAR(30) NOT NULL DEFAULT 'POMODORO',
    planned_minutes INT NOT NULL DEFAULT 25,
    actual_minutes INT NOT NULL DEFAULT 0,
    started_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ended_at TIMESTAMP NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT fk_focus_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_focus_subject FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_focus_user_started ON focus_sessions(user_id, started_at);

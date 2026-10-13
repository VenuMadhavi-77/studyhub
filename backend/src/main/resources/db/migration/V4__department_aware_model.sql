-- =========================================================
-- StudyHub Database Schema - V4__department_aware_model.sql
-- Multi-Department Awareness: Goals, Platforms, Tools & General Resources
-- =========================================================

-- 1. Make department_id in resources nullable (NULL = General / All Departments)
ALTER TABLE resources MODIFY COLUMN department_id BIGINT NULL;

-- 2. Department-Specific & Shared Study Goals
CREATE TABLE IF NOT EXISTS goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NULL,
    goal_key VARCHAR(100) NOT NULL,
    label VARCHAR(150) NOT NULL,
    description VARCHAR(255) NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_goals_department FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_goals_department ON goals(department_id);

-- 3. Department Curated External Platforms
CREATE TABLE IF NOT EXISTS department_platforms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    platform VARCHAR(50) NOT NULL,
    priority INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dept_platforms_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_dept_platforms_dept ON department_platforms(department_id);

-- 4. Department Standard Software Tools & Frameworks
CREATE TABLE IF NOT EXISTS department_tools (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    department_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dept_tools_dept FOREIGN KEY (department_id) REFERENCES departments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_dept_tools_dept ON department_tools(department_id);

-- 5. Performance Indexes for Department-Aware Filtering
CREATE INDEX idx_resources_dept_sem ON resources(department_id, semester);
CREATE INDEX idx_subjects_dept_sem ON subjects(department_id, semester);

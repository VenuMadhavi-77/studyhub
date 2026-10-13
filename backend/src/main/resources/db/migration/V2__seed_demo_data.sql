```sql
-- ==========================================================================
-- STUDYHUB DEMO SEED DATA (Flyway Migration V2)
-- ==========================================================================

-- 1. Initial Academic Departments
INSERT INTO departments
    (id, name, code, created_at, updated_at)
VALUES
    (1, 'Computer Science & Engineering', 'CSE', NOW(), NOW()),
    (2, 'Information Technology', 'IT', NOW(), NOW()),
    (3, 'Electronics & Communication Engineering', 'ECE', NOW(), NOW()),
    (4, 'Mechanical Engineering', 'MECH', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name);


-- 2. Core Subjects
INSERT INTO subjects
    (id, department_id, name, code, semester, created_at, updated_at)
VALUES
    (1, 1, 'Data Structures & Algorithms', 'CS301', 3, NOW(), NOW()),
    (2, 1, 'Database Management Systems', 'CS401', 4, NOW(), NOW()),
    (3, 1, 'Operating Systems', 'CS402', 4, NOW(), NOW()),
    (4, 1, 'Computer Networks', 'CS501', 5, NOW(), NOW()),
    (5, 1, 'Artificial Intelligence', 'CS601', 6, NOW(), NOW()),
    (6, 2, 'Web Technologies & Cloud Computing', 'IT502', 5, NOW(), NOW()),
    (7, 3, 'Digital Signal Processing', 'EC501', 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name);


-- 3. Topics
INSERT INTO topics
    (id, subject_id, name, created_at, updated_at)
VALUES
    (1, 1, 'Binary Search Trees & AVL Trees', NOW(), NOW()),
    (2, 1, 'Graph Traversals (BFS/DFS) & Dijkstra', NOW(), NOW()),
    (3, 2, 'Relational Algebra & Normalization (1NF-BCNF)', NOW(), NOW()),
    (4, 2, 'ACID Properties & Transaction Isolation', NOW(), NOW()),
    (5, 3, 'Process Synchronization & Semaphores', NOW(), NOW()),
    (6, 3, 'CPU Scheduling Algorithms & Deadlocks', NOW(), NOW()),
    (7, 4, 'TCP/IP 5-Layer Model & Subnetting', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name);


-- 4. Initial Tags
-- The slug column was removed because it does not exist
-- in the current tags table schema.
INSERT INTO tags
    (id, name, created_at)
VALUES
    (1, 'algorithms', NOW()),
    (2, 'dbms', NOW()),
    (3, 'concurrency', NOW()),
    (4, 'networking', NOW()),
    (5, 'exam-prep', NOW()),
    (6, 'cheatsheet', NOW())
ON DUPLICATE KEY UPDATE
    name = VALUES(name);


-- 5. Demo Faculty and Students
-- NOTE: Verify the BCrypt hashes before using these accounts.
INSERT INTO users
    (id, email, password_hash, full_name, role, status,
     reputation_points, created_at, updated_at)
VALUES
    (2, 'faculty.sharma@studyhub.edu',
     '$2a$10$7Rz6Z3aN.7cKqG1tF9mF6eXJgP1mF8wXp9q8r7s6t5u4v3w2x1y0z',
     'Prof. Ramesh Sharma', 'FACULTY', 'ACTIVE', 500, NOW(), NOW()),

    (3, 'student.priya@studyhub.edu',
     '$2a$10$7Rz6Z3aN.7cKqG1tF9mF6eXJgP1mF8wXp9q8r7s6t5u4v3w2x1y0z',
     'Priya Patel', 'STUDENT', 'ACTIVE', 120, NOW(), NOW()),

    (4, 'student.rahul@studyhub.edu',
     '$2a$10$7Rz6Z3aN.7cKqG1tF9mF6eXJgP1mF8wXp9q8r7s6t5u4v3w2x1y0z',
     'Rahul Verma', 'STUDENT', 'ACTIVE', 85, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    full_name = VALUES(full_name);
```s

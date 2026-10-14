-- =========================================================
-- StudyHub Database Migration - V5__seed_catalog_resources_and_reviews.sql
-- Seeds Curated Academic Resources & Multi-User Peer Reviews
-- Guaranteed Catalog Seeding for All Environments (Local, Test & Production)
-- =========================================================

-- 1. Additional Demo Users for Multi-User Review Simulation
INSERT INTO users (id, email, password_hash, full_name, role, status, department_id, semester, created_at, updated_at) VALUES
(5, 'student.ananya@studyhub.edu', '$2a$10$7Rz6Z3aN.7cKqG1tF9mF6eXJgP1mF8wXp9q8r7s6t5u4v3w2x1y0z', 'Ananya Sen', 'STUDENT', 'ACTIVE', 2, 5, NOW(), NOW()),
(6, 'student.vikram@studyhub.edu', '$2a$10$7Rz6Z3aN.7cKqG1tF9mF6eXJgP1mF8wXp9q8r7s6t5u4v3w2x1y0z', 'Vikram Reddy', 'STUDENT', 'ACTIVE', 3, 4, NOW(), NOW()),
(7, 'faculty.kulkarni@studyhub.edu', '$2a$10$7Rz6Z3aN.7cKqG1tF9mF6eXJgP1mF8wXp9q8r7s6t5u4v3w2x1y0z', 'Dr. Sunita Kulkarni', 'FACULTY', 'ACTIVE', 3, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE full_name=VALUES(full_name);

-- 2. Ensure Core Department Subjects are Present
INSERT INTO subjects (id, department_id, name, code, semester, created_at, updated_at) VALUES
(8, 2, 'Cloud Computing & DevOps', 'IT503', 6, NOW(), NOW()),
(9, 3, 'Microprocessors & Microcontrollers', 'EC402', 4, NOW(), NOW()),
(10, 4, 'Engineering Thermodynamics', 'ME301', 3, NOW(), NOW()),
(11, 4, 'Fluid Mechanics & Machinery', 'ME401', 4, NOW(), NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 3. Seed Verified Academic Resources Across All Engineering Departments
INSERT INTO resources (
    id, title, description, type, file_path, file_mime_type, file_size_bytes, external_url,
    department_id, subject_id, topic_id, semester, uploader_id, status,
    avg_rating, rating_count, useful_count, view_count, download_count,
    source_platform, level, language, is_free, verified, verified_by,
    reliability_score, reliability_label, created_at, updated_at
) VALUES
-- Department 1 (Computer Science & Engineering) - Resource 1
(1, 'Complete Data Structures & Algorithms Cheatsheet & Code Snippets',
 'Comprehensive exam revision notes covering Binary Search Trees, Graph traversals (BFS, DFS, Dijkstra), Dynamic Programming, and Big-O complexity tables.',
 'PDF', 'dsa_trees_graphs_cheatsheet.pdf', 'application/pdf', 1299, NULL,
 1, 1, 1, 3, 2, 'APPROVED',
 4.67, 3, 25, 342, 189,
 'STUDYHUB', 'INTERMEDIATE', 'ENGLISH', TRUE, TRUE, 'Department Head',
 4.44, 'Highly reliable', NOW(), NOW()),

-- Department 1 (Computer Science & Engineering) - Resource 2
(2, 'Operating Systems Process Scheduling & Memory Management Notes',
 'Detailed lecture notes covering CPU scheduling algorithms, Deadlocks avoidance (Bankers Algorithm), Paging, Virtual Memory, and Semaphores.',
 'PDF', 'os_process_scheduling_deadlocks.pdf', 'application/pdf', 1304, NULL,
 1, 3, 5, 4, 3, 'APPROVED',
 4.33, 3, 18, 215, 102,
 'STUDYHUB', 'BEGINNER', 'ENGLISH', TRUE, FALSE, NULL,
 3.96, 'Good', NOW(), NOW()),

-- Department 2 (Information Technology) - Resource 3
(3, 'Full Stack Web Development & RESTful API Architecture Guide',
 'Complete full-stack engineering guide covering modern React components, state management, REST API patterns, JWT authentication, and database indexing.',
 'PDF', 'web_dev_react_hooks_state_guide.pdf', 'application/pdf', 1305, NULL,
 2, 6, NULL, 5, 2, 'APPROVED',
 4.67, 3, 29, 280, 145,
 'STUDYHUB', 'INTERMEDIATE', 'ENGLISH', TRUE, TRUE, 'Department Head',
 4.44, 'Highly reliable', NOW(), NOW()),

-- Department 2 (Information Technology) - Resource 4
(4, 'Cloud Computing AWS & Docker Containerization Crash Course',
 'Industry-grade notes covering microservice architectures, Docker multi-stage builds, container registries, AWS cloud infrastructure, and CI/CD pipelines.',
 'PDF', 'software_engineering_system_design.pdf', 'application/pdf', 2328, NULL,
 2, 8, NULL, 6, 4, 'APPROVED',
 4.33, 3, 16, 198, 87,
 'STUDYHUB', 'ADVANCED', 'ENGLISH', TRUE, FALSE, NULL,
 3.96, 'Good', NOW(), NOW()),

-- Department 3 (Electronics & Communication Engineering) - Resource 5
(5, 'Digital Signal Processing Fourier Transforms & Filter Design',
 'In-depth analytical study guide covering Discrete Fourier Transform (DFT), Fast Fourier Transform (FFT), IIR/FIR filter design, and spectral estimation.',
 'PDF', 'cn_tcp_ip_osi_model_summary.pdf', 'application/pdf', 1304, NULL,
 3, 7, NULL, 5, 7, 'APPROVED',
 4.67, 3, 34, 245, 130,
 'STUDYHUB', 'ADVANCED', 'ENGLISH', TRUE, TRUE, 'ECE Academic Council',
 4.44, 'Highly reliable', NOW(), NOW()),

-- Department 3 (Electronics & Communication Engineering) - Resource 6
(6, 'Microprocessors 8086 & Embedded Architecture Revision Notes',
 'Hardware architecture notes including 8086 pin diagram, bus cycles, addressing modes, interrupt handling, and peripheral interfacing circuits.',
 'PDF', 'coa_pipelining_cache_architecture.pdf', 'application/pdf', 2325, NULL,
 3, 9, NULL, 4, 6, 'APPROVED',
 4.33, 3, 19, 176, 79,
 'STUDYHUB', 'INTERMEDIATE', 'ENGLISH', TRUE, FALSE, NULL,
 3.96, 'Good', NOW(), NOW()),

-- Department 4 (Mechanical Engineering) - Resource 7
(7, 'Engineering Thermodynamics & Heat Transfer Formula Handbook',
 'Quick reference formula handbook covering the First and Second Laws of Thermodynamics, Carnot and Rankine cycles, entropy generation, and heat exchangers.',
 'PDF', 'dbms_normalization_sql_guide.pdf', 'application/pdf', 1300, NULL,
 4, 10, NULL, 3, 2, 'APPROVED',
 4.67, 3, 27, 310, 165,
 'STUDYHUB', 'BEGINNER', 'ENGLISH', TRUE, TRUE, 'Department Head',
 4.44, 'Highly reliable', NOW(), NOW()),

-- Department 4 (Mechanical Engineering) - Resource 8
(8, 'Fluid Mechanics & Turbo Machinery Solved Numerical Problems',
 'Solved university numericals on Bernoulli theorem, Navier-Stokes approximations, boundary layer theory, Pelton wheels, and centrifugal pump performance.',
 'PDF', 'sql_advanced_joins_indexing_guide.pdf', 'application/pdf', 2325, NULL,
 4, 11, NULL, 4, 4, 'APPROVED',
 4.33, 3, 21, 189, 94,
 'STUDYHUB', 'INTERMEDIATE', 'ENGLISH', TRUE, FALSE, NULL,
 3.96, 'Good', NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    title=VALUES(title),
    reliability_score=VALUES(reliability_score),
    reliability_label=VALUES(reliability_label),
    avg_rating=VALUES(avg_rating),
    rating_count=VALUES(rating_count);

-- 4. Seed Multi-User Peer Reviews (Strictly Respecting Non-Self-Review Constraints)
INSERT INTO resource_reviews (id, resource_id, user_id, rating, review_text, helpful_count, created_at) VALUES
-- Reviews for Resource 1 (Uploaded by User 2 - Prof. Sharma)
(1, 1, 3, 5, 'Outstanding explanation of binary search trees and graph algorithms. Saved my mid-term prep!', 12, NOW()),
(2, 1, 4, 5, 'Concise pseudocode and Big-O complexity tables. Highly recommended for semester exams and coding interviews.', 8, NOW()),
(3, 1, 5, 4, 'Very clear and structured notes, especially on dynamic programming recurrence relations.', 5, NOW()),

-- Reviews for Resource 2 (Uploaded by User 3 - Priya Patel)
(4, 2, 2, 4, 'Good student synthesis of deadlock avoidance algorithms and virtual memory paging.', 9, NOW()),
(5, 2, 4, 4, 'Helpful diagrams explaining CPU scheduling and semaphores clearly.', 6, NOW()),
(6, 2, 6, 5, 'Perfect revision summary before university exams. Clear and easy to grasp.', 7, NOW()),

-- Reviews for Resource 3 (Uploaded by User 2 - Prof. Sharma)
(7, 3, 3, 5, 'Excellent breakdown of modern React state management and backend REST APIs.', 14, NOW()),
(8, 3, 5, 5, 'Super practical examples for building enterprise full-stack web systems.', 11, NOW()),
(9, 3, 6, 4, 'Well organized with clear architectural diagrams. Great reference for lab exams.', 4, NOW()),

-- Reviews for Resource 4 (Uploaded by User 4 - Rahul Verma)
(10, 4, 3, 4, 'Clean walk-through of Docker containerization and AWS cloud concepts.', 8, NOW()),
(11, 4, 5, 4, 'Very easy to follow steps for beginners learning cloud deployment.', 5, NOW()),
(12, 4, 6, 5, 'Clear explanations of CI/CD pipelines and microservice architectures.', 10, NOW()),

-- Reviews for Resource 5 (Uploaded by User 7 - Dr. Sunita Kulkarni)
(13, 5, 4, 5, 'Dr. Kulkarnis DSP notes simplify FFT and Z-transform concepts brilliantly.', 15, NOW()),
(14, 5, 5, 5, 'The solved problems on IIR and FIR digital filters are thorough and accurate.', 12, NOW()),
(15, 5, 6, 4, 'Crucial material for mastering digital signal processing algorithms.', 7, NOW()),

-- Reviews for Resource 6 (Uploaded by User 6 - Vikram Reddy)
(16, 6, 3, 4, 'Solid review of 8086 registers, pin diagrams, and memory interfacing.', 6, NOW()),
(17, 6, 4, 4, 'Concise assembly language opcode tables and timing diagrams.', 5, NOW()),
(18, 6, 7, 5, 'Commendable student work with accurate pin configurations and timing details.', 11, NOW()),

-- Reviews for Resource 7 (Uploaded by User 2 - Prof. Sharma)
(19, 7, 4, 5, 'Comprehensive Carnot cycle diagrams and entropy formulation tables.', 13, NOW()),
(20, 7, 5, 4, 'Clear equations for first and second laws of thermodynamics.', 7, NOW()),
(21, 7, 6, 5, 'Indispensable formula sheet for thermal engineering exams.', 9, NOW()),

-- Reviews for Resource 8 (Uploaded by User 4 - Rahul Verma)
(22, 8, 3, 4, 'Step-by-step solved Bernoulli equation and Navier-Stokes problems.', 8, NOW()),
(23, 8, 5, 5, 'Great numerical examples on boundary layer flow and centrifugal pumps.', 11, NOW()),
(24, 8, 6, 4, 'Helpful dimensional analysis and hydraulic jump derivations.', 6, NOW())
ON DUPLICATE KEY UPDATE 
    rating=VALUES(rating),
    review_text=VALUES(review_text),
    helpful_count=VALUES(helpful_count);

-- 5. Associate Resources with Academic Tags
INSERT INTO resource_tags (resource_id, tag_id) VALUES
(1, 1), (1, 5), (1, 6),
(2, 3), (2, 5),
(3, 2), (3, 6),
(4, 4), (4, 5),
(5, 4), (5, 6),
(6, 3), (6, 6),
(7, 5), (7, 6),
(8, 5), (8, 6)
ON DUPLICATE KEY UPDATE resource_id=VALUES(resource_id);

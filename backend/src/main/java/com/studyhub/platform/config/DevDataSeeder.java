package com.studyhub.platform.config;

import com.studyhub.platform.common.enums.*;
import com.studyhub.platform.modules.admin.repository.ReportRepository;
import com.studyhub.platform.modules.auth.entity.User;
import com.studyhub.platform.modules.auth.repository.UserRepository;
import com.studyhub.platform.modules.doubt.entity.Doubt;
import com.studyhub.platform.modules.doubt.repository.DoubtRepository;
import com.studyhub.platform.modules.focus.entity.FocusSession;
import com.studyhub.platform.modules.focus.repository.FocusSessionRepository;
import com.studyhub.platform.modules.resource.entity.Bookmark;
import com.studyhub.platform.modules.resource.entity.Resource;
import com.studyhub.platform.modules.resource.entity.Review;
import com.studyhub.platform.modules.resource.entity.ReviewHelpfulVote;
import com.studyhub.platform.modules.resource.repository.BookmarkRepository;
import com.studyhub.platform.modules.resource.repository.ResourceRepository;
import com.studyhub.platform.modules.resource.repository.ReviewHelpfulVoteRepository;
import com.studyhub.platform.modules.resource.repository.ReviewRepository;
import com.studyhub.platform.modules.resource.service.ReviewService;
import com.studyhub.platform.modules.taxonomy.entity.Department;
import com.studyhub.platform.modules.taxonomy.entity.Subject;
import com.studyhub.platform.modules.taxonomy.entity.Tag;
import com.studyhub.platform.modules.taxonomy.entity.Topic;
import com.studyhub.platform.modules.taxonomy.repository.DepartmentRepository;
import com.studyhub.platform.modules.taxonomy.repository.SubjectRepository;
import com.studyhub.platform.modules.taxonomy.repository.TagRepository;
import com.studyhub.platform.modules.taxonomy.repository.TopicRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
@Profile("!prod")
@Order(10)
public class DevDataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DevDataSeeder.class);

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;
    private final ResourceRepository resourceRepository;
    private final ReviewRepository reviewRepository;
    private final ReviewHelpfulVoteRepository helpfulVoteRepository;
    private final DoubtRepository doubtRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReviewService reviewService;
    private final PasswordEncoder passwordEncoder;

    public DevDataSeeder(UserRepository userRepository,
                         DepartmentRepository departmentRepository,
                         SubjectRepository subjectRepository,
                         TopicRepository topicRepository,
                         TagRepository tagRepository,
                         ResourceRepository resourceRepository,
                         ReviewRepository reviewRepository,
                         ReviewHelpfulVoteRepository helpfulVoteRepository,
                         DoubtRepository doubtRepository,
                         FocusSessionRepository focusSessionRepository,
                         BookmarkRepository bookmarkRepository,
                         ReviewService reviewService,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
        this.resourceRepository = resourceRepository;
        this.reviewRepository = reviewRepository;
        this.helpfulVoteRepository = helpfulVoteRepository;
        this.doubtRepository = doubtRepository;
        this.focusSessionRepository = focusSessionRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.reviewService = reviewService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking DevDataSeeder initialization...");

        // Ensure upload directory exists
        Path uploadsDir = Paths.get("uploads");
        try {
            if (!Files.exists(uploadsDir)) {
                Files.createDirectories(uploadsDir);
            }
        } catch (IOException e) {
            log.error("Failed to create uploads directory", e);
        }

        // 1. Seed Taxonomy
        Map<String, Department> depts = seedDepartments();
        Map<String, Subject> subjects = seedSubjects(depts);
        Map<String, Tag> tags = seedTags();

        // 2. Seed Demo Users
        Map<String, User> users = seedDemoUsers(depts);

        // 3. Generate 8-10 Original Note PDFs and Seed Resources
        List<Resource> pdfResources = seedOriginalPdfResources(users.get("admin"), depts, subjects, tags);

        // 4. Seed 25-30 Curated External Catalog Entries
        List<Resource> extResources = seedExternalCatalogResources(users.get("admin"), depts, subjects, tags);

        // 5. Seed Reviews & Compute Bayesian Reliability
        seedDemoReviews(users, pdfResources, extResources);

        // 6. Seed Focus Sessions & Streak History
        seedDemoFocusSessions(users, subjects);

        // 7. Seed Doubts for Forum
        seedDemoDoubts(users, subjects, tags);

        log.info("DevDataSeeder completed successfully. Total resources: {}, reviews: {}",
                resourceRepository.count(), reviewRepository.count());
    }

    private Map<String, Department> seedDepartments() {
        Map<String, Department> map = new HashMap<>();
        String[][] list = {
                {"Computer Science & Engineering", "CSE"},
                {"Information Technology", "IT"},
                {"Electronics & Communication", "ECE"},
                {"Mechanical Engineering", "ME"},
                {"Civil Engineering", "CE"}
        };
        for (String[] d : list) {
            Department dept = departmentRepository.findAll().stream()
                    .filter(x -> x.getCode().equalsIgnoreCase(d[1]))
                    .findFirst()
                    .orElseGet(() -> {
                        Department nd = new Department();
                        nd.setName(d[0]);
                        nd.setCode(d[1]);
                        nd.setActive(true);
                        return departmentRepository.save(nd);
                    });
            map.put(d[1], dept);
        }
        return map;
    }

    private Map<String, Subject> seedSubjects(Map<String, Department> depts) {
        Map<String, Subject> map = new HashMap<>();
        Department cse = depts.get("CSE");
        Department it = depts.get("IT");
        Department ece = depts.get("ECE");
        Department me = depts.get("ME");
        Department ce = depts.get("CE");

        Object[][] list = {
                // Computer Science & Engineering
                {"Data Structures & Algorithms", "CS201", 3, cse},
                {"Database Management Systems", "CS301", 4, cse},
                {"Operating Systems", "CS302", 4, cse},
                {"Computer Networks", "CS303", 5, cse},
                {"Java & Object Oriented Programming", "CS202", 3, cse},
                {"Computer Organization & Architecture", "COA301", 4, cse},
                {"Software Engineering & Agile System Design", "SE401", 5, cse},
                {"Competitive Programming & Problem Solving", "CS401", 6, cse},
                {"Aptitude, Quant & Placements Mastery", "GEN101", 6, cse},

                // Information Technology
                {"Full Stack Web Development", "IT301", 5, it},
                {"Cloud Computing & DevOps", "IT302", 6, it},
                {"Cyber Security & Cryptography", "IT401", 7, it},
                {"Machine Learning & Data Mining", "IT303", 5, it},

                // Electronics & Communication
                {"Digital Signal Processing", "ECE301", 5, ece},
                {"VLSI System Design", "ECE302", 6, ece},
                {"Microprocessors & Microcontrollers", "ECE201", 4, ece},
                {"Signals and Systems", "ECE202", 3, ece},

                // Mechanical Engineering
                {"Engineering Thermodynamics", "ME201", 3, me},
                {"Fluid Mechanics & Machinery", "ME202", 4, me},
                {"Theory of Machines", "ME301", 5, me},
                {"Manufacturing Technology", "ME302", 5, me},

                // Civil Engineering
                {"Structural Analysis", "CE301", 5, ce},
                {"Geotechnical Engineering", "CE302", 6, ce},
                {"Surveying & Geomatics", "CE201", 3, ce},
                {"Hydraulics & Water Resources", "CE202", 4, ce}
        };

        for (Object[] s : list) {
            String name = (String) s[0];
            String code = (String) s[1];
            Integer sem = (Integer) s[2];
            Department dept = (Department) s[3];

            Subject sub = subjectRepository.findAll().stream()
                    .filter(x -> x.getCode().equalsIgnoreCase(code))
                    .findFirst()
                    .orElseGet(() -> {
                        Subject ns = new Subject();
                        ns.setName(name);
                        ns.setCode(code);
                        ns.setSemester(sem);
                        ns.setDepartment(dept);
                        ns.setActive(true);
                        return subjectRepository.save(ns);
                    });
            map.put(code, sub);
        }
        return map;
    }

    private Map<String, Tag> seedTags() {
        Map<String, Tag> map = new HashMap<>();
        String[] list = {"dsa", "trees", "graphs", "dbms", "sql", "normalization", "os", "threads",
                "cn", "tcp-ip", "java", "react", "fullstack", "placements", "interview", "leetcode",
                "competitive-programming", "gate", "aptitude", "coa", "software-engineering", "system-design"};
        for (String t : list) {
            Tag tag = tagRepository.findByNameIgnoreCase(t)
                    .orElseGet(() -> tagRepository.save(new Tag(t)));
            map.put(t, tag);
        }
        return map;
    }

    private Map<String, User> seedDemoUsers(Map<String, Department> depts) {
        Map<String, User> map = new HashMap<>();

        // Admin
        User admin = userRepository.findByEmail("admin@studyhub.edu").orElseGet(() -> {
            User u = new User();
            u.setEmail("admin@studyhub.edu");
            u.setPasswordHash(passwordEncoder.encode("admin123"));
            u.setFullName("Platform Administrator");
            u.setRole(Role.ADMIN);
            u.setStatus(UserStatus.ACTIVE);
            u.setDepartment(depts.get("CSE"));
            u.setSemester(8);
            return userRepository.save(u);
        });
        map.put("admin", admin);

        // Students
        Object[][] students = {
                {"alex@studyhub.edu", "Alex Rivera", "CSE", 5, "placements,competitive coding", ContentLevel.ADVANCED, ContentLanguage.ENGLISH},
                {"priya@studyhub.edu", "Priya Sharma", "CSE", 3, "semester exams,projects", ContentLevel.INTERMEDIATE, ContentLanguage.HINDI},
                {"rahul@studyhub.edu", "Rahul Verma", "IT", 4, "placements,projects", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH},
                {"sneha@studyhub.edu", "Sneha Patel", "ECE", 6, "higher studies,semester exams", ContentLevel.ADVANCED, ContentLanguage.ENGLISH},
                {"ananya@studyhub.edu", "Ananya Gupta", "CSE", 2, "semester exams,competitive coding", ContentLevel.BEGINNER, ContentLanguage.HINDI}
        };

        for (Object[] s : students) {
            String email = (String) s[0];
            String name = (String) s[1];
            String deptCode = (String) s[2];
            Integer sem = (Integer) s[3];
            String goals = (String) s[4];
            ContentLevel level = (ContentLevel) s[5];
            ContentLanguage lang = (ContentLanguage) s[6];

            User u = userRepository.findByEmail(email).orElseGet(() -> {
                User nu = new User();
                nu.setEmail(email);
                nu.setPasswordHash(passwordEncoder.encode("student123"));
                nu.setFullName(name);
                nu.setRole(Role.STUDENT);
                nu.setStatus(UserStatus.ACTIVE);
                nu.setDepartment(depts.get(deptCode));
                nu.setSemester(sem);
                nu.setGoals(goals);
                nu.setLevel(level);
                nu.setPreferredLanguage(lang);
                return userRepository.save(nu);
            });
            map.put(email.split("@")[0], u);
        }

        return map;
    }

    private List<Resource> seedOriginalPdfResources(User admin, Map<String, Department> depts,
                                                   Map<String, Subject> subs, Map<String, Tag> tags) {
        List<Resource> created = new ArrayList<>();

        Object[][] pdfDefs = {
                {"DSA Trees & Graphs Quick Revision Cheatsheet", "dsa_trees_graphs_cheatsheet.pdf", "CS201", 3,
                        "Comprehensive handwritten-style review sheet for Binary Search Trees, AVL rotations, DFS/BFS traversal, Dijkstra shortest path, and Disjoint Set Union (DSU).",
                        "Semester exams and technical coding rounds", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("dsa", "trees", "graphs", "interview")},

                {"DBMS Normalization & SQL Query Optimization Primer", "dbms_normalization_sql_guide.pdf", "CS301", 4,
                        "Step-by-step 1NF to BCNF decomposition guide with functional dependency closures, indexing strategies (B-Trees vs Hash), and ACID properties breakdown.",
                        "Database exams and backend system design", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("dbms", "sql", "normalization", "placements")},

                {"SQL Advanced Joins, Indexing & Execution Plans Cheatsheet", "sql_advanced_joins_indexing_guide.pdf", "CS301", 4,
                        "Deep dive into Inner/Outer/Cross Joins, Clustered vs Non-Clustered Indexes, Query Optimizer execution plans, and Window functions.",
                        "Database administrators and data engineering rounds", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("dbms", "sql", "placements")},

                {"OS Process Scheduling & Deadlock Prevention Master Notes", "os_process_scheduling_deadlocks.pdf", "CS302", 4,
                        "Concise reference for CPU scheduling algorithms (Round Robin, SRTF), Semaphore synchronization, Banker's algorithm, and Virtual Memory paging.",
                        "OS midterms and GATE CS preparation", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("os", "threads", "gate")},

                {"Computer Networks TCP/IP vs OSI Protocol Stack Summary", "cn_tcp_ip_osi_model_summary.pdf", "CS303", 5,
                        "Detailed comparison table of OSI 7 layers vs TCP/IP 4 layers, Subnetting formula cheatsheet, TCP 3-way handshake, and DNS/HTTP flow diagrams.",
                        "Networking viva and campus placements", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("cn", "tcp-ip", "interview")},

                {"Java Multithreading & Memory Model Handbook", "java_concurrency_multithreading_notes.pdf", "CS202", 3,
                        "Complete guide covering Thread states, volatile vs synchronized, java.util.concurrent ExecutorService, ReentrantLock, and Garbage Collection mechanics.",
                        "Java developer interview rounds", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("java", "threads", "placements")},

                {"Computer Organization Pipelining & Cache Architecture Guide", "coa_pipelining_cache_architecture.pdf", "COA301", 4,
                        "Instruction pipelining hazards (Data, Structural, Control), Direct vs Set Associative Cache mapping, and Booth's Multiplication algorithm.",
                        "Computer Organization midterms & GATE CS", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("coa", "gate")},

                {"Software Engineering Agile & Microservices System Design Notes", "software_engineering_system_design.pdf", "SE401", 5,
                        "Overview of SDLC methodologies (Agile Scrum, Kanban), UML diagrams, SOLID principles, and microservices design patterns (API Gateway, Circuit Breaker).",
                        "Software engineering vivas and design interviews", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("software-engineering", "system-design")},

                {"Full Stack React Hooks & Modern State Management Guide", "web_dev_react_hooks_state_guide.pdf", "IT301", 5,
                        "Practical patterns for useState, useEffect dependencies, custom hooks, Context API performance pitfalls, and TanStack Query caching.",
                        "Frontend development projects & interviews", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("react", "fullstack", "interview")},

                {"Placements Quantitative Aptitude & Fast Math Formula Sheet", "placements_aptitude_quant_formula_sheet.pdf", "GEN101", 6,
                        "Essential mental math tricks, Time & Work formulas, Permutations & Combinations shortcuts, Probability distributions, and Data Interpretation rules.",
                        "Campus placements aptitude test round 1", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("aptitude", "placements", "interview")},

                {"Competitive Programming Template & Math Theorems Sheet", "competitive_programming_template_cheatsheet.pdf", "CS401", 6,
                        "High-speed C++ template with Fast I/O, Modular Inverse, Binary Exponentiation, Sieve of Eratosthenes, Segment Tree implementation, and Bitwise tricks.",
                        "CodeChef and Codeforces online contests", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("competitive-programming", "leetcode", "dsa")}
        };

        for (Object[] def : pdfDefs) {
            String title = (String) def[0];
            String filename = (String) def[1];
            String subCode = (String) def[2];
            Integer sem = (Integer) def[3];
            String desc = (String) def[4];
            String bestFor = (String) def[5];
            ContentLevel level = (ContentLevel) def[6];
            ContentLanguage lang = (ContentLanguage) def[7];
            @SuppressWarnings("unchecked")
            Set<String> tagKeys = (Set<String>) def[8];

            Path filePath = Paths.get("uploads", filename);
            generateOriginalPdfFile(filePath, title, desc);

            Resource resource = resourceRepository.findAll().stream()
                    .filter(r -> r.getTitle().equalsIgnoreCase(title))
                    .findFirst()
                    .orElseGet(() -> {
                        Resource r = new Resource();
                        r.setTitle(title);
                        r.setDescription(desc);
                        r.setType(ResourceType.NOTE);
                        r.setSourcePlatform(SourcePlatform.STUDYHUB);
                        r.setLevel(level);
                        r.setLanguage(lang);
                        r.setFree(true);
                        r.setBestFor(bestFor);
                        r.setFilePath(filePath.toString().replace('\\', '/'));
                        r.setFileMimeType("application/pdf");
                        r.setFileSizeBytes(new File(filePath.toString()).length());
                        r.setDepartment(depts.get("CSE"));
                        r.setSubject(subs.get(subCode));
                        r.setSemester(sem);
                        r.setUploader(admin);
                        r.setStatus(ResourceStatus.APPROVED);
                        r.setVerified(true);
                        r.setVerifiedBy("Academic Quality Board");
                        r.setVerifiedAt(Instant.now().minus(14, ChronoUnit.DAYS));
                        r.setReliabilityScore(4.5);
                        r.setReliabilityLabel("Highly reliable");

                        Set<Tag> resTags = new HashSet<>();
                        for (String tk : tagKeys) {
                            if (tags.containsKey(tk)) resTags.add(tags.get(tk));
                        }
                        r.setTags(resTags);
                        return resourceRepository.save(r);
                    });

            created.add(resource);
        }

        return created;
    }

    private void generateOriginalPdfFile(Path path, String title, String summary) {
        if (Files.exists(path)) return;
        try (PDDocument doc = new PDDocument()) {
            // Page 1: Title & Executive Summary
            PDPage page1 = new PDPage();
            doc.addPage(page1);
            try (PDPageContentStream stream = new PDPageContentStream(doc, page1)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                stream.newLineAtOffset(50, 740);
                stream.showText("StudyHub Original Academic Notes");
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 11);
                stream.newLineAtOffset(50, 715);
                stream.showText("License: Own Work (StudyHub Open Educational Resource — CC-BY-NC 4.0)");
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                stream.newLineAtOffset(50, 670);
                String safeTitle = title.length() > 55 ? title.substring(0, 52) + "..." : title;
                stream.showText(safeTitle);
                stream.endText();

                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                stream.newLineAtOffset(50, 630);
                stream.setLeading(16f);

                stream.showText("Section 1: Overview & Learning Objectives");
                stream.newLine();
                stream.showText("- Authored directly for students preparing for campus placements and semester exams.");
                stream.newLine();
                stream.showText("- Core theoretical principles and mathematical derivations verified by faculty.");
                stream.newLine();
                stream.showText("- Practical code patterns, algorithm complexities (Time/Space Big-O), and edge cases.");
                stream.newLine();
                stream.showText("- High-yield interview questions with standard solutions and analysis.");
                stream.newLine();
                stream.newLine();
                stream.showText("Summary:");
                stream.newLine();
                String safeSummary = summary.length() > 80 ? summary.substring(0, 77) + "..." : summary;
                stream.showText(safeSummary);
                stream.newLine();
                stream.newLine();
                stream.showText("StudyHub Academic Integrity: Verified Original Study Material.");
                stream.endText();
            }

            // Page 2: Core Concepts & Deep Dive
            PDPage page2 = new PDPage();
            doc.addPage(page2);
            try (PDPageContentStream stream2 = new PDPageContentStream(doc, page2)) {
                stream2.beginText();
                stream2.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                stream2.newLineAtOffset(50, 740);
                stream2.showText("Section 2: Deep Dive & Architectural Principles");
                stream2.endText();

                stream2.beginText();
                stream2.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                stream2.newLineAtOffset(50, 700);
                stream2.setLeading(16f);
                stream2.showText("1. Fundamental Foundations & Definitions:");
                stream2.newLine();
                stream2.showText("   - Mathematical models, invariant properties, and state transition semantics.");
                stream2.newLine();
                stream2.showText("   - Trade-offs between computational time complexity and memory footprints.");
                stream2.newLine();
                stream2.showText("2. Implementation Guidelines & Industry Best Practices:");
                stream2.newLine();
                stream2.showText("   - Separation of concerns, concurrency safety, and scalable data structures.");
                stream2.newLine();
                stream2.showText("   - Robust error handling, boundary validation, and unit testing paradigms.");
                stream2.endText();
            }

            // Page 3: Placements & Viva Practice Questions
            PDPage page3 = new PDPage();
            doc.addPage(page3);
            try (PDPageContentStream stream3 = new PDPageContentStream(doc, page3)) {
                stream3.beginText();
                stream3.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                stream3.newLineAtOffset(50, 740);
                stream3.showText("Section 3: Placement & Examination Review Problems");
                stream3.endText();

                stream3.beginText();
                stream3.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                stream3.newLineAtOffset(50, 700);
                stream3.setLeading(16f);
                stream3.showText("Q1: Analyze the best, worst, and amortized complexity bounds.");
                stream3.newLine();
                stream3.showText("    Solution summary: Leverage master theorem and recurrence tree techniques.");
                stream3.newLine();
                stream3.newLine();
                stream3.showText("Q2: How do ACID transaction isolation levels mitigate race conditions?");
                stream3.newLine();
                stream3.showText("    Solution summary: Two-phase locking, MVCC snapshots, and serializability.");
                stream3.newLine();
                stream3.newLine();
                stream3.showText("End of Study Guide — Authored for StudyHub Open Academic Archive");
                stream3.endText();
            }

            doc.save(path.toFile());
            log.info("Generated 3-page original study PDF: {}", path);
        } catch (Exception e) {
            log.warn("Could not generate PDF with PDFBox: {}. Writing fallback text byte stream.", e.getMessage());
            try {
                Files.writeString(path, "%PDF-1.4\n1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n" +
                        "2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n" +
                        "3 0 obj<</Type/Page/MediaBox[0 0 612 792]/Parent 2 0 R/Contents 4 0 R>>endobj\n" +
                        "4 0 obj<</Length 120>>stream\nBT /F1 14 Tf 50 700 Td (" + title.replace("(", "").replace(")", "") + ") Tj ET\nendstream\nendobj\nxref\n0 5\n0000000000 65535 f\n0000000009 00000 n\n0000000058 00000 n\n0000000115 00000 n\n0000000210 00000 n\ntrailer<</Size 5/Root 1 0 R>>\nstartxref\n380\n%%EOF\n");
            } catch (IOException ignored) {}
        }
    }

    private List<Resource> seedExternalCatalogResources(User admin, Map<String, Department> depts,
                                                       Map<String, Subject> subs, Map<String, Tag> tags) {
        List<Resource> list = new ArrayList<>();

        Object[][] catalog = {
                // YouTube Courses
                {"MIT 6.006: Introduction to Algorithms (Erik Demaine)", SourcePlatform.MIT_OCW, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PLUl4u3cNGP61Oq3tWYp6V_F-5jb5L2iHb", "CS201", 3,
                        "Complete MIT undergraduate lecture series on asymptotic analysis, divide and conquer, dynamic programming, and graphs.",
                        "Deep conceptual algorithms mastery", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("dsa", "graphs", "interview")},

                {"Abdul Bari Algorithms Course Full Playlist", SourcePlatform.YOUTUBE, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PLDN4rrl48XKpZkf03iYFl-O29szjTrs_O", "CS201", 3,
                        "World renowned step-by-step whiteboard explanations of recursion, greedy approach, backtracking, and dynamic programming.",
                        "Semester exam prep and visual algorithm learners", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("dsa", "interview")},

                {"Striver A2Z DSA Course & Sheet", SourcePlatform.YOUTUBE, ResourceType.EXTERNAL_LINK,
                        "https://www.youtube.com/playlist?list=PLgUwDviBIf0oF6QL8m22w1hIDC1vJ_BHz", "CS201", 3,
                        "Comprehensive 450+ question roadmap starting from arrays to complex graph DP with video walkthroughs.",
                        "FAANG and tier-1 product campus placements", ContentLevel.ADVANCED, ContentLanguage.HINDI,
                        Set.of("dsa", "placements", "leetcode")},

                {"Love Babbar Complete C++ & DSA Bootcamp", SourcePlatform.YOUTUBE, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PLDzeHZWIZsTryvtXdMr6rPh4IDexB5NIA", "CS201", 3,
                        "Zero-to-hero comprehensive DSA in C++ including pointers, linked lists, trees, tries and graph algorithms.",
                        "Beginners starting DSA from scratch", ContentLevel.BEGINNER, ContentLanguage.HINDI,
                        Set.of("dsa", "placements")},

                {"Gate Smashers Operating Systems Full Course", SourcePlatform.YOUTUBE, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PLxCzCOWd7aiGz9donHRrE9I3Mwn6XdPvL", "CS302", 4,
                        "Clear and concise animated explanations of CPU scheduling, Memory Management, Disk Scheduling, and Deadlocks.",
                        "University semester exams & GATE CS", ContentLevel.BEGINNER, ContentLanguage.HINDI,
                        Set.of("os", "gate")},

                {"Gate Smashers Computer Networks Complete Playlist", SourcePlatform.YOUTUBE, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PLxCzCOWd7aiGFBD2-2joCpWOLUrDLvVV_", "CS303", 5,
                        "Top-rated playlist breaking down IP addressing, Subnetting, Routing algorithms, and Application layer protocols.",
                        "Computer Networks exams & placement vivas", ContentLevel.BEGINNER, ContentLanguage.HINDI,
                        Set.of("cn", "tcp-ip", "gate")},

                {"Knowledge Gate DBMS Full Course (Sanchit Jain)", SourcePlatform.YOUTUBE, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PLmXKhU9FNesR1rSES7oLdJaNFgmuj0SYV", "CS301", 4,
                        "In-depth academic walkthrough of Relational Algebra, SQL queries, Normal Forms, Indexing and Transactions.",
                        "Semester exam revision & conceptual clarity", ContentLevel.INTERMEDIATE, ContentLanguage.HINDI,
                        Set.of("dbms", "sql", "normalization")},

                // PW / PW Skills
                {"PhysicsWallah GATE CS - Operating Systems Crash Course", SourcePlatform.PW, ResourceType.VIDEO,
                        "https://www.youtube.com/playlist?list=PL3ym4Uyd0u5zR6dZ4F5YxLz9j8K4oY0lP", "CS302", 4,
                        "High yield problem-solving sessions for CPU scheduling, paging and synchronization by PW Gate Wallah.",
                        "GATE aspirants and fast semester revision", ContentLevel.INTERMEDIATE, ContentLanguage.HINDI,
                        Set.of("os", "gate", "placements")},

                {"PW Skills Full Stack Java & React Development", SourcePlatform.PW, ResourceType.EXTERNAL_LINK,
                        "https://pwskills.com/course/full-stack-web-development/", "IT301", 5,
                        "End-to-end full stack web architecture course covering Java Spring Boot, REST APIs, React frontend, and SQL.",
                        "Building production capstone projects", ContentLevel.INTERMEDIATE, ContentLanguage.HINDI,
                        Set.of("fullstack", "react", "java")},

                // CodeWithHarry
                {"CodeWithHarry Sigma Web Development Course", SourcePlatform.CODEWITHHARRY, ResourceType.EXTERNAL_LINK,
                        "https://www.codewithharry.com/tutorial/html-home/", "IT301", 5,
                        "Modern web development tutorials with practical exercises in HTML, CSS, JavaScript, React, and Node.js.",
                        "College students learning web dev from zero", ContentLevel.BEGINNER, ContentLanguage.HINDI,
                        Set.of("fullstack", "react")},

                {"CodeWithHarry Java Tutorials for Beginners", SourcePlatform.CODEWITHHARRY, ResourceType.VIDEO,
                        "https://www.codewithharry.com/videos/java-tutorials-for-beginners-1/", "CS202", 3,
                        "Hands-on Java fundamentals covering OOPs concepts, Collections Framework, Exception Handling and Streams.",
                        "Semester 3 Java course and lab exams", ContentLevel.BEGINNER, ContentLanguage.HINDI,
                        Set.of("java", "interview")},

                // LeetCode
                {"LeetCode Top Interview 150 Curated Problem Set", SourcePlatform.LEETCODE, ResourceType.EXTERNAL_LINK,
                        "https://leetcode.com/studyplan/top-interview-150/", "CS201", 3,
                        "The definitive 150 problems covering all core patterns required for top technology software engineering interviews.",
                        "Placement coding rounds and online assessments", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("leetcode", "dsa", "placements", "interview")},

                {"LeetCode 75 Essential Study Plan", SourcePlatform.LEETCODE, ResourceType.EXTERNAL_LINK,
                        "https://leetcode.com/studyplan/leetcode-75/", "CS201", 3,
                        "Balanced 75 problem collection focusing on Two Pointers, Sliding Window, Binary Search, Trees, and Dynamic Programming.",
                        "Fast 4-week interview preparation", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("leetcode", "dsa", "interview")},

                // CodeChef
                {"CodeChef DSA Learning Series (Contest Tracks)", SourcePlatform.CODECHEF, ResourceType.EXTERNAL_LINK,
                        "https://www.codechef.com/practice/data-structures-and-algorithms", "CS401", 6,
                        "Structured competitive programming tracks covering Complexity Analysis, Linear DS, Math, and Graph Theory.",
                        "Competitive programmers rating boost (1star to 4star)", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("competitive-programming", "dsa")},

                // GeeksforGeeks
                {"GeeksforGeeks Top 50 DBMS Interview Questions", SourcePlatform.GFG, ResourceType.EXTERNAL_LINK,
                        "https://www.geeksforgeeks.org/dbms-interview-questions/", "CS301", 4,
                        "Curated list of frequently asked DBMS technical interview questions with comprehensive answers and SQL examples.",
                        "Technical interview preparation", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("dbms", "sql", "placements", "interview")},

                {"GeeksforGeeks Top 50 Operating System Interview Questions", SourcePlatform.GFG, ResourceType.EXTERNAL_LINK,
                        "https://www.geeksforgeeks.org/operating-systems-interview-questions/", "CS302", 4,
                        "Essential core OS interview questions on Virtual Memory, Deadlocks, Paging, and IPC.",
                        "Campus placements viva and round 2 interviews", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("os", "interview", "placements")},

                {"GeeksforGeeks Love Babbar DSA Sheet Practice", SourcePlatform.GFG, ResourceType.EXTERNAL_LINK,
                        "https://www.geeksforgeeks.org/dsa-sheet-by-love-babbar/", "CS201", 3,
                        "Topic-wise curated list of 450 coding questions with GeeksforGeeks practice problem IDE links.",
                        "Daily coding practice for placement season", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("dsa", "placements")},

                // NPTEL
                {"NPTEL Programming, Data Structures & Algorithms Using Python (IIT Madras)", SourcePlatform.NPTEL, ResourceType.EXTERNAL_LINK,
                        "https://nptel.ac.in/courses/106106145", "CS201", 3,
                        "Rigorous academic course taught by Prof. Madhavan Mukund on algorithmic thinking, data structures, and Python implementations.",
                        "Academic credits and deep algorithmic foundations", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("dsa", "gate")},

                {"NPTEL Database Management System (IIT Kharagpur)", SourcePlatform.NPTEL, ResourceType.EXTERNAL_LINK,
                        "https://nptel.ac.in/courses/106105175", "CS301", 4,
                        "Comprehensive university-level course by Prof. Partha Pratim Das on relational model, SQL, indexing, and query evaluation.",
                        "University semester syllabus and GATE", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("dbms", "sql", "gate")},

                // MIT OCW
                {"MIT OCW 6.0001 Introduction to CS & Programming in Python", SourcePlatform.MIT_OCW, ResourceType.EXTERNAL_LINK,
                        "https://ocw.mit.edu/courses/6-0001-introduction-to-computer-science-and-programming-in-python-fall-2016/", "CS201", 3,
                        "Taught by Prof. Ana Bell and Prof. Eric Grimson, covering computational problem-solving, algorithms, and data abstraction.",
                        "Foundational programming for first/second years", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("dsa", "interview")},

                {"MIT OCW 6.042J Mathematics for Computer Science", SourcePlatform.MIT_OCW, ResourceType.EXTERNAL_LINK,
                        "https://ocw.mit.edu/courses/6-042j-mathematics-for-computer-science-spring-2015/", "CS401", 6,
                        "Discrete mathematics, graph theory, proofs, number theory, and probability for computer scientists.",
                        "Competitive programmers & algorithm researchers", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("competitive-programming", "gate")},

                // Roadmaps & Community
                {"Roadmap.sh Frontend Developer Roadmap 2026", SourcePlatform.OTHER, ResourceType.EXTERNAL_LINK,
                        "https://roadmap.sh/frontend", "IT301", 5,
                        "Step by step guide to becoming a modern frontend developer with resources on HTML, CSS, JavaScript, React, and tooling.",
                        "Frontend career roadmapping", ContentLevel.BEGINNER, ContentLanguage.ENGLISH,
                        Set.of("fullstack", "react")},

                {"Roadmap.sh Backend Developer Roadmap 2026", SourcePlatform.OTHER, ResourceType.EXTERNAL_LINK,
                        "https://roadmap.sh/backend", "IT301", 5,
                        "Structured path for backend engineers covering databases, caching, message queues, APIs, and microservices.",
                        "Backend career roadmapping", ContentLevel.INTERMEDIATE, ContentLanguage.ENGLISH,
                        Set.of("fullstack", "java", "sql")},

                {"NeetCode 150 Blind LeetCode Practice Tracker", SourcePlatform.OTHER, ResourceType.EXTERNAL_LINK,
                        "https://neetcode.io/practice", "CS201", 3,
                        "Free interactive practice sheet with pattern categorization, video solutions, and complexity summaries.",
                        "Product company placements (Google, Amazon, Microsoft)", ContentLevel.ADVANCED, ContentLanguage.ENGLISH,
                        Set.of("leetcode", "dsa", "placements")}
        };

        for (Object[] def : catalog) {
            String title = (String) def[0];
            SourcePlatform platform = (SourcePlatform) def[1];
            ResourceType type = (ResourceType) def[2];
            String url = (String) def[3];
            String subCode = (String) def[4];
            Integer sem = (Integer) def[5];
            String desc = (String) def[6];
            String bestFor = (String) def[7];
            ContentLevel level = (ContentLevel) def[8];
            ContentLanguage lang = (ContentLanguage) def[9];
            @SuppressWarnings("unchecked")
            Set<String> tagKeys = (Set<String>) def[10];

            Resource resource = resourceRepository.findAll().stream()
                    .filter(r -> r.getTitle().equalsIgnoreCase(title))
                    .findFirst()
                    .orElseGet(() -> {
                        Resource r = new Resource();
                        r.setTitle(title);
                        r.setDescription(desc);
                        r.setType(type);
                        r.setSourcePlatform(platform);
                        r.setExternalUrl(url);
                        r.setLevel(level);
                        r.setLanguage(lang);
                        r.setFree(true);
                        r.setBestFor(bestFor);
                        r.setDepartment(depts.get("CSE"));
                        r.setSubject(subs.get(subCode));
                        r.setSemester(sem);
                        r.setUploader(admin);
                        r.setStatus(ResourceStatus.APPROVED);
                        r.setVerified(true);
                        r.setVerifiedBy("StudyHub Content Team");
                        r.setVerifiedAt(Instant.now().minus(20, ChronoUnit.DAYS));
                        r.setReliabilityScore(4.6);
                        r.setReliabilityLabel("Highly reliable");

                        Set<Tag> resTags = new HashSet<>();
                        for (String tk : tagKeys) {
                            if (tags.containsKey(tk)) resTags.add(tags.get(tk));
                        }
                        r.setTags(resTags);
                        return resourceRepository.save(r);
                    });

            list.add(resource);
        }

        return list;
    }

    private void seedDemoReviews(Map<String, User> users, List<Resource> pdfs, List<Resource> ext) {
        List<Resource> all = new ArrayList<>(pdfs);
        all.addAll(ext);

        User alex = users.get("alex");
        User priya = users.get("priya");
        User rahul = users.get("rahul");
        User sneha = users.get("sneha");
        User ananya = users.get("ananya");

        if (alex == null || priya == null || rahul == null) return;

        User[] reviewUsers = {alex, priya, rahul, sneha, ananya};
        String[] reviewTexts = {
                "Absolutely stellar resource! The visual explanations and clean code examples saved me during placement prep.",
                "Very well structured and easy to follow. Covered every topic asked in our mid-semester examination.",
                "Clear, concise and to the point. The complexity analysis helped me crack my online assessment round.",
                "Highly recommended for anyone looking to build rock solid conceptual clarity in this subject.",
                "Good material overall. A few advanced topics could have more examples, but great for revision."
        };

        for (int i = 0; i < all.size(); i++) {
            Resource r = all.get(i);
            int numReviews = (i % 3 == 0) ? 4 : (i % 3 == 1) ? 2 : 1;

            for (int uIdx = 0; uIdx < numReviews; uIdx++) {
                User u = reviewUsers[uIdx];
                if (r.getUploader() != null && r.getUploader().getId().equals(u.getId())) continue;

                if (!reviewRepository.existsByResourceIdAndUserId(r.getId(), u.getId())) {
                    int rating = (uIdx == 0 || uIdx == 1) ? 5 : (uIdx == 2) ? 4 : 3;
                    Review review = new Review(r, u, rating, reviewTexts[uIdx % reviewTexts.length]);
                    review.setHelpfulCount(uIdx * 2 + 1);
                    Review savedReview = reviewRepository.save(review);

                    // Add helpful votes from other users
                    for (int vIdx = 0; vIdx < reviewUsers.length; vIdx++) {
                        User voter = reviewUsers[vIdx];
                        if (!voter.getId().equals(u.getId()) && vIdx < 2) {
                            if (!helpfulVoteRepository.existsByReviewIdAndUserId(savedReview.getId(), voter.getId())) {
                                helpfulVoteRepository.save(new ReviewHelpfulVote(savedReview, voter));
                            }
                        }
                    }
                }
            }

            // Recalculate reliability score
            reviewService.updateResourceReliability(r);

            // Add bookmarks for users to power recommendations
            if (i < 3 && !bookmarkRepository.existsByUserIdAndResourceId(alex.getId(), r.getId())) {
                bookmarkRepository.save(new Bookmark(alex, r));
            }
            if (i >= 2 && i < 5 && !bookmarkRepository.existsByUserIdAndResourceId(priya.getId(), r.getId())) {
                bookmarkRepository.save(new Bookmark(priya, r));
            }
        }
    }

    private void seedDemoFocusSessions(Map<String, User> users, Map<String, Subject> subs) {
        User alex = users.get("alex");
        User priya = users.get("priya");
        User rahul = users.get("rahul");

        if (alex == null || priya == null || rahul == null) return;

        Subject dsa = subs.get("CS201");
        Subject dbms = subs.get("CS301");
        Subject os = subs.get("CS302");

        // Seed consecutive daily focus sessions for Alex (7-day streak)
        for (int day = 0; day < 7; day++) {
            Instant start = Instant.now().minus(day, ChronoUnit.DAYS).minus(45, ChronoUnit.MINUTES);
            FocusSession s = new FocusSession(alex, (day % 2 == 0 ? dsa : os), FocusMode.POMODORO, 45);
            s.setStartedAt(start);
            s.setEndedAt(start.plus(45, ChronoUnit.MINUTES));
            s.setActualMinutes(45);
            s.setStatus(FocusStatus.COMPLETED);
            focusSessionRepository.save(s);
        }

        // Seed 4-day streak for Priya
        for (int day = 0; day < 4; day++) {
            Instant start = Instant.now().minus(day, ChronoUnit.DAYS).minus(30, ChronoUnit.MINUTES);
            FocusSession s = new FocusSession(priya, dbms, FocusMode.POMODORO, 30);
            s.setStartedAt(start);
            s.setEndedAt(start.plus(30, ChronoUnit.MINUTES));
            s.setActualMinutes(30);
            s.setStatus(FocusStatus.COMPLETED);
            focusSessionRepository.save(s);
        }

        // Seed 2-day streak for Rahul
        for (int day = 0; day < 2; day++) {
            Instant start = Instant.now().minus(day, ChronoUnit.DAYS).minus(60, ChronoUnit.MINUTES);
            FocusSession s = new FocusSession(rahul, dsa, FocusMode.TIMER, 60);
            s.setStartedAt(start);
            s.setEndedAt(start.plus(60, ChronoUnit.MINUTES));
            s.setActualMinutes(60);
            s.setStatus(FocusStatus.COMPLETED);
            focusSessionRepository.save(s);
        }
    }

    private void seedDemoDoubts(Map<String, User> users, Map<String, Subject> subs, Map<String, Tag> tags) {
        User alex = users.get("alex");
        User priya = users.get("priya");
        User rahul = users.get("rahul");

        if (alex == null || priya == null || rahul == null) return;

        Subject dsa = subs.get("CS201");
        Subject dbms = subs.get("CS301");

        if (doubtRepository.count() == 0) {
            Doubt d1 = new Doubt();
            d1.setTitle("Difference between Red-Black Trees and AVL Trees in practical systems?");
            d1.setContent("Why do standard library implementations like C++ std::map and Java TreeMap use Red-Black trees instead of AVL trees if AVL trees offer faster search?");
            d1.setSubject(dsa);
            d1.setUser(priya);
            d1.setStatus(DoubtStatus.OPEN);
            Set<Tag> d1Tags = new HashSet<>();
            if (tags.containsKey("dsa")) d1Tags.add(tags.get("dsa"));
            if (tags.containsKey("trees")) d1Tags.add(tags.get("trees"));
            d1.setTags(d1Tags);
            doubtRepository.save(d1);

            Doubt d2 = new Doubt();
            d2.setTitle("How to effectively prevent SQL injection in dynamic WHERE clauses with JPA Specifications?");
            d2.setContent("I am building an advanced search filter and want to know how Spring Data JPA Specification CriteriaBuilder avoids SQL injection compared to raw string queries.");
            d2.setSubject(dbms);
            d2.setUser(rahul);
            d2.setStatus(DoubtStatus.RESOLVED);
            Set<Tag> d2Tags = new HashSet<>();
            if (tags.containsKey("dbms")) d2Tags.add(tags.get("dbms"));
            if (tags.containsKey("sql")) d2Tags.add(tags.get("sql"));
            d2.setTags(d2Tags);
            doubtRepository.save(d2);
        }
    }
}

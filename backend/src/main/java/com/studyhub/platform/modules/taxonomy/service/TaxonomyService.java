package com.studyhub.platform.modules.taxonomy.service;

import com.studyhub.platform.common.exception.BadRequestException;
import com.studyhub.platform.common.exception.ResourceNotFoundException;
import com.studyhub.platform.modules.admin.dto.*;
import com.studyhub.platform.modules.taxonomy.dto.*;
import com.studyhub.platform.modules.taxonomy.entity.*;
import com.studyhub.platform.modules.taxonomy.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class TaxonomyService {

    private final DepartmentRepository departmentRepository;
    private final SubjectRepository subjectRepository;
    private final TopicRepository topicRepository;
    private final TagRepository tagRepository;
    private final GoalRepository goalRepository;
    private final DepartmentPlatformRepository departmentPlatformRepository;
    private final DepartmentToolRepository departmentToolRepository;

    public TaxonomyService(DepartmentRepository departmentRepository,
                           SubjectRepository subjectRepository,
                           TopicRepository topicRepository,
                           TagRepository tagRepository,
                           GoalRepository goalRepository,
                           DepartmentPlatformRepository departmentPlatformRepository,
                           DepartmentToolRepository departmentToolRepository) {
        this.departmentRepository = departmentRepository;
        this.subjectRepository = subjectRepository;
        this.topicRepository = topicRepository;
        this.tagRepository = tagRepository;
        this.goalRepository = goalRepository;
        this.departmentPlatformRepository = departmentPlatformRepository;
        this.departmentToolRepository = departmentToolRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(d -> new DepartmentDto(d.getId(), d.getName(), d.getCode(), d.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartmentsAdmin() {
        return departmentRepository.findAll().stream()
                .map(d -> new DepartmentDto(d.getId(), d.getName(), d.getCode(), d.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public DepartmentDetailDto getDepartmentDetails(Long departmentId) {
        Department dept = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", departmentId));

        List<SubjectDto> subjects = subjectRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(departmentId).stream()
                .map(s -> new SubjectDto(s.getId(), dept.getId(), dept.getName(), s.getName(), s.getCode(), s.getSemester(), s.isActive()))
                .toList();

        List<GoalDto> goals = goalRepository.findAvailableForDepartment(departmentId).stream()
                .map(g -> new GoalDto(g.getId(), g.getDepartment() != null ? g.getDepartment().getId() : null, g.getGoalKey(), g.getLabel(), g.getDescription(), g.isActive()))
                .toList();

        List<DepartmentPlatformDto> platforms = departmentPlatformRepository.findByDepartmentIdOrderByPriorityDesc(departmentId).stream()
                .map(p -> new DepartmentPlatformDto(p.getId(), dept.getId(), p.getPlatform(), p.getPriority()))
                .toList();

        List<DepartmentToolDto> tools = departmentToolRepository.findByDepartmentIdOrderByNameAsc(departmentId).stream()
                .map(t -> new DepartmentToolDto(t.getId(), dept.getId(), t.getName()))
                .toList();

        return new DepartmentDetailDto(dept.getId(), dept.getName(), dept.getCode(), dept.isActive(), subjects, goals, platforms, tools);
    }

    @Transactional(readOnly = true)
    public List<SubjectDto> getSubjects(Long departmentId, Integer semester) {
        List<Subject> subjects;
        if (departmentId != null && semester != null) {
            subjects = subjectRepository.findByDepartmentIdAndSemesterAndActiveTrueOrderByNameAsc(departmentId, semester);
        } else if (departmentId != null) {
            subjects = subjectRepository.findByDepartmentIdAndActiveTrueOrderByNameAsc(departmentId);
        } else {
            subjects = subjectRepository.findAll();
        }

        return subjects.stream()
                .map(s -> new SubjectDto(s.getId(), s.getDepartment().getId(), s.getDepartment().getName(), s.getName(), s.getCode(), s.getSemester(), s.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<GoalDto> getGoals(Long departmentId) {
        List<Goal> goals = departmentId != null
                ? goalRepository.findAvailableForDepartment(departmentId)
                : goalRepository.findAll();

        return goals.stream()
                .map(g -> new GoalDto(g.getId(), g.getDepartment() != null ? g.getDepartment().getId() : null, g.getGoalKey(), g.getLabel(), g.getDescription(), g.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentPlatformDto> getPlatforms(Long departmentId) {
        List<DepartmentPlatform> platforms = departmentId != null
                ? departmentPlatformRepository.findByDepartmentIdOrderByPriorityDesc(departmentId)
                : departmentPlatformRepository.findAll();

        return platforms.stream()
                .map(p -> new DepartmentPlatformDto(p.getId(), p.getDepartment().getId(), p.getPlatform(), p.getPriority()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DepartmentToolDto> getTools(Long departmentId) {
        List<DepartmentTool> tools = departmentId != null
                ? departmentToolRepository.findByDepartmentIdOrderByNameAsc(departmentId)
                : departmentToolRepository.findAll();

        return tools.stream()
                .map(t -> new DepartmentToolDto(t.getId(), t.getDepartment().getId(), t.getName()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TopicDto> getTopicsBySubject(Long subjectId) {
        return topicRepository.findBySubjectIdAndActiveTrueOrderByNameAsc(subjectId).stream()
                .map(t -> new TopicDto(t.getId(), t.getSubject().getId(), t.getSubject().getName(), t.getName(), t.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TagDto> getAllTags() {
        return tagRepository.findAll().stream()
                .map(t -> new TagDto(t.getId(), t.getName()))
                .toList();
    }

    @Transactional
    public Set<Tag> findOrCreateTags(Set<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<Tag> tags = new HashSet<>();
        for (String rawName : tagNames) {
            String name = rawName.trim().toLowerCase();
            if (!name.isEmpty()) {
                Tag tag = tagRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name)));
                tags.add(tag);
            }
        }
        return tags;
    }

    // =========================================================================
    // ADMIN TAXONOMY MUTATIONS
    // =========================================================================

    @Transactional
    public DepartmentDto createDepartment(TaxonomyCreateDepartmentRequest req) {
        String code = req.code().trim().toUpperCase();
        if (departmentRepository.existsByCode(code)) {
            throw new BadRequestException("Department code already exists: " + code);
        }
        Department dept = new Department(req.name().trim(), code);
        dept.setActive(true);
        Department saved = departmentRepository.save(dept);
        return new DepartmentDto(saved.getId(), saved.getName(), saved.getCode(), saved.isActive());
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, TaxonomyUpdateDepartmentRequest req) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setName(req.name().trim());
        dept.setCode(req.code().trim().toUpperCase());
        dept.setActive(req.active());
        Department saved = departmentRepository.save(dept);
        return new DepartmentDto(saved.getId(), saved.getName(), saved.getCode(), saved.isActive());
    }

    @Transactional
    public DepartmentDto toggleDepartmentStatus(Long id, boolean active) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
        dept.setActive(active);
        Department saved = departmentRepository.save(dept);
        return new DepartmentDto(saved.getId(), saved.getName(), saved.getCode(), saved.isActive());
    }

    @Transactional
    public SubjectDto createSubject(TaxonomyCreateSubjectRequest req) {
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.departmentId()));
        Subject subject = new Subject();
        subject.setDepartment(dept);
        subject.setName(req.name().trim());
        subject.setCode(req.code().trim().toUpperCase());
        subject.setSemester(req.semester());
        subject.setActive(true);
        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), dept.getId(), dept.getName(), saved.getName(), saved.getCode(), saved.getSemester(), saved.isActive());
    }

    @Transactional
    public SubjectDto updateSubject(Long id, TaxonomyUpdateSubjectRequest req) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.departmentId()));
        subject.setDepartment(dept);
        subject.setName(req.name().trim());
        subject.setCode(req.code().trim().toUpperCase());
        subject.setSemester(req.semester());
        subject.setActive(req.active());
        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), dept.getId(), dept.getName(), saved.getName(), saved.getCode(), saved.getSemester(), saved.isActive());
    }

    @Transactional
    public SubjectDto toggleSubjectStatus(Long id, boolean active) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", id));
        subject.setActive(active);
        Subject saved = subjectRepository.save(subject);
        return new SubjectDto(saved.getId(), saved.getDepartment().getId(), saved.getDepartment().getName(), saved.getName(), saved.getCode(), saved.getSemester(), saved.isActive());
    }

    @Transactional
    public TopicDto createTopic(TaxonomyCreateTopicRequest req) {
        Subject subject = subjectRepository.findById(req.subjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", req.subjectId()));
        Topic topic = new Topic(subject, req.name().trim());
        Topic saved = topicRepository.save(topic);
        return new TopicDto(saved.getId(), subject.getId(), subject.getName(), saved.getName(), saved.isActive());
    }

    @Transactional
    public GoalDto createGoal(TaxonomyCreateGoalRequest req) {
        Department dept = null;
        if (req.departmentId() != null) {
            dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.departmentId()));
        }
        Goal goal = new Goal(dept, req.goalKey().trim().toUpperCase(), req.label().trim(), req.description());
        Goal saved = goalRepository.save(goal);
        return new GoalDto(saved.getId(), dept != null ? dept.getId() : null, saved.getGoalKey(), saved.getLabel(), saved.getDescription(), saved.isActive());
    }

    @Transactional
    public GoalDto updateGoal(Long id, TaxonomyUpdateGoalRequest req) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        if (req.departmentId() != null) {
            Department dept = departmentRepository.findById(req.departmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.departmentId()));
            goal.setDepartment(dept);
        } else {
            goal.setDepartment(null);
        }
        goal.setLabel(req.label().trim());
        goal.setDescription(req.description());
        goal.setActive(req.active());
        Goal saved = goalRepository.save(goal);
        return new GoalDto(saved.getId(), saved.getDepartment() != null ? saved.getDepartment().getId() : null, saved.getGoalKey(), saved.getLabel(), saved.getDescription(), saved.isActive());
    }

    @Transactional
    public GoalDto toggleGoalStatus(Long id, boolean active) {
        Goal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal", "id", id));
        goal.setActive(active);
        Goal saved = goalRepository.save(goal);
        return new GoalDto(saved.getId(), saved.getDepartment() != null ? saved.getDepartment().getId() : null, saved.getGoalKey(), saved.getLabel(), saved.getDescription(), saved.isActive());
    }

    @Transactional
    public DepartmentPlatformDto createPlatform(TaxonomyCreatePlatformRequest req) {
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.departmentId()));
        DepartmentPlatform dp = new DepartmentPlatform(dept, req.platform(), req.priority() != null ? req.priority() : 0);
        DepartmentPlatform saved = departmentPlatformRepository.save(dp);
        return new DepartmentPlatformDto(saved.getId(), dept.getId(), saved.getPlatform(), saved.getPriority());
    }

    @Transactional
    public void deletePlatform(Long id) {
        if (!departmentPlatformRepository.existsById(id)) {
            throw new ResourceNotFoundException("DepartmentPlatform", "id", id);
        }
        departmentPlatformRepository.deleteById(id);
    }

    @Transactional
    public DepartmentToolDto createTool(TaxonomyCreateToolRequest req) {
        Department dept = departmentRepository.findById(req.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", req.departmentId()));
        DepartmentTool tool = new DepartmentTool(dept, req.name().trim());
        DepartmentTool saved = departmentToolRepository.save(tool);
        return new DepartmentToolDto(saved.getId(), dept.getId(), saved.getName());
    }

    @Transactional
    public void deleteTool(Long id) {
        if (!departmentToolRepository.existsById(id)) {
            throw new ResourceNotFoundException("DepartmentTool", "id", id);
        }
        departmentToolRepository.deleteById(id);
    }
}

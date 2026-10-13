package com.studyhub.platform.modules.taxonomy.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.taxonomy.dto.*;
import com.studyhub.platform.modules.taxonomy.service.TaxonomyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/taxonomy")
@Tag(name = "Taxonomy", description = "Departments, Subjects, Goals, Platforms, Tools, Topics and Tags APIs")
public class TaxonomyController {

    private final TaxonomyService taxonomyService;

    public TaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    @GetMapping("/departments")
    @Operation(summary = "Get all active academic departments")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getAllDepartments()));
    }

    @GetMapping("/departments/{id}/details")
    @Operation(summary = "Get department complete profile including subjects, goals, platforms, and tools")
    public ResponseEntity<ApiResponse<DepartmentDetailDto>> getDepartmentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getDepartmentDetails(id)));
    }

    @GetMapping("/subjects")
    @Operation(summary = "Get subjects filtered by department and semester")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getSubjects(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer semester) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getSubjects(departmentId, semester)));
    }

    @GetMapping("/departments/{departmentId}/subjects")
    @Operation(summary = "Get subjects for a specific department")
    public ResponseEntity<ApiResponse<List<SubjectDto>>> getDepartmentSubjects(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getSubjects(departmentId, null)));
    }

    @GetMapping("/goals")
    @Operation(summary = "Get study goals available for a department (including shared goals)")
    public ResponseEntity<ApiResponse<List<GoalDto>>> getGoals(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getGoals(departmentId)));
    }

    @GetMapping("/platforms")
    @Operation(summary = "Get curated external platforms for a department")
    public ResponseEntity<ApiResponse<List<DepartmentPlatformDto>>> getPlatforms(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getPlatforms(departmentId)));
    }

    @GetMapping("/tools")
    @Operation(summary = "Get tools and frameworks for a department")
    public ResponseEntity<ApiResponse<List<DepartmentToolDto>>> getTools(
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getTools(departmentId)));
    }

    @GetMapping("/topics")
    @Operation(summary = "Get topics for a specific subject")
    public ResponseEntity<ApiResponse<List<TopicDto>>> getTopics(@RequestParam Long subjectId) {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getTopicsBySubject(subjectId)));
    }

    @GetMapping("/tags")
    @Operation(summary = "Get all tags")
    public ResponseEntity<ApiResponse<List<TagDto>>> getTags() {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getAllTags()));
    }
}

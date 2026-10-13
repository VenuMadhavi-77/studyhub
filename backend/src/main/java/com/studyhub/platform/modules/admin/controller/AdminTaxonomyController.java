package com.studyhub.platform.modules.admin.controller;

import com.studyhub.platform.common.dto.ApiResponse;
import com.studyhub.platform.modules.admin.dto.*;
import com.studyhub.platform.modules.taxonomy.dto.*;
import com.studyhub.platform.modules.taxonomy.service.TaxonomyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/taxonomy")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Taxonomy", description = "Admin Management for Departments, Subjects, Goals, Platforms, and Tools")
public class AdminTaxonomyController {

    private final TaxonomyService taxonomyService;

    public AdminTaxonomyController(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    // --- DEPARTMENTS ---

    @GetMapping("/departments")
    @Operation(summary = "Get all departments (including inactive) for administration")
    public ResponseEntity<ApiResponse<List<DepartmentDto>>> getAllDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(taxonomyService.getAllDepartmentsAdmin()));
    }

    @PostMapping("/departments")
    @Operation(summary = "Create an academic department")
    public ResponseEntity<ApiResponse<DepartmentDto>> createDepartment(
            @Valid @RequestBody TaxonomyCreateDepartmentRequest request) {
        DepartmentDto created = taxonomyService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Department created successfully"));
    }

    @PutMapping("/departments/{id}")
    @Operation(summary = "Update an academic department")
    public ResponseEntity<ApiResponse<DepartmentDto>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody TaxonomyUpdateDepartmentRequest request) {
        DepartmentDto updated = taxonomyService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Department updated successfully"));
    }

    @PatchMapping("/departments/{id}/status")
    @Operation(summary = "Activate or deactivate a department")
    public ResponseEntity<ApiResponse<DepartmentDto>> toggleDepartmentStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        DepartmentDto updated = taxonomyService.toggleDepartmentStatus(id, active);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Department status updated"));
    }

    // --- SUBJECTS ---

    @PostMapping("/subjects")
    @Operation(summary = "Create an academic subject")
    public ResponseEntity<ApiResponse<SubjectDto>> createSubject(
            @Valid @RequestBody TaxonomyCreateSubjectRequest request) {
        SubjectDto created = taxonomyService.createSubject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Subject created successfully"));
    }

    @PutMapping("/subjects/{id}")
    @Operation(summary = "Update an academic subject")
    public ResponseEntity<ApiResponse<SubjectDto>> updateSubject(
            @PathVariable Long id,
            @Valid @RequestBody TaxonomyUpdateSubjectRequest request) {
        SubjectDto updated = taxonomyService.updateSubject(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Subject updated successfully"));
    }

    @PatchMapping("/subjects/{id}/status")
    @Operation(summary = "Activate or deactivate a subject")
    public ResponseEntity<ApiResponse<SubjectDto>> toggleSubjectStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        SubjectDto updated = taxonomyService.toggleSubjectStatus(id, active);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Subject status updated"));
    }

    @PostMapping("/topics")
    @Operation(summary = "Create an academic topic under a subject")
    public ResponseEntity<ApiResponse<TopicDto>> createTopic(
            @Valid @RequestBody TaxonomyCreateTopicRequest request) {
        TopicDto created = taxonomyService.createTopic(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Topic created successfully"));
    }

    // --- GOALS ---

    @PostMapping("/goals")
    @Operation(summary = "Create a department or shared goal")
    public ResponseEntity<ApiResponse<GoalDto>> createGoal(
            @Valid @RequestBody TaxonomyCreateGoalRequest request) {
        GoalDto created = taxonomyService.createGoal(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Goal created successfully"));
    }

    @PutMapping("/goals/{id}")
    @Operation(summary = "Update a goal")
    public ResponseEntity<ApiResponse<GoalDto>> updateGoal(
            @PathVariable Long id,
            @Valid @RequestBody TaxonomyUpdateGoalRequest request) {
        GoalDto updated = taxonomyService.updateGoal(id, request);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Goal updated successfully"));
    }

    @PatchMapping("/goals/{id}/status")
    @Operation(summary = "Activate or deactivate a goal")
    public ResponseEntity<ApiResponse<GoalDto>> toggleGoalStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        GoalDto updated = taxonomyService.toggleGoalStatus(id, active);
        return ResponseEntity.ok(ApiResponse.ok(updated, "Goal status updated"));
    }

    // --- PLATFORMS ---

    @PostMapping("/platforms")
    @Operation(summary = "Add an external platform mapping to a department")
    public ResponseEntity<ApiResponse<DepartmentPlatformDto>> createPlatform(
            @Valid @RequestBody TaxonomyCreatePlatformRequest request) {
        DepartmentPlatformDto created = taxonomyService.createPlatform(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Platform assigned successfully"));
    }

    @DeleteMapping("/platforms/{id}")
    @Operation(summary = "Remove a platform mapping from a department")
    public ResponseEntity<ApiResponse<Void>> deletePlatform(@PathVariable Long id) {
        taxonomyService.deletePlatform(id);
        return ResponseEntity.ok(ApiResponse.ok("Platform mapping removed"));
    }

    // --- TOOLS ---

    @PostMapping("/tools")
    @Operation(summary = "Add a software tool or framework to a department")
    public ResponseEntity<ApiResponse<DepartmentToolDto>> createTool(
            @Valid @RequestBody TaxonomyCreateToolRequest request) {
        DepartmentToolDto created = taxonomyService.createTool(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(created, "Tool assigned successfully"));
    }

    @DeleteMapping("/tools/{id}")
    @Operation(summary = "Remove a software tool from a department")
    public ResponseEntity<ApiResponse<Void>> deleteTool(@PathVariable Long id) {
        taxonomyService.deleteTool(id);
        return ResponseEntity.ok(ApiResponse.ok("Tool mapping removed"));
    }
}

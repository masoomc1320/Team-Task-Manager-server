package com.taskmanager.controller;

import com.taskmanager.dto.ProjectDtos;
import com.taskmanager.model.Project;
import com.taskmanager.repo.ProjectRepository;
import com.taskmanager.repo.UserRepository;
import com.taskmanager.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public ProjectController(ProjectRepository projectRepository, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> all(@AuthenticationPrincipal AuthUser authUser) {
        List<Project> projects = authUser.isAdmin()
                ? projectRepository.findAll()
                : projectRepository.findAllWhereMember(authUser.id());
        return projects.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    public ResponseEntity<?> create(@AuthenticationPrincipal AuthUser authUser, @Valid @RequestBody ProjectDtos.CreateProjectRequest req) {
        if (!authUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required"));
        }
        Project p = new Project();
        p.setName(req.name());
        p.setDescription(req.description() == null ? "" : req.description());
        p.setCreatedBy(authUser.id());
        p.getMemberIds().add(authUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(projectRepository.save(p)));
    }

    @PostMapping("/{projectId}/members")
    public ResponseEntity<?> addMember(@AuthenticationPrincipal AuthUser authUser,
                                       @PathVariable Long projectId,
                                       @Valid @RequestBody ProjectDtos.AddMemberRequest req) {
        if (!authUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required"));
        }
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));
        }
        Long userId;
        try {
            userId = Long.valueOf(req.userId());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid user id"));
        }
        if (userRepository.findById(userId).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "User not found"));
        }
        if (!project.getMemberIds().contains(userId)) {
            project.getMemberIds().add(userId);
            projectRepository.save(project);
        }
        return ResponseEntity.ok(toResponse(project));
    }

    @PostMapping("/{projectId}/members/by-email")
    public ResponseEntity<?> addMemberByEmail(@AuthenticationPrincipal AuthUser authUser,
                                              @PathVariable Long projectId,
                                              @Valid @RequestBody ProjectDtos.AddMemberByEmailRequest req) {
        if (!authUser.isAdmin()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Admin access required"));
        }
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));
        }
        String email = req.email().trim().toLowerCase();
        var userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "No user with that email"));
        }
        Long userId = userOpt.get().getId();
        if (!project.getMemberIds().contains(userId)) {
            project.getMemberIds().add(userId);
            projectRepository.save(project);
        }
        return ResponseEntity.ok(toResponse(project));
    }

    private Map<String, Object> toResponse(Project project) {
        List<Map<String, Object>> members = userRepository.findAllById(project.getMemberIds()).stream()
                .map(user -> Map.<String, Object>of(
                        "_id", user.getId(),
                        "name", user.getName(),
                        "email", user.getEmail(),
                        "role", user.getRole().name().toLowerCase()
                ))
                .collect(Collectors.toList());
        String description = project.getDescription() != null ? project.getDescription() : "";
        return Map.of(
                "_id", project.getId(),
                "name", project.getName(),
                "description", description,
                "members", members
        );
    }
}

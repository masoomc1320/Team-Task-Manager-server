package com.taskmanager.controller;

import com.taskmanager.dto.TaskDtos;
import com.taskmanager.model.Project;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repo.ProjectRepository;
import com.taskmanager.repo.TaskRepository;
import com.taskmanager.repo.UserRepository;
import com.taskmanager.security.AuthUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public TaskController(TaskRepository taskRepository, ProjectRepository projectRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Map<String, Object>> all(@AuthenticationPrincipal AuthUser authUser) {
        List<Task> tasks = authUser.isAdmin() ? taskRepository.findAll() : taskRepository.findByAssignedTo(authUser.id());
        return tasks.stream().map(this::toResponse).toList();
    }

    @PostMapping("/project/{projectId}")
    public ResponseEntity<?> create(@AuthenticationPrincipal AuthUser authUser,
                                    @PathVariable Long projectId,
                                    @Valid @RequestBody TaskDtos.CreateTaskRequest req) {
        Project project = projectRepository.findById(projectId).orElse(null);
        if (project == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Project not found"));
        }
        if (!authUser.isAdmin() && !project.getMemberIds().contains(authUser.id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not allowed for this project"));
        }
        Long assignedTo = req.assignedTo();
        if (!project.getMemberIds().contains(assignedTo)) {
            return ResponseEntity.badRequest().body(Map.of("message", "Assignee is not a project member"));
        }
        if (userRepository.findById(assignedTo).isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid assignee id"));
        }

        Task task = new Task();
        task.setTitle(req.title());
        task.setDescription(req.description() == null ? "" : req.description());
        task.setDueDate(req.dueDate());
        task.setAssignedTo(assignedTo);
        task.setProjectId(projectId);
        task.setCreatedBy(authUser.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(taskRepository.save(task)));
    }

    @PatchMapping("/{taskId}/status")
    public ResponseEntity<?> updateStatus(@AuthenticationPrincipal AuthUser authUser,
                                          @PathVariable Long taskId,
                                          @Valid @RequestBody TaskDtos.UpdateStatusRequest req) {
        Task task = taskRepository.findById(taskId).orElse(null);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Task not found"));
        }
        if (!authUser.isAdmin() && !task.getAssignedTo().equals(authUser.id())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Not allowed to update task"));
        }
        TaskStatus newStatus;
        try {
            newStatus = TaskStatus.valueOf(req.status().toUpperCase().replace("-", "_"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status"));
        }
        task.setStatus(newStatus);
        return ResponseEntity.ok(toResponse(taskRepository.save(task)));
    }

    private Map<String, Object> toResponse(Task task) {
        Project project = projectRepository.findById(task.getProjectId()).orElse(null);
        var user = userRepository.findById(task.getAssignedTo()).orElse(null);
        String desc = task.getDescription() != null ? task.getDescription() : "";
        return Map.of(
                "_id", task.getId(),
                "title", task.getTitle(),
                "description", desc,
                "status", task.getStatus().name().toLowerCase().replace("_", "-"),
                "dueDate", task.getDueDate().toString(),
                "project", project == null ? Map.of("_id", task.getProjectId(), "name", "Unknown") : Map.of("_id", project.getId(), "name", project.getName()),
                "assignedTo", user == null ? Map.of("_id", task.getAssignedTo(), "name", "Unknown") : Map.of("_id", user.getId(), "name", user.getName(), "email", user.getEmail())
        );
    }
}

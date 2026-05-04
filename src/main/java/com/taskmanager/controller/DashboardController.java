package com.taskmanager.controller;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repo.TaskRepository;
import com.taskmanager.security.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final TaskRepository taskRepository;

    public DashboardController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping
    public Map<String, Object> dashboard(@AuthenticationPrincipal AuthUser authUser) {
        LocalDate today = LocalDate.now();
        boolean isAdmin = authUser.isAdmin();

        long total = isAdmin ? taskRepository.count() : taskRepository.countByAssignedTo(authUser.id());
        long todo = isAdmin ? taskRepository.countByStatus(TaskStatus.TODO) : taskRepository.countByAssignedToAndStatus(authUser.id(), TaskStatus.TODO);
        long inProgress = isAdmin ? taskRepository.countByStatus(TaskStatus.IN_PROGRESS) : taskRepository.countByAssignedToAndStatus(authUser.id(), TaskStatus.IN_PROGRESS);
        long done = isAdmin ? taskRepository.countByStatus(TaskStatus.DONE) : taskRepository.countByAssignedToAndStatus(authUser.id(), TaskStatus.DONE);
        long overdue = isAdmin
                ? taskRepository.countByStatusNotAndDueDateBefore(TaskStatus.DONE, today)
                : taskRepository.countByAssignedToAndStatusNotAndDueDateBefore(authUser.id(), TaskStatus.DONE, today);

        List<Task> overdueTasks = isAdmin
                ? taskRepository.findTop10ByStatusNotAndDueDateBeforeOrderByDueDateAsc(TaskStatus.DONE, today)
                : taskRepository.findTop10ByAssignedToAndStatusNotAndDueDateBeforeOrderByDueDateAsc(authUser.id(), TaskStatus.DONE, today);

        return Map.of(
                "counters", Map.of(
                        "total", total,
                        "todo", todo,
                        "inProgress", inProgress,
                        "done", done,
                        "overdue", overdue
                ),
                "overdueTasks", overdueTasks
        );
    }
}

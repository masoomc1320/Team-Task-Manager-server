package com.taskmanager.repo;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo(Long assignedTo);
    long countByAssignedTo(Long assignedTo);
    long countByAssignedToAndStatus(Long assignedTo, TaskStatus status);
    long countByAssignedToAndStatusNotAndDueDateBefore(Long assignedTo, TaskStatus status, LocalDate date);
    List<Task> findTop10ByAssignedToAndStatusNotAndDueDateBeforeOrderByDueDateAsc(Long assignedTo, TaskStatus status, LocalDate date);
    long countByStatus(TaskStatus status);
    long countByStatusNotAndDueDateBefore(TaskStatus status, LocalDate date);
    List<Task> findTop10ByStatusNotAndDueDateBeforeOrderByDueDateAsc(TaskStatus status, LocalDate date);
}

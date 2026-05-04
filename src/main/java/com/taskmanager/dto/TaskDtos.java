package com.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class TaskDtos {
    public record CreateTaskRequest(
            @NotBlank @Size(min = 2) String title,
            String description,
            @NotNull LocalDate dueDate,
            @NotNull Long assignedTo
    ) {}

    public record UpdateStatusRequest(@NotBlank String status) {}
}

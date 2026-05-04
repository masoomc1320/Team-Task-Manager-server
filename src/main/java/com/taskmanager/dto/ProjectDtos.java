package com.taskmanager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectDtos {
    public record CreateProjectRequest(@NotBlank @Size(min = 2) String name, String description) {}
    public record AddMemberRequest(@NotBlank String userId) {}
    public record AddMemberByEmailRequest(@NotBlank @Email String email) {}
}

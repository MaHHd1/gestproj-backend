package com.gestproj.backend.task.dto;

import java.time.LocalDate;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TaskRequest(
    Long projectId,
    @NotBlank @Size(min = 2, max = 200) String title,
    @Size(max = 2000) String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    Long assignedTo) {}

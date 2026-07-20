package com.gestproj.backend.task.dto;

import java.time.LocalDate;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;

public record TaskResponse(
    Long id,
    Long projectId,
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    boolean late,
    Long assignedTo,
    String assignedToUsername) {}

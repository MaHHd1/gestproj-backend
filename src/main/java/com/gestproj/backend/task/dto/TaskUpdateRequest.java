package com.gestproj.backend.task.dto;

import java.time.LocalDate;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;

public record TaskUpdateRequest(
    String title,
    String description,
    TaskStatus status,
    TaskPriority priority,
    LocalDate dueDate,
    Long assignedTo) {}

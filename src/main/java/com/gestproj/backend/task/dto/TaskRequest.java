package com.gestproj.backend.task.dto;

import com.gestproj.backend.common.enums.TaskPriority;
import com.gestproj.backend.common.enums.TaskStatus;

import java.time.LocalDate;

public record TaskRequest(
        Long projectId,
        String title,
        String description,
        TaskStatus status,
        TaskPriority priority,
        LocalDate dueDate,
        Long assignedTo
) {
}

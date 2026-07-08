package com.gestproj.backend.project.dto;

public record ProjectStatisticsResponse(
        Long totalTasks,
        Long completedTasks,
        Long inProgressTasks,
        Long notStartedTasks,
        Long lateTasks,
        Double completionPercentage
) {
}

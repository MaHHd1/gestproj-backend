package com.gestproj.backend.task.service;

import com.gestproj.backend.common.enums.TaskStatus;
import com.gestproj.backend.task.entity.Task;
import com.gestproj.backend.task.repository.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskSchedulerService {

    private final TaskRepository taskRepository;

    public TaskSchedulerService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void updateTasksLateness() {
        LocalDate today = LocalDate.now();
        List<Task> allTasks = taskRepository.findAll();

        for (Task task : allTasks) {
            boolean shouldBeLate = task.getDueDate() != null
                    && task.getDueDate().isBefore(today)
                    && task.getStatus() != TaskStatus.TERMINE;

            if (task.isLate() != shouldBeLate) {
                task.setLate(shouldBeLate);
                taskRepository.save(task);
            }
        }
    }
}


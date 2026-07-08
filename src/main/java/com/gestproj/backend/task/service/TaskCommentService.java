package com.gestproj.backend.task.service;

import com.gestproj.backend.common.exception.ForbiddenException;
import com.gestproj.backend.common.exception.ResourceNotFoundException;
import com.gestproj.backend.task.dto.TaskCommentCreateRequest;
import com.gestproj.backend.task.dto.TaskCommentResponse;
import com.gestproj.backend.task.entity.Task;
import com.gestproj.backend.task.entity.TaskComment;
import com.gestproj.backend.task.repository.TaskCommentRepository;
import com.gestproj.backend.task.repository.TaskRepository;
import com.gestproj.backend.user.entity.User;
import com.gestproj.backend.user.service.UserService;
import com.gestproj.backend.member.service.ProjectMemberService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskCommentService {

    private final TaskCommentRepository taskCommentRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ProjectMemberService projectMemberService;

    public TaskCommentService(TaskCommentRepository taskCommentRepository, TaskRepository taskRepository, UserService userService, ProjectMemberService projectMemberService) {
        this.taskCommentRepository = taskCommentRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.projectMemberService = projectMemberService;
    }

    public TaskCommentResponse create(Long taskId, TaskCommentCreateRequest request, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!projectMemberService.isMember(task.getProject(), currentUser)) {
            throw new ForbiddenException("You are not a member of this project");
        }

        TaskComment comment = new TaskComment();
        comment.setTask(task);
        comment.setUser(currentUser);
        comment.setContent(request.content().trim());
        comment.setCreatedAt(LocalDateTime.now());

        TaskComment savedComment = taskCommentRepository.save(comment);
        return toResponse(savedComment);
    }

    public List<TaskCommentResponse> getByTaskId(Long taskId, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (!projectMemberService.isMember(task.getProject(), currentUser)) {
            throw new ForbiddenException("You are not a member of this project");
        }

        return taskCommentRepository.findByTaskIdOrderByCreatedAtDesc(taskId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public TaskCommentResponse delete(Long commentId, String currentUserEmail) {
        User currentUser = userService.findEntityByEmail(currentUserEmail);
        TaskComment comment = taskCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        if (!comment.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You can only delete your own comments");
        }

        taskCommentRepository.delete(comment);
        return toResponse(comment);
    }

    private TaskCommentResponse toResponse(TaskComment comment) {
        return new TaskCommentResponse(
                comment.getId(),
                comment.getTask().getId(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getUser().getEmail(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}

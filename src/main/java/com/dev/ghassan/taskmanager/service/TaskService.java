package com.dev.ghassan.taskmanager.service;

import com.dev.ghassan.taskmanager.dto.TaskRequest;
import com.dev.ghassan.taskmanager.dto.TaskResponse;
import com.dev.ghassan.taskmanager.exception.ResourceNotFoundException;
import com.dev.ghassan.taskmanager.model.Task;
import com.dev.ghassan.taskmanager.model.TaskStatus;
import com.dev.ghassan.taskmanager.model.User;
import com.dev.ghassan.taskmanager.repository.TaskRepository;
import com.dev.ghassan.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final InputSanitizationService sanitizationService;

    private User getCurrentUser() {
        // Authentication now contains email as the principal
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private TaskResponse mapToResponse(Task task) {
        TaskResponse response = new TaskResponse();
        response.setId(task.getId());
        response.setTitle(task.getTitle());
        response.setDescription(task.getDescription());
        response.setStatus(task.getStatus());
        response.setPriority(task.getPriority());
        response.setDueDate(task.getDueDate());
        response.setCreatedAt(task.getCreatedAt());
        response.setTags(task.getTags());
        return response;
    }

    public List<TaskResponse> getAllTasks() {
        User user = getCurrentUser();
        return taskRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TaskResponse getTaskById(String id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return mapToResponse(task);
    }

    public TaskResponse createTask(TaskRequest request) {
        User user = getCurrentUser();
        Task task = new Task();
        task.setTitle(sanitizationService.sanitizeString(request.getTitle()));
        task.setDescription(sanitizationService.sanitizeString(request.getDescription()));
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setTags(sanitizationService.sanitizeStringList(request.getTags()));
        task.setUser(user);
        
        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    public TaskResponse updateTask(String id, TaskRequest request) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(sanitizationService.sanitizeString(request.getTitle()));
        task.setDescription(sanitizationService.sanitizeString(request.getDescription()));
        task.setStatus(request.getStatus());
        task.setPriority(request.getPriority());
        task.setDueDate(request.getDueDate());
        task.setTags(sanitizationService.sanitizeStringList(request.getTags()));

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    public TaskResponse updateTaskStatus(String id, TaskStatus status) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        
        task.setStatus(status);
        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    public void deleteTask(String id) {
        User user = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }
}
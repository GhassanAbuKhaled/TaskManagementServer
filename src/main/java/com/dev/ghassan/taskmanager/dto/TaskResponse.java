package com.dev.ghassan.taskmanager.dto;

import com.dev.ghassan.taskmanager.model.TaskPriority;
import com.dev.ghassan.taskmanager.model.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskResponse {
    private String id;
    private String title;
    private String description;
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
    private List<String> tags;
}
package com.dev.ghassan.taskmanager.dto;

import com.dev.ghassan.taskmanager.model.TaskPriority;
import com.dev.ghassan.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title must not exceed 100 characters")
    private String title;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    private TaskStatus status;
    private TaskPriority priority;
    private LocalDateTime dueDate;
    
    @Size(max = 10, message = "Maximum 10 tags allowed")
    private List<@Size(max = 30, message = "Each tag must not exceed 30 characters") String> tags;

    public void setTitle(String title) { this.title = title; }

    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
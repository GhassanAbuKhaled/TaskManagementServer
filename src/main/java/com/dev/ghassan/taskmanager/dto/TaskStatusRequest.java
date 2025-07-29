package com.dev.ghassan.taskmanager.dto;

import com.dev.ghassan.taskmanager.model.TaskStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskStatusRequest {
    @NotNull(message = "Status is required")
    private TaskStatus status;
}
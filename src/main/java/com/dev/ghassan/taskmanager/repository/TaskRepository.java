package com.dev.ghassan.taskmanager.repository;

import com.dev.ghassan.taskmanager.model.Task;
import com.dev.ghassan.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByUser(User user);
    Optional<Task> findByIdAndUser(String id, User user);
}
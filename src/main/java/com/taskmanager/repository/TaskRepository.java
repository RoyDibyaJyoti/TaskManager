package com.taskmanager.repository;

import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import java.util.List;

public interface TaskRepository {
    List<Task> getAllTasks();
    void saveTasks(List<Task> tasks);
    
    List<TaskCategory> getAllCategories();
    void saveCategories(List<TaskCategory> categories);
}

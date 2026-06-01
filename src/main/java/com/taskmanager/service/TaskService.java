package com.taskmanager.service;

import com.taskmanager.model.PriorityLevel;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import com.taskmanager.repository.TaskRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TaskService {
    private final TaskRepository repository;
    private final List<Task> tasks;
    private final List<TaskCategory> categories;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
        this.tasks = new ArrayList<>(repository.getAllTasks());
        this.categories = new ArrayList<>(repository.getAllCategories());
    }

    // --- Task Operations ---

    public List<Task> getAllTasks() {
        return Collections.unmodifiableList(tasks);
    }

    public Task getTaskById(String id) {
        return tasks.stream()
                .filter(t -> t.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public void addTask(Task task) {
        validateTask(task);
        if (getTaskById(task.getId()) != null) {
            throw new IllegalArgumentException("Task with ID " + task.getId() + " already exists.");
        }
        tasks.add(task);
        saveTasks();
    }

    public void updateTask(Task updatedTask) {
        validateTask(updatedTask);
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getId().equals(updatedTask.getId())) {
                tasks.set(i, updatedTask);
                saveTasks();
                return;
            }
        }
        throw new IllegalArgumentException("Task with ID " + updatedTask.getId() + " not found.");
    }

    public void deleteTask(String id) {
        boolean removed = tasks.removeIf(t -> t.getId().equals(id));
        if (removed) {
            saveTasks();
        }
    }

    private void saveTasks() {
        repository.saveTasks(tasks);
    }

    private void validateTask(Task task) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty.");
        }
        if (task.getPriority() == null) {
            throw new IllegalArgumentException("Task priority must be specified.");
        }
    }

    // --- Category Operations ---

    public List<TaskCategory> getAllCategories() {
        return Collections.unmodifiableList(categories);
    }

    public TaskCategory getCategoryById(String id) {
        return categories.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public TaskCategory getCategoryByName(String name) {
        return categories.stream()
                .filter(c -> c.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void addCategory(TaskCategory category) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        if (getCategoryByName(category.getName()) != null) {
            throw new IllegalArgumentException("Category with name '" + category.getName() + "' already exists.");
        }
        categories.add(category);
        saveCategories();
    }

    public void deleteCategory(String id) {
        TaskCategory categoryToDelete = getCategoryById(id);
        if (categoryToDelete != null) {
            categories.remove(categoryToDelete);
            saveCategories();

            // Cascade: Update tasks belonging to deleted category to have no category
            boolean updatedAny = false;
            for (Task task : tasks) {
                if (categoryToDelete.getName().equalsIgnoreCase(task.getCategoryName())) {
                    task.setCategoryName(null);
                    updatedAny = true;
                }
            }
            if (updatedAny) {
                saveTasks();
            }
        }
    }

    private void saveCategories() {
        repository.saveCategories(categories);
    }

    // --- Search & Filter Logic ---

    public List<Task> searchAndFilterTasks(String query, String category, PriorityLevel priority, Boolean completed) {
        return tasks.stream()
                .filter(task -> {
                    if (query == null || query.trim().isEmpty()) {
                        return true;
                    }
                    String lowerQuery = query.toLowerCase();
                    boolean matchTitle = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerQuery);
                    boolean matchDesc = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerQuery);
                    return matchTitle || matchDesc;
                })
                .filter(task -> {
                    if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) {
                        return true;
                    }
                    return category.equalsIgnoreCase(task.getCategoryName());
                })
                .filter(task -> {
                    if (priority == null) {
                        return true;
                    }
                    return task.getPriority() == priority;
                })
                .filter(task -> {
                    if (completed == null) {
                        return true;
                    }
                    return task.isCompleted() == completed;
                })
                .collect(Collectors.toList());
    }

    // --- Statistics Calculations ---

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int total = tasks.size();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long pending = total - completed;
        
        LocalDate today = LocalDate.now();
        long overdue = tasks.stream()
                .filter(t -> !t.isCompleted() && t.getDueDate() != null && t.getDueDate().isBefore(today))
                .count();

        stats.put("total", total);
        stats.put("completed", (int) completed);
        stats.put("pending", (int) pending);
        stats.put("overdue", (int) overdue);

        // Completion percentage
        double completionRate = total > 0 ? (double) completed / total : 0.0;
        stats.put("completionRate", completionRate);

        // Priority breakdown
        Map<PriorityLevel, Long> priorityCount = tasks.stream()
                .collect(Collectors.groupingBy(Task::getPriority, Collectors.counting()));
        
        // Ensure all priority levels exist in map
        for (PriorityLevel p : PriorityLevel.values()) {
            priorityCount.putIfAbsent(p, 0L);
        }
        stats.put("priorityBreakdown", priorityCount);

        // Category breakdown
        Map<String, Long> categoryCount = tasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getCategoryName() != null ? t.getCategoryName() : "Uncategorized",
                        Collectors.counting()
                ));
        stats.put("categoryBreakdown", categoryCount);

        return stats;
    }
}

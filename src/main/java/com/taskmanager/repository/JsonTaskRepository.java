package com.taskmanager.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import com.taskmanager.util.JsonUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonTaskRepository implements TaskRepository {
    private final String tasksFilePath;
    private final String categoriesFilePath;

    public JsonTaskRepository() {
        this(
            new File(resolveBaseDataDirectory(), "tasks.json").getAbsolutePath(),
            new File(resolveBaseDataDirectory(), "categories.json").getAbsolutePath()
        );
    }

    public JsonTaskRepository(String tasksFilePath, String categoriesFilePath) {
        this.tasksFilePath = tasksFilePath;
        this.categoriesFilePath = categoriesFilePath;
        ensureDirectoryExists(tasksFilePath);
        ensureDirectoryExists(categoriesFilePath);
    }

    private static String resolveBaseDataDirectory() {
        String userHome = System.getProperty("user.home");
        String osName = System.getProperty("os.name").toLowerCase();
        
        File baseDir;
        if (osName.contains("mac")) {
            // ~/Library/Application Support/TaskManager/
            baseDir = new File(userHome, "Library/Application Support/TaskManager");
        } else if (osName.contains("win")) {
            // %APPDATA%\TaskManager\
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                baseDir = new File(appData, "TaskManager");
            } else {
                baseDir = new File(userHome, "AppData/Roaming/TaskManager");
            }
        } else {
            // Linux/Unix fallback
            baseDir = new File(userHome, ".config/TaskManager");
        }
        
        return baseDir.getAbsolutePath();
    }

    private void ensureDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            boolean created = parent.mkdirs();
            if (created) {
                System.out.println("[INFO] Created data directory: " + parent.getAbsolutePath());
            } else {
                System.err.println("[ERROR] Failed to create data directory: " + parent.getAbsolutePath());
            }
        }
    }

    @Override
    public List<Task> getAllTasks() {
        File file = new File(tasksFilePath);
        System.out.println("[INFO] Loading tasks from: " + file.getAbsolutePath());
        if (!file.exists() || file.length() == 0) {
            System.out.println("[INFO] Tasks file is empty or missing. Loading 0 tasks.");
            return new ArrayList<>();
        }
        try {
            List<Task> tasks = JsonUtils.getMapper().readValue(file, new TypeReference<List<Task>>() {});
            System.out.println("[INFO] Loaded " + tasks.size() + " tasks successfully.");
            return tasks;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load tasks from " + file.getAbsolutePath() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public void saveTasks(List<Task> tasks) {
        File file = new File(tasksFilePath);
        System.out.println("[INFO] Saving tasks to: " + file.getAbsolutePath());
        try {
            JsonUtils.getMapper().writeValue(file, tasks);
            System.out.println("[INFO] Saved " + tasks.size() + " tasks successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save tasks to " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    @Override
    public List<TaskCategory> getAllCategories() {
        File file = new File(categoriesFilePath);
        System.out.println("[INFO] Loading categories from: " + file.getAbsolutePath());
        if (!file.exists() || file.length() == 0) {
            System.out.println("[INFO] Categories file is empty or missing. Loading default categories.");
            return getDefaultCategories();
        }
        try {
            List<TaskCategory> categories = JsonUtils.getMapper().readValue(file, new TypeReference<List<TaskCategory>>() {});
            System.out.println("[INFO] Loaded " + categories.size() + " categories successfully.");
            return categories;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to load categories from " + file.getAbsolutePath() + ": " + e.getMessage());
            return getDefaultCategories();
        }
    }

    @Override
    public void saveCategories(List<TaskCategory> categories) {
        File file = new File(categoriesFilePath);
        System.out.println("[INFO] Saving categories to: " + file.getAbsolutePath());
        try {
            JsonUtils.getMapper().writeValue(file, categories);
            System.out.println("[INFO] Saved " + categories.size() + " categories successfully.");
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to save categories to " + file.getAbsolutePath() + ": " + e.getMessage());
        }
    }

    private List<TaskCategory> getDefaultCategories() {
        List<TaskCategory> defaults = new ArrayList<>();
        defaults.add(new TaskCategory("1", "Work", "#3B82F6"));       // Blue
        defaults.add(new TaskCategory("2", "Personal", "#10B981"));   // Emerald
        defaults.add(new TaskCategory("3", "Shopping", "#EC4899"));   // Pink
        defaults.add(new TaskCategory("4", "Urgent", "#EF4444"));     // Red
        defaults.add(new TaskCategory("5", "Health", "#8B5CF6"));     // Purple
        return defaults;
    }
}

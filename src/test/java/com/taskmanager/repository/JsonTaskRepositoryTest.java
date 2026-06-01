package com.taskmanager.repository;

import com.taskmanager.model.PriorityLevel;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonTaskRepositoryTest {

    @TempDir
    public Path tempDir;

    private String tasksPath;
    private String categoriesPath;
    private JsonTaskRepository repository;

    @BeforeEach
    public void setUp() {
        tasksPath = tempDir.resolve("tasks_test.json").toString();
        categoriesPath = tempDir.resolve("categories_test.json").toString();
        repository = new JsonTaskRepository(tasksPath, categoriesPath);
    }

    @Test
    public void testGetTasks_FileDoesNotExist_ReturnsEmptyList() {
        List<Task> tasks = repository.getAllTasks();
        assertNotNull(tasks);
        assertTrue(tasks.isEmpty());
    }

    @Test
    public void testGetCategories_FileDoesNotExist_ReturnsDefaultCategories() {
        List<TaskCategory> categories = repository.getAllCategories();
        assertNotNull(categories);
        assertFalse(categories.isEmpty());
        // Verify default categories are loaded
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Work")));
        assertTrue(categories.stream().anyMatch(c -> c.getName().equals("Personal")));
    }

    @Test
    public void testSaveAndLoadTasks() {
        List<Task> tasksToSave = new ArrayList<>();
        tasksToSave.add(new Task("t1", "Test Title 1", "Desc 1", LocalDate.of(2026, 6, 1), PriorityLevel.HIGH, "Work", false));
        tasksToSave.add(new Task("t2", "Test Title 2", "Desc 2", null, PriorityLevel.LOW, "Personal", true));

        // Save
        repository.saveTasks(tasksToSave);

        // Verify file is created
        File file = new File(tasksPath);
        assertTrue(file.exists());
        assertTrue(file.length() > 0);

        // Create new repository to read it from disk
        JsonTaskRepository anotherRepository = new JsonTaskRepository(tasksPath, categoriesPath);
        List<Task> loadedTasks = anotherRepository.getAllTasks();

        assertEquals(2, loadedTasks.size());
        
        Task loaded1 = loadedTasks.stream().filter(t -> t.getId().equals("t1")).findFirst().orElse(null);
        assertNotNull(loaded1);
        assertEquals("Test Title 1", loaded1.getTitle());
        assertEquals("Desc 1", loaded1.getDescription());
        assertEquals(LocalDate.of(2026, 6, 1), loaded1.getDueDate());
        assertEquals(PriorityLevel.HIGH, loaded1.getPriority());
        assertEquals("Work", loaded1.getCategoryName());
        assertFalse(loaded1.isCompleted());

        Task loaded2 = loadedTasks.stream().filter(t -> t.getId().equals("t2")).findFirst().orElse(null);
        assertNotNull(loaded2);
        assertNull(loaded2.getDueDate());
        assertTrue(loaded2.isCompleted());
    }

    @Test
    public void testSaveAndLoadCategories() {
        List<TaskCategory> categoriesToSave = new ArrayList<>();
        categoriesToSave.add(new TaskCategory("c1", "Custom Cat", "#FFFFFF"));

        // Save
        repository.saveCategories(categoriesToSave);

        // Verify file created
        assertTrue(new File(categoriesPath).exists());

        // Reload
        JsonTaskRepository anotherRepository = new JsonTaskRepository(tasksPath, categoriesPath);
        List<TaskCategory> loaded = anotherRepository.getAllCategories();

        assertEquals(1, loaded.size());
        assertEquals("Custom Cat", loaded.get(0).getName());
        assertEquals("#FFFFFF", loaded.get(0).getColorHex());
    }

    @Test
    public void testCorruptJsonFile_ReturnsEmptyList() throws IOException {
        // Write corrupt JSON to tasks file
        Files.writeString(Path.of(tasksPath), "{invalid_json}");
        
        List<Task> loaded = repository.getAllTasks();
        assertNotNull(loaded);
        assertTrue(loaded.isEmpty());
    }
}

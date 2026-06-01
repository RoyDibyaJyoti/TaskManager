package com.taskmanager.service;

import com.taskmanager.model.PriorityLevel;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class TaskServiceTest {

    private TaskService taskService;
    private MockTaskRepository mockRepository;

    @BeforeEach
    public void setUp() {
        mockRepository = new MockTaskRepository();
        taskService = new TaskService(mockRepository);
    }

    @Test
    public void testAddTask_Success() {
        Task task = new Task("t1", "Test Task", "Description", LocalDate.now().plusDays(2), PriorityLevel.HIGH, "Work", false);
        taskService.addTask(task);

        assertEquals(1, taskService.getAllTasks().size());
        assertEquals("Test Task", taskService.getTaskById("t1").getTitle());
    }

    @Test
    public void testAddTask_EmptyTitle_ThrowsException() {
        Task task = new Task("t1", "", "Description", LocalDate.now().plusDays(2), PriorityLevel.HIGH, "Work", false);
        assertThrows(IllegalArgumentException.class, () -> taskService.addTask(task));
    }

    @Test
    public void testAddTask_NullPriority_ThrowsException() {
        Task task = new Task("t1", "Task Title", "Description", LocalDate.now().plusDays(2), null, "Work", false);
        assertThrows(IllegalArgumentException.class, () -> taskService.addTask(task));
    }

    @Test
    public void testUpdateTask_Success() {
        Task task = new Task("t1", "Original Title", "Description", LocalDate.now().plusDays(2), PriorityLevel.MEDIUM, "Work", false);
        taskService.addTask(task);

        Task updated = new Task("t1", "Updated Title", "Updated Description", LocalDate.now().plusDays(3), PriorityLevel.CRITICAL, "Personal", true);
        taskService.updateTask(updated);

        Task retrieved = taskService.getTaskById("t1");
        assertEquals("Updated Title", retrieved.getTitle());
        assertEquals(PriorityLevel.CRITICAL, retrieved.getPriority());
        assertEquals("Personal", retrieved.getCategoryName());
        assertTrue(retrieved.isCompleted());
    }

    @Test
    public void testDeleteTask() {
        Task task = new Task("t1", "Task", "Description", LocalDate.now(), PriorityLevel.LOW, "Work", false);
        taskService.addTask(task);
        assertEquals(1, taskService.getAllTasks().size());

        taskService.deleteTask("t1");
        assertEquals(0, taskService.getAllTasks().size());
        assertNull(taskService.getTaskById("t1"));
    }

    @Test
    public void testDeleteCategory_CascadesToTasks() {
        TaskCategory cat = new TaskCategory("c1", "Work", "#3B82F6");
        taskService.addCategory(cat);

        Task task1 = new Task("t1", "Task 1", "Desc", LocalDate.now(), PriorityLevel.LOW, "Work", false);
        Task task2 = new Task("t2", "Task 2", "Desc", LocalDate.now(), PriorityLevel.HIGH, "Personal", false);
        taskService.addTask(task1);
        taskService.addTask(task2);

        // Delete "Work" category
        taskService.deleteCategory("c1");

        assertNull(taskService.getTaskById("t1").getCategoryName()); // should be reset to null
        assertEquals("Personal", taskService.getTaskById("t2").getCategoryName()); // should remain unchanged
    }

    @Test
    public void testSearchAndFilterTasks() {
        taskService.addTask(new Task("t1", "Buy groceries", "Milk and Eggs", LocalDate.now().plusDays(1), PriorityLevel.LOW, "Shopping", false));
        taskService.addTask(new Task("t2", "Submit report", "Work project report", LocalDate.now().plusDays(2), PriorityLevel.HIGH, "Work", false));
        taskService.addTask(new Task("t3", "Gym session", "Workout", LocalDate.now().plusDays(3), PriorityLevel.MEDIUM, "Personal", true));

        // Search query filter
        List<Task> searchResults = taskService.searchAndFilterTasks("report", null, null, null);
        assertEquals(1, searchResults.size());
        assertEquals("t2", searchResults.get(0).getId());

        // Category filter
        List<Task> categoryResults = taskService.searchAndFilterTasks(null, "Shopping", null, null);
        assertEquals(1, categoryResults.size());
        assertEquals("t1", categoryResults.get(0).getId());

        // Priority filter
        List<Task> priorityResults = taskService.searchAndFilterTasks(null, null, PriorityLevel.LOW, null);
        assertEquals(1, priorityResults.size());
        assertEquals("t1", priorityResults.get(0).getId());

        // Completion status filter
        List<Task> completedResults = taskService.searchAndFilterTasks(null, null, null, true);
        assertEquals(1, completedResults.size());
        assertEquals("t3", completedResults.get(0).getId());
    }

    @Test
    public void testGetStatistics() {
        // Today is June 2nd, 2026. Let's make an overdue task relative to now
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate tomorrow = LocalDate.now().plusDays(1);

        taskService.addTask(new Task("t1", "Overdue Task", "Desc", yesterday, PriorityLevel.HIGH, "Work", false));
        taskService.addTask(new Task("t2", "Completed Task", "Desc", yesterday, PriorityLevel.LOW, "Work", true));
        taskService.addTask(new Task("t3", "Pending Task", "Desc", tomorrow, PriorityLevel.MEDIUM, "Personal", false));

        Map<String, Object> stats = taskService.getStatistics();

        assertEquals(3, stats.get("total"));
        assertEquals(1, stats.get("completed"));
        assertEquals(2, stats.get("pending"));
        assertEquals(1, stats.get("overdue")); // t1 is overdue (yesterday and incomplete), t2 is yesterday but completed (not overdue)
        assertEquals(0.333, (double) stats.get("completionRate"), 0.01);
    }

    // Simple in-memory mock repository implementation
    private static class MockTaskRepository implements TaskRepository {
        private final List<Task> tasks = new ArrayList<>();
        private final List<TaskCategory> categories = new ArrayList<>();

        @Override
        public List<Task> getAllTasks() {
            return tasks;
        }

        @Override
        public void saveTasks(List<Task> tasks) {
            this.tasks.clear();
            this.tasks.addAll(tasks);
        }

        @Override
        public List<TaskCategory> getAllCategories() {
            return categories;
        }

        @Override
        public void saveCategories(List<TaskCategory> categories) {
            this.categories.clear();
            this.categories.addAll(categories);
        }
    }
}

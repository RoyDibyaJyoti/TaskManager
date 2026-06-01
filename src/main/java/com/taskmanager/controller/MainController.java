package com.taskmanager.controller;

import com.taskmanager.model.PriorityLevel;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import com.taskmanager.repository.JsonTaskRepository;
import com.taskmanager.service.TaskService;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainController {

    private TaskService taskService;
    private final ObservableList<Task> masterTasks = FXCollections.observableArrayList();
    private FilteredList<Task> filteredTasks;
    private SortedList<Task> sortedTasks;
    private ObservableList<TaskCategory> observableCategories = FXCollections.observableArrayList();

    // --- Sidebar Buttons ---
    @FXML private Button btnDashboard;
    @FXML private Button btnTasks;
    @FXML private Button btnCategories;
    @FXML private Button btnSettings;

    // --- Content Panes ---
    @FXML private VBox paneDashboard;
    @FXML private VBox paneTasks;
    @FXML private VBox paneCategories;
    @FXML private VBox paneSettings;

    // --- Dashboard Metrics ---
    @FXML private Label lblTotalTasks;
    @FXML private Label lblCompletedTasks;
    @FXML private Label lblPendingTasks;
    @FXML private Label lblOverdueTasks;
    @FXML private ProgressBar pbCompletion;
    @FXML private Label lblCompletionPercent;
    @FXML private VBox boxPriorityBreakdown;
    @FXML private VBox boxCategoryBreakdown;

    // --- Tasks View Inputs & Table ---
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> comboCategoryFilter;
    @FXML private ComboBox<PriorityLevel> comboPriorityFilter;
    @FXML private ComboBox<String> comboStatusFilter;
    
    @FXML private TableView<Task> tableTasks;
    @FXML private TableColumn<Task, Boolean> colStatus;
    @FXML private TableColumn<Task, String> colTitle;
    @FXML private TableColumn<Task, String> colCategory;
    @FXML private TableColumn<Task, PriorityLevel> colPriority;
    @FXML private TableColumn<Task, LocalDate> colDueDate;

    @FXML private Button btnToggleStatus;
    @FXML private Button btnDelete;
    @FXML private Button btnEdit;
    @FXML private Button btnAdd;

    // --- Categories Management ---
    @FXML private TableView<TaskCategory> tableCategories;
    @FXML private TableColumn<TaskCategory, String> colCatColor;
    @FXML private TableColumn<TaskCategory, String> colCatName;
    @FXML private TextField txtCategoryName;
    @FXML private ColorPicker cpCategoryColor;
    @FXML private Label lblCategoryError;
    @FXML private Button btnSaveCategory;
    @FXML private Button btnDeleteCategory;

    // --- Settings ---
    @FXML private CheckBox toggleDarkMode;

    // --- Status Bar ---
    @FXML private Label lblStatus;
    @FXML private Label lblStatsSummary;

    // --- Initialization & Dependency Injection ---
    public void init(TaskService taskService) {
        this.taskService = taskService;

        // Configure Navigation
        showDashboard();

        // Populate Comboboxes for Filters
        setupFilters();

        // Initialize Filtered and Sorted lists
        filteredTasks = new FilteredList<>(masterTasks, p -> true);
        sortedTasks = new SortedList<>(filteredTasks);

        // Configure Tables
        setupTasksTable();
        setupCategoriesTable();

        // Refresh Data and UI
        refreshAll();

        // Listen for Search and Filter events
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        comboCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        comboPriorityFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        comboStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Set default color in color picker
        cpCategoryColor.setValue(Color.web("#3B82F6"));
        
        // Setup native Mac system menu handlers (About, Preferences)
        setupMacMenuHandlers();
        
        setStatus("Application started and database loaded successfully.");
    }

    private void setupFilters() {
        // Category Filter
        refreshCategoryFilterComboBox();

        // Priority Filter
        ObservableList<PriorityLevel> priorities = FXCollections.observableArrayList();
        priorities.add(null); // represent "All"
        priorities.addAll(PriorityLevel.values());
        comboPriorityFilter.setItems(priorities);
        comboPriorityFilter.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Priorities" : item.getDisplayName());
            }
        });
        comboPriorityFilter.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "All Priorities" : item.getDisplayName());
            }
        });

        // Status Filter
        comboStatusFilter.setItems(FXCollections.observableArrayList("All Statuses", "Pending", "Completed"));
        comboStatusFilter.setValue("All Statuses");
    }

    private void refreshCategoryFilterComboBox() {
        ObservableList<String> categoryFilterOptions = FXCollections.observableArrayList();
        categoryFilterOptions.add("All Categories");
        categoryFilterOptions.add("Uncategorized");
        for (TaskCategory category : taskService.getAllCategories()) {
            categoryFilterOptions.add(category.getName());
        }
        comboCategoryFilter.setItems(categoryFilterOptions);
        comboCategoryFilter.setValue("All Categories");
    }

    // --- Table Views Configuration ---

    private void setupTasksTable() {
        // Bind sorted list to table comparator and set items
        sortedTasks.comparatorProperty().bind(tableTasks.comparatorProperty());
        tableTasks.setItems(sortedTasks);

        // Double-click row to edit
        tableTasks.setRowFactory(tv -> {
            TableRow<Task> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    handleEditTask();
                }
            });
            return row;
        });

        // Status Checkbox Column
        colStatus.setCellValueFactory(cellData -> {
            if (cellData == null || cellData.getValue() == null) {
                return null;
            }
            return new SimpleObjectProperty<>(cellData.getValue().isCompleted());
        });
        colStatus.setCellFactory(tc -> new TableCell<>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setAlignment(Pos.CENTER);
                checkBox.setDisable(false);
                checkBox.setOnAction(e -> {
                    Task task = getTableRow().getItem();
                    if (task != null) {
                        task.setCompleted(checkBox.isSelected());
                        taskService.updateTask(task);
                        refreshAll();
                        setStatus("Task '" + task.getTitle() + "' marked as " + (task.isCompleted() ? "completed" : "pending"));
                    }
                });
            }

            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                    setText(null);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Title Column (strike through if completed)
        colTitle.setCellValueFactory(cellData -> {
            if (cellData == null || cellData.getValue() == null) {
                return null;
            }
            return new SimpleStringProperty(cellData.getValue().getTitle());
        });
        colTitle.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    Task task = getTableRow().getItem();
                    if (task != null) {
                        setText(item);
                        setGraphic(null);
                        if (task.isCompleted()) {
                            setStyle("-fx-text-fill: -color-text-muted; -fx-underline: false; -fx-opacity: 0.6;");
                        } else {
                            setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold;");
                        }
                    } else {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    }
                }
            }
        });

        // Category Column with custom colored tag circle
        colCategory.setCellValueFactory(cellData -> {
            if (cellData == null || cellData.getValue() == null) {
                return null;
            }
            return new SimpleStringProperty(
                    cellData.getValue().getCategoryName() != null ? cellData.getValue().getCategoryName() : "Uncategorized"
            );
        });
        colCategory.setCellFactory(column -> new TableCell<>() {
            private final HBox container = new HBox(8);
            private final Circle colorIndicator = new Circle(5);
            private final Label nameLabel = new Label();
            {
                container.setAlignment(Pos.CENTER_LEFT);
                container.getChildren().addAll(colorIndicator, nameLabel);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    nameLabel.setText(item);
                    nameLabel.setStyle("-fx-text-fill: -color-text-primary;");
                    
                    TaskCategory cat = taskService.getCategoryByName(item);
                    if (cat != null && cat.getColorHex() != null) {
                        colorIndicator.setFill(Color.web(cat.getColorHex()));
                        colorIndicator.setVisible(true);
                    } else {
                        colorIndicator.setFill(Color.web("#94A3B8")); // neutral slate
                        if ("Uncategorized".equals(item)) {
                            colorIndicator.setVisible(false);
                        } else {
                            colorIndicator.setVisible(true);
                        }
                    }
                    setGraphic(container);
                    setText(null);
                }
            }
        });

        // Priority Column with colored badge styling
        colPriority.setCellValueFactory(cellData -> {
            if (cellData == null || cellData.getValue() == null) {
                return null;
            }
            return new SimpleObjectProperty<>(cellData.getValue().getPriority());
        });
        colPriority.setCellFactory(column -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.getStyleClass().add("card-badge");
                badge.setMinWidth(80);
                badge.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(PriorityLevel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    badge.setText(item.getDisplayName().toUpperCase());
                    badge.setStyle("-fx-background-color: " + item.getColorHex() + "; -fx-text-fill: white;");
                    setGraphic(badge);
                    setText(null);
                    setAlignment(Pos.CENTER_LEFT);
                }
            }
        });

        // Due Date Column with colored warning if overdue
        colDueDate.setCellValueFactory(cellData -> {
            if (cellData == null || cellData.getValue() == null) {
                return null;
            }
            return new SimpleObjectProperty<>(cellData.getValue().getDueDate());
        });
        colDueDate.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else if (item == null) {
                    setText("No Due Date");
                    setGraphic(null);
                    setStyle("-fx-text-fill: -color-text-muted;");
                } else {
                    setText(item.format(formatter));
                    setGraphic(null);
                    Task task = getTableRow().getItem();
                    if (task != null) {
                        if (!task.isCompleted() && item.isBefore(LocalDate.now())) {
                            setStyle("-fx-text-fill: -color-danger; -fx-font-weight: bold;");
                        } else if (!task.isCompleted() && item.equals(LocalDate.now())) {
                            setStyle("-fx-text-fill: -color-warning; -fx-font-weight: bold;");
                        } else {
                            setStyle("-fx-text-fill: -color-text-secondary;");
                        }
                    } else {
                        setStyle("-fx-text-fill: -color-text-secondary;");
                    }
                }
            }
        });
    }

    private void setupCategoriesTable() {
        colCatName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        
        colCatColor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getColorHex()));
        colCatColor.setCellFactory(column -> new TableCell<>() {
            private final Circle circle = new Circle(8);
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    circle.setFill(Color.web(item));
                    setGraphic(circle);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    // --- View Navigation ---

    @FXML
    private void showDashboard() {
        setAllPanesInvisible();
        paneDashboard.setVisible(true);
        setActiveSidebarButton(btnDashboard);
        refreshDashboard();
    }

    @FXML
    private void showTasks() {
        setAllPanesInvisible();
        paneTasks.setVisible(true);
        setActiveSidebarButton(btnTasks);
        applyFilters();
    }

    @FXML
    private void showCategories() {
        setAllPanesInvisible();
        paneCategories.setVisible(true);
        setActiveSidebarButton(btnCategories);
        refreshCategoriesList();
    }

    @FXML
    private void showSettings() {
        setAllPanesInvisible();
        paneSettings.setVisible(true);
        setActiveSidebarButton(btnSettings);
    }

    private void setAllPanesInvisible() {
        paneDashboard.setVisible(false);
        paneTasks.setVisible(false);
        paneCategories.setVisible(false);
        paneSettings.setVisible(false);
    }

    private void setActiveSidebarButton(Button activeButton) {
        btnDashboard.getStyleClass().remove("sidebar-button-active");
        btnTasks.getStyleClass().remove("sidebar-button-active");
        btnCategories.getStyleClass().remove("sidebar-button-active");
        btnSettings.getStyleClass().remove("sidebar-button-active");

        activeButton.getStyleClass().add("sidebar-button-active");
    }

    // --- Action Handlers ---

    @FXML
    private void handleThemeToggle() {
        Preferences prefs = Preferences.userNodeForPackage(com.taskmanager.App.class);
        if (toggleDarkMode.isSelected()) {
            prefs.put("theme_mode", "Dark Mode");
        } else {
            prefs.put("theme_mode", "Light Mode");
        }
        applyPreferencesTheme();
    }

    @FXML
    private void handleAddTask() {
        Preferences prefs = Preferences.userNodeForPackage(com.taskmanager.App.class);
        String defaultPriorityStr = prefs.get("default_priority", "MEDIUM");
        PriorityLevel defaultPriority;
        try {
            defaultPriority = PriorityLevel.valueOf(defaultPriorityStr);
        } catch (IllegalArgumentException e) {
            defaultPriority = PriorityLevel.MEDIUM;
        }

        Task newTask = new Task(
                UUID.randomUUID().toString(),
                "",
                "",
                null,
                defaultPriority,
                null,
                false
        );
        
        boolean saved = showTaskDialog(newTask, "Create New Task");
        if (saved) {
            taskService.addTask(newTask);
            refreshAll();
            setStatus("Task '" + newTask.getTitle() + "' successfully created.");
        }
    }

    @FXML
    private void handleEditTask() {
        Task selected = tableTasks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningDialog("No Task Selected", "Please select a task from the table to edit.");
            return;
        }

        // Clone task to support cancel operations
        Task clone = new Task(
                selected.getId(),
                selected.getTitle(),
                selected.getDescription(),
                selected.getDueDate(),
                selected.getPriority(),
                selected.getCategoryName(),
                selected.isCompleted(),
                selected.getCreatedAt()
        );

        boolean saved = showTaskDialog(clone, "Edit Task Details");
        if (saved) {
            taskService.updateTask(clone);
            refreshAll();
            setStatus("Task '" + clone.getTitle() + "' successfully updated.");
        }
    }

    @FXML
    private void handleDeleteTask() {
        Task selected = tableTasks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningDialog("No Task Selected", "Please select a task from the table to delete.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Task: " + selected.getTitle());
        alert.setContentText("Are you sure you want to permanently delete this task?");
        
        // Match dialog theme
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        applyThemeToStage(stage);

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            taskService.deleteTask(selected.getId());
            refreshAll();
            setStatus("Task '" + selected.getTitle() + "' successfully deleted.");
        }
    }

    @FXML
    private void handleToggleStatus() {
        Task selected = tableTasks.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningDialog("No Task Selected", "Please select a task from the table to toggle its status.");
            return;
        }
        selected.setCompleted(!selected.isCompleted());
        taskService.updateTask(selected);
        refreshAll();
        setStatus("Task '" + selected.getTitle() + "' marked as " + (selected.isCompleted() ? "completed" : "pending"));
    }

    @FXML
    private void handleSaveCategory() {
        String name = txtCategoryName.getText();
        Color color = cpCategoryColor.getValue();

        lblCategoryError.setVisible(false);
        lblCategoryError.setManaged(false);

        if (name == null || name.trim().isEmpty()) {
            lblCategoryError.setText("Category name cannot be empty.");
            lblCategoryError.setVisible(true);
            lblCategoryError.setManaged(true);
            return;
        }

        String hex = String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255)
        );

        try {
            TaskCategory category = new TaskCategory(UUID.randomUUID().toString(), name.trim(), hex);
            taskService.addCategory(category);
            txtCategoryName.clear();
            refreshCategoriesList();
            refreshCategoryFilterComboBox();
            refreshDashboard();
            setStatus("Category '" + name + "' successfully created.");
        } catch (IllegalArgumentException e) {
            lblCategoryError.setText(e.getMessage());
            lblCategoryError.setVisible(true);
            lblCategoryError.setManaged(true);
        }
    }

    @FXML
    private void handleDeleteCategory() {
        TaskCategory selected = tableCategories.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarningDialog("No Category Selected", "Please select a category to delete.");
            return;
        }

        // Avoid deleting default categories that are currently referenced by active tasks without warning
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Category Deletion");
        alert.setHeaderText("Delete Category: " + selected.getName());
        alert.setContentText("Warning: Any task belonging to this category will be updated to 'Uncategorized'. Do you want to proceed?");

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        applyThemeToStage(stage);

        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            taskService.deleteCategory(selected.getId());
            refreshCategoriesList();
            refreshCategoryFilterComboBox();
            refreshAll();
            setStatus("Category '" + selected.getName() + "' successfully deleted.");
        }
    }

    // --- Helper UI Windows ---

    private boolean showTaskDialog(Task task, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmanager/fxml/task_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle(title);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(tableTasks.getScene().getWindow());
            
            // Set size boundaries
            dialogStage.setMinWidth(480);
            dialogStage.setMinHeight(560);

            Scene scene = new Scene(root);
            applyThemeToStage(dialogStage);
            dialogStage.setScene(scene);

            TaskDialogController controller = loader.getController();
            controller.setDialogStage(dialogStage);
            controller.setCategories(taskService.getAllCategories());
            controller.setTask(task);

            dialogStage.showAndWait();
            return controller.isSaveClicked();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Loading Error", "Could not load dialog layout file: " + e.getMessage());
            return false;
        }
    }

    private void applyThemeToStage(Stage stage) {
        Scene scene = stage.getScene();
        if (scene == null) return;
        
        Preferences prefs = Preferences.userNodeForPackage(com.taskmanager.App.class);
        String themeMode = prefs.get("theme_mode", "System Default");

        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/com/taskmanager/css/style.css").toExternalForm());
        
        if ("Dark Mode".equals(themeMode)) {
            scene.getStylesheets().add(getClass().getResource("/com/taskmanager/css/dark.css").toExternalForm());
        }
    }

    private void showWarningDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        applyThemeToStage(stage);
        alert.showAndWait();
    }

    private void showErrorDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        applyThemeToStage(stage);
        alert.showAndWait();
    }

    private void applyFilters() {
        String query = txtSearch.getText();
        String category = comboCategoryFilter.getValue();
        PriorityLevel priority = comboPriorityFilter.getValue();
        String statusText = comboStatusFilter.getValue();

        Boolean completed = null;
        if ("Completed".equals(statusText)) {
            completed = true;
        } else if ("Pending".equals(statusText)) {
            completed = false;
        }

        final Boolean finalCompleted = completed;
        filteredTasks.setPredicate(task -> {
            // Search query filter
            if (query != null && !query.trim().isEmpty()) {
                String lowerQuery = query.toLowerCase();
                boolean matchTitle = task.getTitle() != null && task.getTitle().toLowerCase().contains(lowerQuery);
                boolean matchDesc = task.getDescription() != null && task.getDescription().toLowerCase().contains(lowerQuery);
                if (!matchTitle && !matchDesc) {
                    return false;
                }
            }
            // Category filter
            if (category != null && !category.trim().isEmpty() && !"All Categories".equalsIgnoreCase(category)) {
                if ("Uncategorized".equalsIgnoreCase(category)) {
                    if (task.getCategoryName() != null) {
                        return false;
                    }
                } else {
                    if (!category.equalsIgnoreCase(task.getCategoryName())) {
                        return false;
                    }
                }
            }
            // Priority filter
            if (priority != null) {
                if (task.getPriority() != priority) {
                    return false;
                }
            }
            // Status filter
            if (finalCompleted != null) {
                if (task.isCompleted() != finalCompleted) {
                    return false;
                }
            }
            return true;
        });
    }

    private void refreshAll() {
        masterTasks.setAll(taskService.getAllTasks());
        applyFilters();
        refreshDashboard();
        refreshStatusSummary();
    }

    private void refreshDashboard() {
        Map<String, Object> stats = taskService.getStatistics();

        // Cards
        lblTotalTasks.setText(String.valueOf(stats.get("total")));
        lblCompletedTasks.setText(String.valueOf(stats.get("completed")));
        lblPendingTasks.setText(String.valueOf(stats.get("pending")));
        lblOverdueTasks.setText(String.valueOf(stats.get("overdue")));

        // Progress Bar
        double completionRate = (double) stats.get("completionRate");
        pbCompletion.setProgress(completionRate);
        lblCompletionPercent.setText(Math.round(completionRate * 100) + "%");

        // Priority breakdown VBox
        boxPriorityBreakdown.getChildren().clear();
        Map<PriorityLevel, Long> priorityBreakdown = (Map<PriorityLevel, Long>) stats.get("priorityBreakdown");
        int total = (int) stats.get("total");

        for (PriorityLevel p : PriorityLevel.values()) {
            long count = priorityBreakdown.getOrDefault(p, 0L);
            double fraction = total > 0 ? (double) count / total : 0.0;

            HBox row = new HBox(12);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPrefHeight(26);

            Label badge = new Label(p.getDisplayName().toUpperCase());
            badge.setMinWidth(80);
            badge.setAlignment(Pos.CENTER);
            badge.setStyle("-fx-background-color: " + p.getColorHex() + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px; -fx-background-radius: 10px; -fx-padding: 2px 6px;");

            ProgressBar bar = new ProgressBar(fraction);
            bar.setPrefWidth(180);
            bar.setStyle("-fx-accent: " + p.getColorHex() + ";");
            HBox.setHgrow(bar, Priority.ALWAYS);

            Label countLabel = new Label(count + " (" + Math.round(fraction * 100) + "%)");
            countLabel.setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold; -fx-font-size: 12px;");

            row.getChildren().addAll(badge, bar, countLabel);
            boxPriorityBreakdown.getChildren().add(row);
        }

        // Category breakdown VBox
        boxCategoryBreakdown.getChildren().clear();
        Map<String, Long> categoryBreakdown = (Map<String, Long>) stats.get("categoryBreakdown");

        if (categoryBreakdown.isEmpty()) {
            Label placeholder = new Label("No categories with active tasks.");
            placeholder.setStyle("-fx-text-fill: -color-text-muted; -fx-font-style: italic;");
            boxCategoryBreakdown.getChildren().add(placeholder);
        } else {
            categoryBreakdown.forEach((catName, count) -> {
                double fraction = total > 0 ? (double) count / total : 0.0;

                HBox row = new HBox(12);
                row.setAlignment(Pos.CENTER_LEFT);
                row.setPrefHeight(26);

                TaskCategory cat = taskService.getCategoryByName(catName);
                String colorHex = (cat != null) ? cat.getColorHex() : "#94A3B8";

                Label label = new Label(catName);
                label.setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold; -fx-font-size: 12px;");
                label.setMinWidth(100);

                ProgressBar bar = new ProgressBar(fraction);
                bar.setPrefWidth(160);
                bar.setStyle("-fx-accent: " + colorHex + ";");
                HBox.setHgrow(bar, Priority.ALWAYS);

                Label countLabel = new Label(count + " (" + Math.round(fraction * 100) + "%)");
                countLabel.setStyle("-fx-text-fill: -color-text-primary; -fx-font-weight: bold; -fx-font-size: 12px;");

                row.getChildren().addAll(label, bar, countLabel);
                boxCategoryBreakdown.getChildren().add(row);
            });
        }
    }

    private void refreshCategoriesList() {
        List<TaskCategory> cats = taskService.getAllCategories();
        observableCategories.setAll(cats);
        tableCategories.setItems(observableCategories);
    }

    private void refreshStatusSummary() {
        Map<String, Object> stats = taskService.getStatistics();
        int total = (int) stats.get("total");
        int completed = (int) stats.get("completed");
        int pending = (int) stats.get("pending");
        int overdue = (int) stats.get("overdue");

        lblStatsSummary.setText(String.format("%d Tasks | %d Completed | %d Pending | %d Overdue", total, completed, pending, overdue));
    }

    private void setStatus(String message) {
        lblStatus.setText(message);
    }

    @FXML
    private void handleExit() {
        Stage stage = (Stage) btnDashboard.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleAbout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmanager/fxml/about_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("About TaskManager");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnDashboard.getScene().getWindow());
            dialogStage.setResizable(false);

            Scene scene = new Scene(root);
            applyThemeToStage(dialogStage);
            dialogStage.setScene(scene);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Loading Error", "Could not load About dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handlePreferences() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmanager/fxml/preferences_dialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Preferences");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(btnDashboard.getScene().getWindow());
            dialogStage.setResizable(false);

            Scene scene = new Scene(root);
            applyThemeToStage(dialogStage);
            dialogStage.setScene(scene);

            PreferencesDialogController controller = loader.getController();
            controller.setMainController(this);

            dialogStage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
            showErrorDialog("Loading Error", "Could not load Preferences dialog: " + e.getMessage());
        }
    }

    @FXML
    private void handleThemeToggleShortcut() {
        toggleDarkMode.setSelected(!toggleDarkMode.isSelected());
        handleThemeToggle();
    }

    public void applyPreferencesTheme() {
        Preferences prefs = Preferences.userNodeForPackage(com.taskmanager.App.class);
        String themeMode = prefs.get("theme_mode", "System Default");

        Scene scene = btnDashboard.getScene();
        if (scene == null) return;

        scene.getStylesheets().clear();
        scene.getStylesheets().add(getClass().getResource("/com/taskmanager/css/style.css").toExternalForm());

        if ("Dark Mode".equals(themeMode)) {
            scene.getStylesheets().add(getClass().getResource("/com/taskmanager/css/dark.css").toExternalForm());
            toggleDarkMode.setSelected(true);
            setStatus("Theme toggled to Dark Mode.");
        } else {
            toggleDarkMode.setSelected(false);
            setStatus("Theme toggled to Light Mode.");
        }
    }

    private void setupMacMenuHandlers() {
        try {
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                if (java.awt.Desktop.isDesktopSupported()) {
                    java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                    
                    if (desktop.isSupported(java.awt.Desktop.Action.APP_ABOUT)) {
                        desktop.setAboutHandler(e -> {
                            javafx.application.Platform.runLater(this::handleAbout);
                        });
                    }
                    
                    if (desktop.isSupported(java.awt.Desktop.Action.APP_PREFERENCES)) {
                        desktop.setPreferencesHandler(e -> {
                            javafx.application.Platform.runLater(this::handlePreferences);
                        });
                    }
                }
            }
        } catch (Throwable t) {
            // Fail-safe to avoid crash if unsupported on platform
            t.printStackTrace();
        }
    }
}

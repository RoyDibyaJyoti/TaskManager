package com.taskmanager.controller;

import com.taskmanager.model.PriorityLevel;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskCategory;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class TaskDialogController {

    @FXML private Label lblDialogTitle;
    @FXML private TextField txtTitle;
    @FXML private TextArea txtDescription;
    @FXML private ComboBox<PriorityLevel> comboPriority;
    @FXML private ComboBox<String> comboCategory;
    @FXML private DatePicker dpDueDate;

    @FXML private Label lblTitleError;
    @FXML private Label lblPriorityError;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    private Stage dialogStage;
    private Task task;
    private boolean saveClicked = false;

    @FXML
    public void initialize() {
        // Populate Priorities combobox
        comboPriority.setItems(FXCollections.observableArrayList(PriorityLevel.values()));
        
        // Hide errors initially
        hideError(txtTitle, lblTitleError);
        hideError(comboPriority, lblPriorityError);

        // Bind listeners to clear errors on typing/selection
        txtTitle.textProperty().addListener((obs, oldVal, newVal) -> hideError(txtTitle, lblTitleError));
        comboPriority.valueProperty().addListener((obs, oldVal, newVal) -> hideError(comboPriority, lblPriorityError));
    }

    public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }

    public void setCategories(List<TaskCategory> categories) {
        // Populate Categories combobox
        comboCategory.getItems().clear();
        comboCategory.getItems().add(null); // Represents Uncategorized/None
        for (TaskCategory category : categories) {
            comboCategory.getItems().add(category.getName());
        }
        
        // Custom rendering for null/empty category
        comboCategory.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "None (Uncategorized)" : item);
            }
        });
        comboCategory.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "None (Uncategorized)" : item);
            }
        });
    }

    public void setTask(Task task) {
        this.task = task;

        if (task.getTitle() != null && !task.getTitle().isEmpty()) {
            lblDialogTitle.setText("Edit Task Details");
            btnSave.setText("Save Changes");
        } else {
            lblDialogTitle.setText("Create New Task");
            btnSave.setText("Create Task");
        }

        txtTitle.setText(task.getTitle());
        txtDescription.setText(task.getDescription());
        comboPriority.setValue(task.getPriority());
        comboCategory.setValue(task.getCategoryName());
        dpDueDate.setValue(task.getDueDate());
    }

    public boolean isSaveClicked() {
        return saveClicked;
    }

    @FXML
    private void handleSave() {
        if (isInputValid()) {
            task.setTitle(txtTitle.getText().trim());
            task.setDescription(txtDescription.getText() != null ? txtDescription.getText().trim() : "");
            task.setPriority(comboPriority.getValue());
            task.setCategoryName(comboCategory.getValue());
            task.setDueDate(dpDueDate.getValue());

            saveClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        boolean valid = true;

        if (txtTitle.getText() == null || txtTitle.getText().trim().isEmpty()) {
            showError(txtTitle, lblTitleError, "Task title is required.");
            valid = false;
        }

        if (comboPriority.getValue() == null) {
            showError(comboPriority, lblPriorityError, "Please select a priority level.");
            valid = false;
        }

        return valid;
    }

    private void showError(Control inputControl, Label errorLabel, String message) {
        inputControl.getStyleClass().add("validation-error");
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void hideError(Control inputControl, Label errorLabel) {
        inputControl.getStyleClass().remove("validation-error");
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }
}

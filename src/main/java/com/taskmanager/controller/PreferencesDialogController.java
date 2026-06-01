package com.taskmanager.controller;

import com.taskmanager.model.PriorityLevel;
import com.taskmanager.App;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class PreferencesDialogController {

    @FXML private ComboBox<String> comboTheme;
    @FXML private ComboBox<PriorityLevel> comboDefaultPriority;
    @FXML private Button btnClose;

    private MainController mainController;

    @FXML
    public void initialize() {
        // Theme Options
        comboTheme.setItems(FXCollections.observableArrayList("System Default", "Light Mode", "Dark Mode"));
        
        // Priority Options
        comboDefaultPriority.setItems(FXCollections.observableArrayList(PriorityLevel.values()));

        // Load current Preferences
        Preferences prefs = Preferences.userNodeForPackage(App.class);
        String theme = prefs.get("theme_mode", "System Default");
        String defaultPriorityStr = prefs.get("default_priority", "MEDIUM");
        
        comboTheme.setValue(theme);
        try {
            comboDefaultPriority.setValue(PriorityLevel.valueOf(defaultPriorityStr));
        } catch (IllegalArgumentException e) {
            comboDefaultPriority.setValue(PriorityLevel.MEDIUM);
        }

        // Add listeners to auto-save upon selection changes
        comboTheme.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                prefs.put("theme_mode", newVal);
                if (mainController != null) {
                    mainController.applyPreferencesTheme();
                }
            }
        });

        comboDefaultPriority.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                prefs.put("default_priority", newVal.name());
            }
        });
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}

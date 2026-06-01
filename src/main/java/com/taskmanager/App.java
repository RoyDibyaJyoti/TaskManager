package com.taskmanager;

import com.taskmanager.controller.MainController;
import com.taskmanager.repository.JsonTaskRepository;
import com.taskmanager.repository.TaskRepository;
import com.taskmanager.service.TaskService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Instantiate Repository and Service (Dependency Injection)
        TaskRepository repository = new JsonTaskRepository();
        TaskService service = new TaskService(repository);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/taskmanager/fxml/main.fxml"));
        Parent root = loader.load();
        
        // Inject Service and initialize Controller (MVC Pattern)
        MainController controller = loader.getController();
        controller.init(service);
        
        primaryStage.setTitle("TaskManager - Professional Desktop Suite");
        
        Scene scene = new Scene(root, 1024, 720);
        // Load main stylesheet
        scene.getStylesheets().add(getClass().getResource("/com/taskmanager/css/style.css").toExternalForm());
        
        primaryStage.setScene(scene);
        controller.applyPreferencesTheme();
        primaryStage.setMinWidth(960);
        primaryStage.setMinHeight(640);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

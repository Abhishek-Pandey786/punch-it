package com.attendancemanager;

import com.attendancemanager.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main application entry point.
 */
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Initialize database (this will handle migration if needed)
            DatabaseManager.getInstance();

            // Load Profile Selection screen first
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/attendancemanager/view/profile-selection.fxml"));
            Parent root = loader.load();

            // Create scene
            Scene scene = new Scene(root, 800, 600);

            // Add main stylesheet
            String stylesheet = getClass().getResource("/com/attendancemanager/css/styles.css").toExternalForm();
            scene.getStylesheets().add(stylesheet);

            // Set up stage
            primaryStage.setTitle("Punch IT - Select Your Profile");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(700);
            primaryStage.setMinHeight(500);

            // Set custom app icon
            try {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/com/attendancemanager/icons/app-icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception iconError) {
                System.err.println("Could not load app icon: " + iconError.getMessage());
            }

            // Show application
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        // Clean up database connections when application closes
        DatabaseManager.getInstance().closeConnections();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

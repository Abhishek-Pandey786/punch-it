package com.attendancemanager.controller;

import com.attendancemanager.context.UserContext;
import com.attendancemanager.dao.ProfileDAO;
import com.attendancemanager.model.Profile;
import com.attendancemanager.service.ProfileService;
import com.attendancemanager.util.DatabaseManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the profile selection screen.
 * Displays available profiles and allows profile management.
 */
public class ProfileSelectionController {

    @FXML
    private VBox profilesContainer;

    private ProfileService profileService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    @FXML
    public void initialize() {
        // Initialize profile service
        DatabaseManager dbManager = DatabaseManager.getInstance();
        ProfileDAO profileDAO = new ProfileDAO(dbManager.getConfigConnection());
        profileService = new ProfileService(profileDAO);

        // Ensure at least one profile exists
        profileService.ensureDefaultProfileExists();

        // Load profiles
        loadProfiles();
    }

    /**
     * Load and display all profiles.
     */
    private void loadProfiles() {
        profilesContainer.getChildren().clear();
        List<Profile> profiles = profileService.getAllProfiles();

        for (Profile profile : profiles) {
            profilesContainer.getChildren().add(createProfileCard(profile));
        }

        if (profiles.isEmpty()) {
            Label emptyLabel = new Label("No profiles found. Click 'Add New Profile' to create one.");
            emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b; -fx-padding: 20;");
            profilesContainer.getChildren().add(emptyLabel);
        }
    }

    /**
     * Create a profile card UI component.
     */
    private HBox createProfileCard(Profile profile) {
        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffffff, #fffbf8); " +
                        "-fx-border-color: #fed7aa; -fx-border-width: 2; " +
                        "-fx-border-radius: 15; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 10, 0, 0, 3); " +
                        "-fx-min-width: 600; -fx-max-width: 600; -fx-cursor: hand;");

        // Profile icon
        Text icon = new Text("👤");
        icon.setStyle("-fx-font-size: 48px;");

        // Profile info
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Text nameText = new Text(profile.getName());
        nameText.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-fill: #1e293b;");

        Text dateText = new Text("Created: " + profile.getCreatedAt().format(DATE_FORMATTER));
        dateText.setStyle("-fx-font-size: 12px; -fx-fill: #64748b;");

        Text lastAccessText = new Text("Last accessed: " + profile.getLastAccessedAt().format(DATE_FORMATTER));
        lastAccessText.setStyle("-fx-font-size: 12px; -fx-fill: #64748b;");

        infoBox.getChildren().addAll(nameText, dateText, lastAccessText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action buttons
        VBox buttonsBox = new VBox(8);
        buttonsBox.setAlignment(Pos.CENTER);

        Button selectBtn = new Button("✓ Select");
        selectBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #fb923c, #f97316); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.3), 8, 0, 0, 2);");
        selectBtn.setOnMouseEntered(e -> selectBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #f97316, #ea580c); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.4), 10, 0, 0, 3); -fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        selectBtn.setOnMouseExited(e -> selectBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #fb923c, #f97316); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 24; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.3), 8, 0, 0, 2);"));
        selectBtn.setOnAction(e -> handleSelectProfile(profile));

        HBox actionButtons = new HBox(8);
        actionButtons.setAlignment(Pos.CENTER);

        Button editBtn = new Button("✏️");
        editBtn.setStyle(
                "-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; " +
                        "-fx-font-size: 16px; -fx-padding: 8; -fx-background-radius: 6; -fx-cursor: hand;");
        editBtn.setOnAction(e -> handleEditProfile(profile));

        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle(
                "-fx-background-color: #fee2e2; -fx-text-fill: #ef4444; " +
                        "-fx-font-size: 16px; -fx-padding: 8; -fx-background-radius: 6; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> handleDeleteProfile(profile));

        actionButtons.getChildren().addAll(editBtn, deleteBtn);
        buttonsBox.getChildren().addAll(selectBtn, actionButtons);

        card.getChildren().addAll(icon, infoBox, spacer, buttonsBox);

        // Add hover effect
        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: linear-gradient(to right, #fffbf8, #fef5f1); " +
                        "-fx-border-color: #fb923c; -fx-border-width: 2; " +
                        "-fx-border-radius: 15; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.3), 12, 0, 0, 4); " +
                        "-fx-min-width: 600; -fx-max-width: 600; -fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffffff, #fffbf8); " +
                        "-fx-border-color: #fed7aa; -fx-border-width: 2; " +
                        "-fx-border-radius: 15; -fx-background-radius: 15; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 10, 0, 0, 3); " +
                        "-fx-min-width: 600; -fx-max-width: 600; -fx-cursor: hand;"));

        return card;
    }

    /**
     * Handle profile selection - open dashboard with selected profile.
     */
    private void handleSelectProfile(Profile profile) {
        try {
            // Set current profile in context
            UserContext.getInstance().setCurrentProfile(profile);

            // Update last accessed timestamp
            profileService.updateLastAccessed(profile.getId());

            // Switch database to selected profile
            DatabaseManager.getInstance().switchProfile(profile.getId());

            // Load dashboard
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/attendancemanager/view/dashboard.fxml"));
            Parent root = loader.load();

            // Get current stage
            Stage stage = (Stage) profilesContainer.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("📊 Punch IT - " + profile.getName());
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.centerOnScreen();

        } catch (Exception e) {
            System.err.println("Error loading dashboard: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load dashboard: " + e.getMessage());
        }
    }

    /**
     * Handle add new profile.
     */
    @FXML
    private void handleAddProfile() {
        Dialog<String> dialog = createProfileDialog(null);
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                Profile newProfile = profileService.createProfile(name);
                loadProfiles();
                showAlert(Alert.AlertType.INFORMATION, "Success",
                        "Profile '" + newProfile.getName() + "' created successfully!");
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to create profile: " + e.getMessage());
            }
        });
    }

    /**
     * Handle edit profile.
     */
    private void handleEditProfile(Profile profile) {
        Dialog<String> dialog = createProfileDialog(profile.getName());
        Optional<String> result = dialog.showAndWait();

        result.ifPresent(name -> {
            try {
                profile.setName(name);
                if (profileService.updateProfile(profile)) {
                    loadProfiles();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile updated successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to update profile.");
                }
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
            }
        });
    }

    /**
     * Handle delete profile.
     */
    private void handleDeleteProfile(Profile profile) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete profile '" + profile.getName() + "'?");
        confirmation.setContentText(
                "This will permanently delete all attendance data for this profile. " +
                        "This action cannot be undone.\n\nAre you sure?");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (profileService.deleteProfile(profile.getId())) {
                    // Delete profile database file
                    java.io.File dbFile = new java.io.File("profile_" + profile.getId() + ".db");
                    if (dbFile.exists()) {
                        dbFile.delete();
                    }

                    loadProfiles();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Profile deleted successfully!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete profile.");
                }
            } catch (IllegalArgumentException e) {
                showAlert(Alert.AlertType.ERROR, "Cannot Delete", e.getMessage());
            }
        }
    }

    /**
     * Create profile name input dialog.
     */
    private Dialog<String> createProfileDialog(String existingName) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(existingName == null ? "Add New Profile" : "Edit Profile");
        dialog.setHeaderText(existingName == null ? "Enter profile name" : "Update profile name");

        dialog.getDialogPane().setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #fffbf8); " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 15, 0, 0, 5);");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));

        Label label = new Label("Profile Name:");
        label.setStyle("-fx-font-weight: 600; -fx-font-size: 14px;");

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., John's Attendance");
        nameField.setStyle(
                "-fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; " +
                        "-fx-border-color: #fed7aa; -fx-font-size: 14px; -fx-pref-width: 300;");

        if (existingName != null) {
            nameField.setText(existingName);
        }

        content.getChildren().addAll(label, nameField);
        dialog.getDialogPane().setContent(content);

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Profile name cannot be empty.");
                    return null;
                }
                return name;
            }
            return null;
        });

        return dialog;
    }

    /**
     * Handle exit button - close application.
     */
    @FXML
    private void handleExit() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Exit Application");
        confirmation.setHeaderText("Are you sure you want to exit?");
        confirmation.setContentText("The application will close.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Platform.exit();
        }
    }

    /**
     * Show alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

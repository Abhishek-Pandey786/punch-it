package com.attendancemanager.controller;

import com.attendancemanager.component.AnimatedCircle;
import com.attendancemanager.context.UserContext;
import com.attendancemanager.model.Subject;
import com.attendancemanager.service.AttendanceService;
import com.attendancemanager.service.AttendanceService.AttendanceStatusLevel;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the main dashboard view.
 */
public class DashboardController {

    @FXML
    private VBox subjectsContainer;
    @FXML
    private Label noSubjectsLabel;
    @FXML
    private StackPane totalSubjectsCircle;
    @FXML
    private StackPane totalClassesCircle;
    @FXML
    private StackPane attendedClassesCircle;
    @FXML
    private StackPane overallAttendanceCircle;
    @FXML
    private StackPane safeSubjectsCircle;
    @FXML
    private StackPane riskSubjectsCircle;
    @FXML
    private Text overallRecommendationText;
    @FXML
    private Button themeToggle;
    @FXML
    private Text profileNameText;

    private AttendanceService attendanceService;
    private boolean isDarkTheme = false;

    // Animated circle components
    private AnimatedCircle totalSubjectsAnimCircle;
    private AnimatedCircle totalClassesAnimCircle;
    private AnimatedCircle attendedClassesAnimCircle;
    private AnimatedCircle overallAttendanceAnimCircle;
    private AnimatedCircle safeSubjectsAnimCircle;
    private AnimatedCircle riskSubjectsAnimCircle;

    @FXML
    public void initialize() {
        attendanceService = new AttendanceService();
        initializeAnimatedCircles();
        updateProfileDisplay();
        loadDashboard();
    }

    private void initializeAnimatedCircles() {
        // Total Subjects - Vibrant Purple
        totalSubjectsAnimCircle = new AnimatedCircle(45, "#9333EA");
        totalSubjectsCircle.getChildren().add(totalSubjectsAnimCircle);

        // Total Classes - Vibrant Blue
        totalClassesAnimCircle = new AnimatedCircle(45, "#3B82F6");
        totalClassesCircle.getChildren().add(totalClassesAnimCircle);

        // Attended Classes - Vibrant Orange
        attendedClassesAnimCircle = new AnimatedCircle(45, "#F59E0B");
        attendedClassesCircle.getChildren().add(attendedClassesAnimCircle);

        // Overall Attendance - Vibrant Amber
        overallAttendanceAnimCircle = new AnimatedCircle(45, "#F59E0B");
        overallAttendanceCircle.getChildren().add(overallAttendanceAnimCircle);

        // Safe Subjects - Vibrant Green
        safeSubjectsAnimCircle = new AnimatedCircle(45, "#10B981");
        safeSubjectsCircle.getChildren().add(safeSubjectsAnimCircle);

        // At Risk - Vibrant Red
        riskSubjectsAnimCircle = new AnimatedCircle(45, "#EF4444");
        riskSubjectsCircle.getChildren().add(riskSubjectsAnimCircle);
    }

    /**
     * Load and display all subjects on the dashboard.
     */
    private void loadDashboard() {
        List<Subject> subjects = attendanceService.getAllSubjects();

        subjectsContainer.getChildren().clear();

        if (subjects.isEmpty()) {
            noSubjectsLabel.setVisible(true);
            noSubjectsLabel.setManaged(true);
        } else {
            noSubjectsLabel.setVisible(false);
            noSubjectsLabel.setManaged(false);

            for (Subject subject : subjects) {
                subjectsContainer.getChildren().add(createSubjectCard(subject));
            }
        }

        updateSummaryStatistics(subjects);
        updateLastUpdatedTime();
    }

    /**
     * Create a subject card with progress bar and intelligent predictions.
     */
    private VBox createSubjectCard(Subject subject) {
        VBox card = new VBox(10);
        card.getStyleClass().add("subject-card");
        card.setPadding(new Insets(15));

        // Get status level for color coding
        AttendanceStatusLevel statusLevel = attendanceService.getAttendanceStatusLevel(subject);
        String statusClass = switch (statusLevel) {
            case SAFE -> "status-safe";
            case WARNING -> "status-warning";
            case RISK -> "status-risk";
        };
        card.getStyleClass().add(statusClass);

        // Header with subject name and actions
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Text nameText = new Text(subject.getName());
        nameText.getStyleClass().add("subject-name");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button editBtn = new Button("⋮");
        editBtn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-background-color: #f1f5f9; " +
                "-fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8; " +
                "-fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        editBtn.setOnMouseEntered(e -> editBtn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-background-color: #e0e7ff; -fx-text-fill: #4f46e5; -fx-cursor: hand; -fx-padding: 8 12; " +
                "-fx-background-radius: 8; -fx-border-color: #c7d2fe; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(79,70,229,0.3), 6, 0, 0, 2); -fx-scale-y: 1.05; -fx-scale-x: 1.05;"));
        editBtn.setOnMouseExited(e -> editBtn.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; " +
                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; -fx-cursor: hand; -fx-padding: 8 12; " +
                "-fx-background-radius: 8; -fx-border-color: #e2e8f0; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"));
        editBtn.setOnAction(e -> handleEditSubject(subject));

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #fef2f2; -fx-text-fill: #ef4444; " +
                "-fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8; " +
                "-fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);");
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #fee2e2; " +
                "-fx-text-fill: #dc2626; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8; " +
                "-fx-border-color: #fca5a5; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 6, 0, 0, 2); -fx-scale-y: 1.05; -fx-scale-x: 1.05;"));
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle("-fx-font-size: 18px; -fx-background-color: #fef2f2; " +
                "-fx-text-fill: #ef4444; -fx-cursor: hand; -fx-padding: 8 12; -fx-background-radius: 8; " +
                "-fx-border-color: #fecaca; -fx-border-width: 1; -fx-border-radius: 8; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0, 0, 2);"));
        deleteBtn.setOnAction(e -> handleDeleteSubject(subject));

        header.getChildren().addAll(nameText, spacer, editBtn, deleteBtn);

        // Progress bar
        double percentage = subject.getCurrentPercentage();
        ProgressBar progressBar = new ProgressBar(percentage / 100.0);
        progressBar.getStyleClass().add("attendance-progress");
        progressBar.setPrefWidth(Double.MAX_VALUE);

        // Stats row
        HBox stats = new HBox(20);
        stats.setAlignment(Pos.CENTER_LEFT);

        Text attendanceText = new Text(String.format("%.1f%%", percentage));
        attendanceText.getStyleClass().add("percentage-text");

        Text classesText = new Text(String.format("%d/%d classes",
                subject.getAttendedClasses(), subject.getTotalClasses()));
        classesText.getStyleClass().add("classes-text");

        Text targetText = new Text(String.format("Target: %.0f%%", subject.getTargetPercentage()));
        targetText.getStyleClass().add("target-text");

        stats.getChildren().addAll(attendanceText, classesText, targetText);

        // Intelligent recommendation
        String recommendation = attendanceService.generateRecommendation(subject);
        Text recommendationText = new Text("💡 " + recommendation);
        recommendationText.getStyleClass().add("recommendation-text");
        recommendationText.setWrappingWidth(400);

        // Additional predictions
        int classesNeeded = attendanceService.calculateClassesNeededToReachTarget(subject);
        int safeBunks = attendanceService.calculateSafeBunks(subject);

        HBox predictions = new HBox(15);
        predictions.setAlignment(Pos.CENTER_LEFT);

        if (classesNeeded > 0) {
            Text needText = new Text("📚 Need: " + classesNeeded + " classes");
            needText.getStyleClass().add("prediction-text");
            predictions.getChildren().add(needText);
        }

        if (safeBunks > 0) {
            Text bunkText = new Text("✅ Safe bunks: " + safeBunks);
            bunkText.getStyleClass().add("prediction-text");
            predictions.getChildren().add(bunkText);
        }

        card.getChildren().addAll(header, progressBar, stats, recommendationText, predictions);

        return card;
    }

    /**
     * Update summary statistics at the top.
     */
    private void updateSummaryStatistics(List<Subject> subjects) {
        // Update total subjects
        totalSubjectsAnimCircle.setValue(String.valueOf(subjects.size()));

        if (subjects.isEmpty()) {
            totalClassesAnimCircle.setValue("0");
            attendedClassesAnimCircle.setValue("0");
            overallAttendanceAnimCircle.setValue("0%");
            safeSubjectsAnimCircle.setValue("0");
            riskSubjectsAnimCircle.setValue("0");
            return;
        }

        // Calculate overall attendance correctly: total attended / total classes across
        // ALL subjects
        int totalClassesAll = subjects.stream()
                .mapToInt(Subject::getTotalClasses)
                .sum();

        int totalAttendedAll = subjects.stream()
                .mapToInt(Subject::getAttendedClasses)
                .sum();

        // Update total classes circle
        totalClassesAnimCircle.setValue(String.valueOf(totalClassesAll));

        double totalAttendance = totalClassesAll > 0 ? (totalAttendedAll * 100.0) / totalClassesAll : 0.0;

        long safeCount = subjects.stream()
                .filter(s -> attendanceService.getAttendanceStatusLevel(s) == AttendanceStatusLevel.SAFE)
                .count();

        long riskCount = subjects.stream()
                .filter(s -> attendanceService.getAttendanceStatusLevel(s) == AttendanceStatusLevel.RISK)
                .count();

        // Update all circle values with real-time data
        attendedClassesAnimCircle.setValue(String.valueOf(totalAttendedAll));
        overallAttendanceAnimCircle.setValue(String.format("%.2f%%", totalAttendance));

        // Color code overall attendance based on target of 85%
        if (totalAttendance >= 85.0) {
            overallAttendanceAnimCircle.setColor("#10B981"); // Green
        } else if (totalAttendance >= 80.0) {
            overallAttendanceAnimCircle.setColor("#F59E0B"); // Amber
        } else {
            overallAttendanceAnimCircle.setColor("#EF4444"); // Red
        }

        safeSubjectsAnimCircle.setValue(String.valueOf(safeCount));
        riskSubjectsAnimCircle.setValue(String.valueOf(riskCount));

        // Update overall recommendation text
        updateOverallRecommendation(totalAttendedAll, totalClassesAll, totalAttendance);
    }

    private void updateOverallRecommendation(int attended, int total, double currentPercentage) {
        double targetPercentage = 85.0;

        if (currentPercentage >= targetPercentage) {
            int safeBunks = (int) Math.floor((attended * 100.0 / targetPercentage) - total);
            if (safeBunks > 0) {
                overallRecommendationText
                        .setText(String.format("🎉 You can safely bunk %d class%s and still maintain 85%%!",
                                safeBunks, safeBunks == 1 ? "" : "es"));
                overallRecommendationText.setStyle("-fx-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 600;");
            } else {
                overallRecommendationText
                        .setText("✅ Perfect! You're at your target. Attend all classes to maintain 85%!");
                overallRecommendationText.setStyle("-fx-fill: #10B981; -fx-font-size: 13px; -fx-font-weight: 600;");
            }
        } else {
            int classesNeeded = (int) Math
                    .ceil((targetPercentage * total / 100.0 - attended) / (1 - targetPercentage / 100.0));
            if (classesNeeded > 0) {
                overallRecommendationText
                        .setText(String.format("📚 Attend next %d class%s to reach 85%% overall attendance",
                                classesNeeded, classesNeeded == 1 ? "" : "es"));
                overallRecommendationText.setStyle("-fx-fill: #F59E0B; -fx-font-size: 13px; -fx-font-weight: 600;");
            } else {
                overallRecommendationText.setText("⚠️ Keep attending classes to improve your attendance!");
                overallRecommendationText.setStyle("-fx-fill: #EF4444; -fx-font-size: 13px; -fx-font-weight: 600;");
            }
        }
    }

    /**
     * Update last updated timestamp.
     */
    private void updateLastUpdatedTime() {
        // Timestamp display removed
    }

    @FXML
    private void handleAddSubject() {
        Dialog<Subject> dialog = createSubjectDialog(null);
        Optional<Subject> result = dialog.showAndWait();

        result.ifPresent(subject -> {
            if (attendanceService.createSubject(subject)) {
                loadDashboard();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Subject added successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to add subject. Name might already exist.");
            }
        });
    }

    private void handleEditSubject(Subject subject) {
        Dialog<Subject> dialog = createSubjectDialog(subject);
        Optional<Subject> result = dialog.showAndWait();

        result.ifPresent(updatedSubject -> {
            updatedSubject.setId(subject.getId());
            if (attendanceService.updateSubject(updatedSubject)) {
                loadDashboard();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Subject updated successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to update subject.");
            }
        });
    }

    private void handleDeleteSubject(Subject subject) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Delete");
        confirmation.setHeaderText("Delete " + subject.getName() + "?");
        confirmation.setContentText(
                "This will delete all attendance records for this subject. This action cannot be undone.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (attendanceService.deleteSubject(subject.getId())) {
                loadDashboard();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Subject deleted successfully!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete subject.");
            }
        }
    }

    /**
     * Create dialog for adding/editing subjects.
     */
    private Dialog<Subject> createSubjectDialog(Subject existingSubject) {
        Dialog<Subject> dialog = new Dialog<>();
        dialog.setTitle(existingSubject == null ? "✨ Add New Subject" : "✏️ Edit Subject");
        dialog.setHeaderText(existingSubject == null ? "Enter subject details" : "Update subject details");

        // Style the dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #fffbf8); " +
                "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 15, 0, 0, 5);");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Style buttons
        dialog.getDialogPane().lookupButton(saveButtonType).setStyle(
                "-fx-background-color: linear-gradient(to right, #fb923c, #f97316); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.3), 8, 0, 0, 2);");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                        "-fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 8; -fx-cursor: hand;");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("e.g., Mathematics");
        nameField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #fed7aa; -fx-font-size: 14px;");

        TextField totalField = new TextField();
        totalField.setPromptText("Total classes conducted");
        totalField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #fed7aa; -fx-font-size: 14px;");

        TextField attendedField = new TextField();
        attendedField.setPromptText("Classes attended");
        attendedField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #fed7aa; -fx-font-size: 14px;");

        TextField targetField = new TextField();
        targetField.setPromptText("Target percentage (default: 75)");
        targetField.setStyle("-fx-padding: 12; -fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #fed7aa; -fx-font-size: 14px;");

        if (existingSubject != null) {
            nameField.setText(existingSubject.getName());
            totalField.setText(String.valueOf(existingSubject.getTotalClasses()));
            attendedField.setText(String.valueOf(existingSubject.getAttendedClasses()));
            targetField.setText(String.valueOf(existingSubject.getTargetPercentage()));
        } else {
            targetField.setText("75.0");
        }

        Label nameLabel = new Label("📚 Subject Name:");
        nameLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);

        Label totalLabel = new Label("📊 Total Classes:");
        totalLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        grid.add(totalLabel, 0, 1);
        grid.add(totalField, 1, 1);

        Label attendedLabel = new Label("✅ Attended Classes:");
        attendedLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        grid.add(attendedLabel, 0, 2);
        grid.add(attendedField, 1, 2);

        Label targetLabel = new Label("🎯 Target %:");
        targetLabel.setStyle("-fx-font-weight: 600; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
        grid.add(targetLabel, 0, 3);
        grid.add(targetField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    String name = nameField.getText().trim();
                    int total = Integer.parseInt(totalField.getText().trim());
                    int attended = Integer.parseInt(attendedField.getText().trim());
                    double target = Double.parseDouble(targetField.getText().trim());

                    if (name.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Subject name cannot be empty.");
                        return null;
                    }

                    if (total < 0 || attended < 0 || attended > total) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Invalid class counts.");
                        return null;
                    }

                    if (target < 0 || target > 100) {
                        showAlert(Alert.AlertType.ERROR, "Validation Error", "Target must be between 0 and 100.");
                        return null;
                    }

                    return new Subject(0, name, total, attended, target, "#3498db");
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", "Please enter valid numbers.");
                    return null;
                }
            }
            return null;
        });

        return dialog;
    }

    @FXML
    private void handleMarkAttendance() {
        List<Subject> subjects = attendanceService.getAllSubjects();

        if (subjects.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Subjects", "Please add subjects first before marking attendance.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("✅ Mark Attendance");
        dialog.setHeaderText(
                "📅 Mark attendance for today: " + LocalDate.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")));

        // Style dialog pane
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #fffbf8); " +
                "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 15, 0, 0, 5);");

        ButtonType markButtonType = new ButtonType("💾 Save Attendance", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(markButtonType, ButtonType.CANCEL);

        // Style buttons
        dialog.getDialogPane().lookupButton(markButtonType).setStyle(
                "-fx-background-color: linear-gradient(to right, #fb923c, #f97316); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12 24; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.3), 8, 0, 0, 2);");
        dialog.getDialogPane().lookupButton(ButtonType.CANCEL).setStyle(
                "-fx-background-color: #f1f5f9; -fx-text-fill: #64748b; " +
                        "-fx-font-weight: bold; -fx-padding: 12 24; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px;");

        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-min-width: 650px; -fx-pref-width: 650px;");

        List<AttendanceMarkItem> markItems = new ArrayList<>();

        for (Subject subject : subjects) {
            HBox subjectRow = new HBox(20);
            subjectRow.setAlignment(Pos.CENTER_LEFT);
            subjectRow.setPadding(new Insets(12));
            subjectRow.setStyle(
                    "-fx-background-color: linear-gradient(to right, #ffffff, #fffbf8); " +
                            "-fx-background-radius: 10; -fx-border-color: #fed7aa; -fx-border-radius: 10; -fx-border-width: 2; "
                            +
                            "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.15), 8, 0, 0, 2);");

            // Subject name
            Label subjectName = new Label(subject.getName());
            subjectName.setStyle(
                    "-fx-font-size: 14px; -fx-font-weight: bold; -fx-min-width: 180px; -fx-text-fill: #2c3e50;");

            // Classes count display
            VBox countBox = new VBox(5);
            countBox.setAlignment(Pos.CENTER);
            Label countLabel = new Label("Classes");
            countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");

            HBox countsDisplay = new HBox(15);
            countsDisplay.setAlignment(Pos.CENTER);

            VBox presentDisplay = new VBox(2);
            presentDisplay.setAlignment(Pos.CENTER);
            Label presentCountLabel = new Label("0");
            presentCountLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #10B981;");
            Label presentText = new Label("Present");
            presentText.setStyle("-fx-font-size: 10px; -fx-text-fill: #10B981;");
            presentDisplay.getChildren().addAll(presentCountLabel, presentText);

            VBox absentDisplay = new VBox(2);
            absentDisplay.setAlignment(Pos.CENTER);
            Label absentCountLabel = new Label("0");
            absentCountLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #EF4444;");
            Label absentText = new Label("Absent");
            absentText.setStyle("-fx-font-size: 10px; -fx-text-fill: #EF4444;");
            absentDisplay.getChildren().addAll(absentCountLabel, absentText);

            countsDisplay.getChildren().addAll(presentDisplay, absentDisplay);
            countBox.getChildren().addAll(countLabel, countsDisplay);

            // Control buttons
            HBox controlsBox = new HBox(10);
            controlsBox.setAlignment(Pos.CENTER);

            Button plusBtn = new Button("+");
            plusBtn.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold; -fx-min-width: 55px; -fx-min-height: 55px; " +
                            "-fx-background-color: linear-gradient(to bottom, #10B981, #059669); -fx-text-fill: white; "
                            +
                            "-fx-background-radius: 12; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 8, 0, 0, 2);");
            plusBtn.setOnMouseEntered(e -> plusBtn.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold; -fx-min-width: 55px; -fx-min-height: 55px; " +
                            "-fx-background-color: linear-gradient(to bottom, #059669, #047857); -fx-text-fill: white; "
                            +
                            "-fx-background-radius: 12; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.5), 10, 0, 0, 3); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
            plusBtn.setOnMouseExited(e -> plusBtn.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold; -fx-min-width: 55px; -fx-min-height: 55px; " +
                            "-fx-background-color: linear-gradient(to bottom, #10B981, #059669); -fx-text-fill: white; "
                            +
                            "-fx-background-radius: 12; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 8, 0, 0, 2);"));
            plusBtn.setOnAction(e -> {
                int current = Integer.parseInt(presentCountLabel.getText());
                if (current < 10)
                    presentCountLabel.setText(String.valueOf(current + 1));
            });

            Button minusBtn = new Button("-");
            minusBtn.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold; -fx-min-width: 55px; -fx-min-height: 55px; " +
                            "-fx-background-color: linear-gradient(to bottom, #EF4444, #DC2626); -fx-text-fill: white; "
                            +
                            "-fx-background-radius: 12; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.4), 8, 0, 0, 2);");
            minusBtn.setOnMouseEntered(e -> minusBtn.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold; -fx-min-width: 55px; -fx-min-height: 55px; " +
                            "-fx-background-color: linear-gradient(to bottom, #DC2626, #B91C1C); -fx-text-fill: white; "
                            +
                            "-fx-background-radius: 12; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.5), 10, 0, 0, 3); -fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
            minusBtn.setOnMouseExited(e -> minusBtn.setStyle(
                    "-fx-font-size: 22px; -fx-font-weight: bold; -fx-min-width: 55px; -fx-min-height: 55px; " +
                            "-fx-background-color: linear-gradient(to bottom, #EF4444, #DC2626); -fx-text-fill: white; "
                            +
                            "-fx-background-radius: 12; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.4), 8, 0, 0, 2);"));
            minusBtn.setOnAction(e -> {
                int current = Integer.parseInt(absentCountLabel.getText());
                if (current < 10)
                    absentCountLabel.setText(String.valueOf(current + 1));
            });

            controlsBox.getChildren().addAll(plusBtn, minusBtn);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            subjectRow.getChildren().addAll(subjectName, spacer, countBox, controlsBox);

            markItems.add(new AttendanceMarkItem(subject.getId(), presentCountLabel, absentCountLabel));
            content.getChildren().add(subjectRow);
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(500);
        scrollPane.setMaxHeight(600);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().setPrefSize(700, 550);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == markButtonType) {
                int totalPresent = 0;
                int totalAbsent = 0;

                for (AttendanceMarkItem item : markItems) {
                    int presentClasses = Integer.parseInt(item.presentCount.getText());
                    int absentClasses = Integer.parseInt(item.absentCount.getText());

                    totalPresent += presentClasses;
                    totalAbsent += absentClasses;

                    // Update subject counts directly
                    Subject subject = attendanceService.getSubjectById(item.subjectId);
                    if (subject != null) {
                        int newTotal = subject.getTotalClasses() + presentClasses + absentClasses;
                        int newAttended = subject.getAttendedClasses() + presentClasses;

                        subject.setTotalClasses(newTotal);
                        subject.setAttendedClasses(newAttended);
                        attendanceService.updateSubject(subject);
                    }
                }

                loadDashboard();

                if (totalPresent + totalAbsent > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            String.format("Attendance updated!\n✅ Present: %d classes\n❌ Absent: %d classes",
                                    totalPresent, totalAbsent));
                } else {
                    showAlert(Alert.AlertType.INFORMATION, "No Changes", "No attendance was marked.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    // Helper class for attendance marking
    private static class AttendanceMarkItem {
        int subjectId;
        Label presentCount;
        Label absentCount;

        AttendanceMarkItem(int subjectId, Label presentCount, Label absentCount) {
            this.subjectId = subjectId;
            this.presentCount = presentCount;
            this.absentCount = absentCount;
        }
    }

    @FXML
    private void handleToggleTheme() {
        isDarkTheme = !isDarkTheme;
        themeToggle.setText(isDarkTheme ? "☀️" : "🌙");

        // Toggle theme stylesheet
        Scene scene = themeToggle.getScene();
        if (scene != null) {
            try {
                String darkThemePath = getClass().getResource("/com/attendancemanager/css/dark-theme.css")
                        .toExternalForm();
                System.out.println("Dark theme path: " + darkThemePath);
                System.out.println("Current stylesheets: " + scene.getStylesheets());

                if (isDarkTheme) {
                    // Add dark theme if not already present
                    if (!scene.getStylesheets().contains(darkThemePath)) {
                        scene.getStylesheets().add(darkThemePath);
                        System.out.println("Dark theme added");
                    }
                } else {
                    // Remove dark theme
                    scene.getStylesheets().remove(darkThemePath);
                    System.out.println("Dark theme removed");
                }

                System.out.println("Updated stylesheets: " + scene.getStylesheets());
            } catch (Exception e) {
                System.err.println("Error toggling theme: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleAnalytics() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/attendancemanager/view/analytics.fxml"));
            javafx.scene.Parent root = loader.load();

            javafx.stage.Stage analyticsStage = new javafx.stage.Stage();
            analyticsStage.setTitle("📊 Analytics Dashboard - Punch IT");
            analyticsStage.setScene(new javafx.scene.Scene(root, 1400, 900));
            analyticsStage.setMinWidth(1200);
            analyticsStage.setMinHeight(800);

            // Set custom app icon
            try {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/com/attendancemanager/icons/app-icon.png"));
                analyticsStage.getIcons().add(icon);
            } catch (Exception iconError) {
                System.err.println("Could not load app icon: " + iconError.getMessage());
            }

            analyticsStage.show();
        } catch (Exception e) {
            System.err.println("Error opening analytics: " + e.getMessage());
            e.printStackTrace();
            showAlert(javafx.scene.control.Alert.AlertType.ERROR, "Error", "Failed to open Analytics Dashboard.");
        }
    }

    @FXML
    private void handleExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Attendance Data");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("attendance_export.csv");

        File file = fileChooser.showSaveDialog(themeToggle.getScene().getWindow());
        if (file != null) {
            exportToCSV(file);
        }
    }

    @FXML
    private void handleImportData() {
        showAlert(Alert.AlertType.INFORMATION, "Coming Soon", "Data import will be available soon!");
    }

    /**
     * Export data to CSV file.
     */
    private void exportToCSV(File file) {
        try (PrintWriter writer = new PrintWriter(file)) {
            writer.println("Subject,Total Classes,Attended Classes,Current %,Target %,Status");

            List<Subject> subjects = attendanceService.getAllSubjects();
            for (Subject subject : subjects) {
                AttendanceStatusLevel status = attendanceService.getAttendanceStatusLevel(subject);
                writer.printf("%s,%d,%d,%.2f,%.2f,%s%n",
                        subject.getName(),
                        subject.getTotalClasses(),
                        subject.getAttendedClasses(),
                        subject.getCurrentPercentage(),
                        subject.getTargetPercentage(),
                        status.name());
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Data exported successfully!");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Export Failed", "Failed to export data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSchedule() {
        showScheduleDialog();
    }

    @FXML
    private void handleTimetable() {
        showTimetableEditor();
    }

    /**
     * Show today's schedule dialog - displays classes for current day.
     */
    private void showScheduleDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📅 Today's Schedule");
        dialog.setHeaderText("🎓 Classes for " + java.time.LocalDate.now().getDayOfWeek());

        // Style dialog
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #fffbf8); " +
                "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 15, 0, 0, 5);");

        // Get today's timetable entries
        com.attendancemanager.dao.TimetableDAO timetableDAO = new com.attendancemanager.dao.TimetableDAO();
        List<com.attendancemanager.model.TimetableEntry> todayClasses = timetableDAO
                .getByDay(java.time.LocalDate.now().getDayOfWeek());

        // Create content
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(500);

        if (todayClasses.isEmpty()) {
            Label emptyLabel = new Label("No classes scheduled for today! 🎉");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #64748b;");
            content.getChildren().add(emptyLabel);
        } else {
            for (com.attendancemanager.model.TimetableEntry entry : todayClasses) {
                VBox classCard = new VBox(8);
                classCard.setStyle("-fx-background-color: linear-gradient(to right, #ffffff, #fffbf8); " +
                        "-fx-padding: 18; -fx-border-color: #fed7aa; " +
                        "-fx-border-width: 2; -fx-border-radius: 12; -fx-background-radius: 12; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.15), 8, 0, 0, 2);");

                Label subjectLabel = new Label(entry.getSubjectName());
                subjectLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

                HBox timeBox = new HBox(10);
                timeBox.setAlignment(Pos.CENTER_LEFT);
                Label timeLabel = new Label("⏰ " + entry.getStartTime() + " - " + entry.getEndTime());
                timeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
                timeBox.getChildren().add(timeLabel);

                if (entry.getRoom() != null && !entry.getRoom().isEmpty()) {
                    Label roomLabel = new Label("📍 Room: " + entry.getRoom());
                    roomLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #475569;");
                    timeBox.getChildren().add(roomLabel);
                }

                classCard.getChildren().addAll(subjectLabel, timeBox);
                content.getChildren().add(classCard);
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(400);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    /**
     * Show timetable editor - full weekly schedule management.
     */
    private void showTimetableEditor() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("📚 Timetable Editor");
        dialog.setHeaderText("🗓️ Manage Weekly Schedule");

        // Style dialog
        dialog.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, #fffbf8); " +
                "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.2), 15, 0, 0, 5);");

        com.attendancemanager.dao.TimetableDAO timetableDAO = new com.attendancemanager.dao.TimetableDAO();

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(700);

        // Add new entry form
        VBox addForm = new VBox(12);
        addForm.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #fef5f1, #fff4ed); " +
                        "-fx-padding: 20; -fx-border-radius: 12; -fx-background-radius: 12; " +
                        "-fx-border-color: #fed7aa; -fx-border-width: 2; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.15), 8, 0, 0, 2);");

        Label formTitle = new Label("✨ Add New Class");
        formTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f97316;");

        // Subject dropdown
        ComboBox<Subject> subjectCombo = new ComboBox<>();
        subjectCombo.setPromptText("📚 Select Subject");
        subjectCombo.getItems().addAll(attendanceService.getAllSubjects());
        subjectCombo.setConverter(new javafx.util.StringConverter<Subject>() {
            @Override
            public String toString(Subject subject) {
                return subject != null ? subject.getName() : "";
            }

            @Override
            public Subject fromString(String string) {
                return null;
            }
        });
        subjectCombo.setPrefWidth(220);
        subjectCombo.setStyle("-fx-background-radius: 8; -fx-border-color: #fed7aa; -fx-border-radius: 8; " +
                "-fx-font-size: 14px; -fx-padding: 8;");

        // Day dropdown
        ComboBox<java.time.DayOfWeek> dayCombo = new ComboBox<>();
        dayCombo.setPromptText("📅 Select Day");
        dayCombo.getItems().addAll(java.time.DayOfWeek.values());
        dayCombo.setPrefWidth(170);
        dayCombo.setStyle("-fx-background-radius: 8; -fx-border-color: #fed7aa; -fx-border-radius: 8; " +
                "-fx-font-size: 14px; -fx-padding: 8;");

        // Time pickers using Spinners
        Label startLabel = new Label("Start:");
        Spinner<Integer> startHourSpinner = new Spinner<>(0, 23, 9);
        startHourSpinner.setPrefWidth(65);
        startHourSpinner.setEditable(true);
        Spinner<Integer> startMinuteSpinner = new Spinner<>(0, 59, 0);
        startMinuteSpinner.setPrefWidth(65);
        startMinuteSpinner.setEditable(true);
        HBox startTimePicker = new HBox(5, startLabel, startHourSpinner, new Label(":"), startMinuteSpinner);
        startTimePicker.setAlignment(Pos.CENTER_LEFT);

        Label endLabel = new Label("End:");
        Spinner<Integer> endHourSpinner = new Spinner<>(0, 23, 10);
        endHourSpinner.setPrefWidth(65);
        endHourSpinner.setEditable(true);
        Spinner<Integer> endMinuteSpinner = new Spinner<>(0, 59, 0);
        endMinuteSpinner.setPrefWidth(65);
        endMinuteSpinner.setEditable(true);
        HBox endTimePicker = new HBox(5, endLabel, endHourSpinner, new Label(":"), endMinuteSpinner);
        endTimePicker.setAlignment(Pos.CENTER_LEFT);

        TextField roomField = new TextField();
        roomField.setPromptText("Room (optional)");
        roomField.setPrefWidth(120);

        roomField.setStyle("-fx-background-radius: 8; -fx-border-color: #fed7aa; -fx-border-radius: 8; " +
                "-fx-padding: 8; -fx-font-size: 14px;");

        Button addButton = new Button("➕ Add Class");
        addButton.setStyle("-fx-background-color: linear-gradient(to right, #fb923c, #f97316); " +
                "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; " +
                "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.4), 8, 0, 0, 2);");
        addButton.setOnMouseEntered(e -> addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #f97316, #ea580c); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.5), 10, 0, 0, 3); -fx-scale-x: 1.03; -fx-scale-y: 1.03;"));
        addButton.setOnMouseExited(e -> addButton.setStyle(
                "-fx-background-color: linear-gradient(to right, #fb923c, #f97316); " +
                        "-fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; " +
                        "-fx-background-radius: 8; -fx-cursor: hand; -fx-font-size: 14px; " +
                        "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.4), 8, 0, 0, 2);"));

        HBox formRow1 = new HBox(12, subjectCombo, dayCombo);
        HBox formRow2 = new HBox(12, startTimePicker, endTimePicker, roomField, addButton);

        addForm.getChildren().addAll(formTitle, formRow1, formRow2);

        // Existing entries list
        VBox entriesList = new VBox(10);

        // Refresh entries function (using array to allow modification in lambda)
        final Runnable[] refreshEntriesArray = new Runnable[1];
        refreshEntriesArray[0] = () -> {
            entriesList.getChildren().clear();
            List<com.attendancemanager.model.TimetableEntry> entries = timetableDAO.getAll();

            if (entries.isEmpty()) {
                Label emptyLabel = new Label("No classes in timetable. Add your first class above!");
                emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
                entriesList.getChildren().add(emptyLabel);
            } else {
                // Group by day and display with day headers
                java.time.DayOfWeek currentDay = null;
                for (com.attendancemanager.model.TimetableEntry entry : entries) {
                    // Add day header if it's a new day
                    if (currentDay == null || !currentDay.equals(entry.getDayOfWeek())) {
                        currentDay = entry.getDayOfWeek();
                        Label dayHeader = new Label("📅 " + currentDay.toString());
                        dayHeader.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; " +
                                "-fx-padding: 10 0 5 0;");
                        entriesList.getChildren().add(dayHeader);
                    }

                    HBox entryRow = new HBox(15);
                    entryRow.setStyle("-fx-background-color: linear-gradient(to right, #ffffff, #fffbf8); " +
                            "-fx-padding: 14; -fx-border-color: #fed7aa; " +
                            "-fx-border-width: 2; -fx-border-radius: 10; -fx-background-radius: 10; " +
                            "-fx-effect: dropshadow(gaussian, rgba(251,146,60,0.12), 6, 0, 0, 2);");
                    entryRow.setAlignment(Pos.CENTER_LEFT);

                    VBox infoBox = new VBox(5);
                    Label subjectLabel = new Label(entry.getSubjectName());
                    subjectLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

                    String timeInfo = String.format("⏰ %s - %s",
                            entry.getStartTime(), entry.getEndTime());
                    if (entry.getRoom() != null && !entry.getRoom().isEmpty()) {
                        timeInfo += " | 📍 Room: " + entry.getRoom();
                    }
                    Label timeLabel = new Label(timeInfo);
                    timeLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 500;");

                    infoBox.getChildren().addAll(subjectLabel, timeLabel);

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button deleteButton = new Button("🗑️ Delete");
                    deleteButton.setStyle("-fx-background-color: linear-gradient(to right, #EF4444, #DC2626); " +
                            "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 600; " +
                            "-fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; " +
                            "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 6, 0, 0, 2);");
                    deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(
                            "-fx-background-color: linear-gradient(to right, #DC2626, #B91C1C); " +
                                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 600; " +
                                    "-fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.4), 8, 0, 0, 3);"));
                    deleteButton.setOnMouseExited(e -> deleteButton.setStyle(
                            "-fx-background-color: linear-gradient(to right, #EF4444, #DC2626); " +
                                    "-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: 600; " +
                                    "-fx-padding: 8 16; -fx-background-radius: 8; -fx-cursor: hand; " +
                                    "-fx-effect: dropshadow(gaussian, rgba(239,68,68,0.3), 6, 0, 0, 2);"));
                    deleteButton.setOnAction(e -> {
                        if (timetableDAO.delete(entry.getId())) {
                            refreshEntriesArray[0].run();
                        }
                    });

                    entryRow.getChildren().addAll(infoBox, spacer, deleteButton);
                    entriesList.getChildren().add(entryRow);
                }
            }
        };

        // Add button action
        addButton.setOnAction(e -> {
            Subject selectedSubject = subjectCombo.getValue();
            java.time.DayOfWeek selectedDay = dayCombo.getValue();
            String room = roomField.getText();

            if (selectedSubject == null || selectedDay == null) {
                showAlert(Alert.AlertType.WARNING, "Missing Information",
                        "Please fill in subject and day.");
                return;
            }

            try {
                java.time.LocalTime start = java.time.LocalTime.of(
                        startHourSpinner.getValue(),
                        startMinuteSpinner.getValue());
                java.time.LocalTime end = java.time.LocalTime.of(
                        endHourSpinner.getValue(),
                        endMinuteSpinner.getValue());

                com.attendancemanager.model.TimetableEntry newEntry = new com.attendancemanager.model.TimetableEntry();
                newEntry.setSubjectId(selectedSubject.getId());
                newEntry.setDayOfWeek(selectedDay);
                newEntry.setStartTime(start);
                newEntry.setEndTime(end);
                newEntry.setRoom(room);

                if (end.isBefore(start) || end.equals(start)) {
                    showAlert(Alert.AlertType.ERROR, "Invalid Time",
                            "End time must be after start time!");
                    return;
                }

                if (timetableDAO.create(newEntry)) {
                    // Clear form (keep day selected for better UX)
                    subjectCombo.setValue(null);
                    // dayCombo keeps its value for consecutive entries on same day
                    startHourSpinner.getValueFactory().setValue(9);
                    startMinuteSpinner.getValueFactory().setValue(0);
                    endHourSpinner.getValueFactory().setValue(10);
                    endMinuteSpinner.getValueFactory().setValue(0);
                    roomField.clear();

                    refreshEntriesArray[0].run();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Class added to timetable!");
                }
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Error",
                        "Failed to add class: " + ex.getMessage());
            }
        });

        // Initial load
        refreshEntriesArray[0].run();
        ScrollPane scrollPane = new ScrollPane(entriesList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        content.getChildren().addAll(addForm, new Label("Current Schedule:"), scrollPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefSize(750, 600);
        dialog.showAndWait();
    }

    /**
     * Update profile name display in header.
     */
    private void updateProfileDisplay() {
        UserContext userContext = UserContext.getInstance();
        if (userContext.hasProfile() && profileNameText != null) {
            profileNameText.setText(userContext.getCurrentProfileName());
        }
    }

    /**
     * Handle switch profile - return to profile selection screen.
     */
    @FXML
    private void handleSwitchProfile() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Switch Profile");
        confirmation.setHeaderText("Switch to a different profile?");
        confirmation.setContentText("Your current data is saved. You can switch back anytime.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Load profile selection screen
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/com/attendancemanager/view/profile-selection.fxml"));
                javafx.scene.Parent root = loader.load();

                // Get current stage
                javafx.stage.Stage stage = (javafx.stage.Stage) themeToggle.getScene().getWindow();
                stage.setScene(new javafx.scene.Scene(root, 800, 600));
                stage.setTitle("Punch IT - Select Your Profile");
                stage.setMinWidth(700);
                stage.setMinHeight(500);
                stage.centerOnScreen();
            } catch (Exception e) {
                System.err.println("Error switching profiles: " + e.getMessage());
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Error", "Failed to switch profiles.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

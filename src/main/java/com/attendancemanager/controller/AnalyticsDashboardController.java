package com.attendancemanager.controller;

import com.attendancemanager.component.AttendanceHeatmap;
import com.attendancemanager.model.AttendanceRecord.AttendanceStatus;
import com.attendancemanager.model.Subject;
import com.attendancemanager.service.AnalyticsService;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * Controller for the Analytics Dashboard view.
 */
public class AnalyticsDashboardController {

    @FXML
    private ComboBox<String> subjectFilter;
    @FXML
    private ComboBox<Integer> yearFilter;
    @FXML
    private LineChart<String, Number> monthlyTrendsChart;
    @FXML
    private BarChart<String, Number> subjectComparisonChart;
    @FXML
    private PieChart bestPerformersChart;
    @FXML
    private PieChart worstPerformersChart;
    @FXML
    private StackPane heatmapContainer;
    @FXML
    private Label trendsSubtitle;
    @FXML
    private Label comparisonSubtitle;
    @FXML
    private Label heatmapSubtitle;

    private AnalyticsService analyticsService;
    private AttendanceHeatmap heatmap;
    private List<Subject> allSubjects;

    @FXML
    public void initialize() {
        analyticsService = new AnalyticsService();
        heatmap = new AttendanceHeatmap();
        heatmapContainer.getChildren().add(heatmap);

        setupFilters();
        loadAllData();
    }

    /**
     * Setup year and subject filter dropdowns.
     */
    private void setupFilters() {
        // Populate year filter (last 5 years)
        int currentYear = Year.now().getValue();
        for (int i = 0; i < 5; i++) {
            yearFilter.getItems().add(currentYear - i);
        }
        yearFilter.setValue(currentYear);

        // Populate subject filter
        allSubjects = analyticsService.getSubjectComparison();
        subjectFilter.getItems().add("All Subjects");
        for (Subject subject : allSubjects) {
            subjectFilter.getItems().add(subject.getName());
        }
        subjectFilter.setValue("All Subjects");

        // Add listeners for filter changes
        yearFilter.setOnAction(e -> loadAllData());
        subjectFilter.setOnAction(e -> loadAllData());
    }

    /**
     * Load all chart data based on current filters.
     */
    private void loadAllData() {
        int selectedYear = yearFilter.getValue();
        String selectedSubject = subjectFilter.getValue();

        // Update subtitles
        trendsSubtitle.setText(selectedYear + " - " + selectedSubject);
        comparisonSubtitle.setText("All Subjects - " + selectedYear);
        heatmapSubtitle.setText(selectedYear + " - " + selectedSubject);

        loadMonthlyTrends(selectedSubject, selectedYear);
        loadSubjectComparison();
        loadHeatmap(selectedSubject, selectedYear);
        loadPerformanceSummary();
    }

    /**
     * Load monthly attendance trends line chart.
     */
    private void loadMonthlyTrends(String subjectName, int year) {
        monthlyTrendsChart.getData().clear();

        if ("All Subjects".equals(subjectName)) {
            // Show trends for all subjects
            for (Subject subject : allSubjects) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(subject.getName());

                Map<String, Double> monthlyData = analyticsService.getMonthlyAttendancePercentages(subject.getId(),
                        year);

                for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }

                monthlyTrendsChart.getData().add(series);
            }
        } else {
            // Show trend for selected subject
            Subject subject = allSubjects.stream()
                    .filter(s -> s.getName().equals(subjectName))
                    .findFirst()
                    .orElse(null);

            if (subject != null) {
                XYChart.Series<String, Number> series = new XYChart.Series<>();
                series.setName(subject.getName());

                Map<String, Double> monthlyData = analyticsService.getMonthlyAttendancePercentages(subject.getId(),
                        year);

                for (Map.Entry<String, Double> entry : monthlyData.entrySet()) {
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                }

                monthlyTrendsChart.getData().add(series);
            }
        }

        // Style the chart
        styleChart(monthlyTrendsChart);
    }

    /**
     * Load subject comparison bar chart.
     */
    private void loadSubjectComparison() {
        subjectComparisonChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Attendance %");

        List<Subject> subjects = analyticsService.getSubjectComparison();

        for (Subject subject : subjects) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(
                    subject.getName(),
                    subject.getCurrentPercentage());

            series.getData().add(data);

            // Color code bars based on percentage
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    double percentage = subject.getCurrentPercentage();
                    String color;
                    if (percentage >= 85) {
                        color = "#10B981"; // Green
                    } else if (percentage >= 75) {
                        color = "#fb923c"; // Orange
                    } else {
                        color = "#EF4444"; // Red
                    }
                    newNode.setStyle("-fx-bar-fill: " + color + ";");
                }
            });
        }

        subjectComparisonChart.getData().add(series);
        styleChart(subjectComparisonChart);
    }

    /**
     * Load attendance heatmap calendar.
     */
    private void loadHeatmap(String subjectName, int year) {
        if ("All Subjects".equals(subjectName)) {
            // Show first subject's heatmap
            if (!allSubjects.isEmpty()) {
                Map<LocalDate, AttendanceStatus> heatmapData = analyticsService
                        .getYearHeatmapData(allSubjects.get(0).getId(), year);
                heatmap.populateYear(year, heatmapData);
            }
        } else {
            Subject subject = allSubjects.stream()
                    .filter(s -> s.getName().equals(subjectName))
                    .findFirst()
                    .orElse(null);

            if (subject != null) {
                Map<LocalDate, AttendanceStatus> heatmapData = analyticsService.getYearHeatmapData(subject.getId(),
                        year);
                heatmap.populateYear(year, heatmapData);
            }
        }
    }

    /**
     * Load best and worst performing subjects pie charts.
     */
    private void loadPerformanceSummary() {
        // Best performers
        bestPerformersChart.getData().clear();
        List<Subject> bestSubjects = analyticsService.getBestPerformingSubjects(5);

        for (Subject subject : bestSubjects) {
            PieChart.Data slice = new PieChart.Data(
                    subject.getName() + " (" + String.format("%.1f%%", subject.getCurrentPercentage()) + ")",
                    subject.getCurrentPercentage());
            bestPerformersChart.getData().add(slice);
        }

        // Worst performers
        worstPerformersChart.getData().clear();
        List<Subject> worstSubjects = analyticsService.getWorstPerformingSubjects(5);

        for (Subject subject : worstSubjects) {
            PieChart.Data slice = new PieChart.Data(
                    subject.getName() + " (" + String.format("%.1f%%", subject.getCurrentPercentage()) + ")",
                    subject.getCurrentPercentage());
            worstPerformersChart.getData().add(slice);
        }

        // Apply colors to pie slices
        applyPieChartColors(bestPerformersChart, true);
        applyPieChartColors(worstPerformersChart, false);
    }

    /**
     * Apply color scheme to pie charts.
     */
    private void applyPieChartColors(PieChart chart, boolean isBest) {
        String[] colors = isBest
                ? new String[] { "#10B981", "#34D399", "#6EE7B7", "#A7F3D0", "#D1FAE5" } // Green shades
                : new String[] { "#EF4444", "#F87171", "#FCA5A5", "#FECACA", "#FEE2E2" }; // Red shades

        int index = 0;
        for (PieChart.Data data : chart.getData()) {
            data.getNode().setStyle("-fx-pie-color: " + colors[index % colors.length] + ";");
            index++;
        }
    }

    /**
     * Apply consistent styling to charts.
     */
    private void styleChart(Chart chart) {
        chart.setStyle("-fx-background-color: transparent;");
    }

    @FXML
    private void handleRefresh() {
        loadAllData();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) yearFilter.getScene().getWindow();
        stage.close();
    }
}

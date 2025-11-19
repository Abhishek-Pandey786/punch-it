package com.attendancemanager.component;

import com.attendancemanager.model.AttendanceRecord.AttendanceStatus;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

/**
 * Custom GitHub-style contribution heatmap calendar for attendance
 * visualization.
 */
public class AttendanceHeatmap extends GridPane {

    private static final int CELL_SIZE = 15;
    private static final int CELL_SPACING = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public AttendanceHeatmap() {
        setHgap(CELL_SPACING);
        setVgap(CELL_SPACING);
        setAlignment(Pos.CENTER);
        getStyleClass().add("attendance-heatmap");
    }

    /**
     * Populate heatmap with attendance data for a year.
     * 
     * @param year           The year to display
     * @param attendanceData Map of dates to attendance status
     */
    public void populateYear(int year, Map<LocalDate, AttendanceStatus> attendanceData) {
        getChildren().clear();

        // Add day of week labels (left column)
        String[] dayLabels = { "Mon", "Wed", "Fri" }; // Abbreviated for space
        int[] dayRows = { 0, 2, 4 }; // Skip rows for better readability

        for (int i = 0; i < dayLabels.length; i++) {
            Label dayLabel = new Label(dayLabels[i]);
            dayLabel.getStyleClass().add("heatmap-day-label");
            dayLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #64748b; -fx-min-width: 30px;");
            add(dayLabel, 0, dayRows[i]);
        }

        // Start from first day of year
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        // Find the first Monday
        LocalDate current = startDate;
        while (current.getDayOfWeek().getValue() != 1) { // 1 = Monday
            current = current.minusDays(1);
        }

        int weekColumn = 1; // Start after day labels
        int currentMonth = -1;

        while (!current.isAfter(endDate) || current.getDayOfWeek().getValue() != 1) {
            // Add month labels at the start of each month
            if (current.getMonthValue() != currentMonth && current.getDayOfMonth() <= 7) {
                currentMonth = current.getMonthValue();
                Label monthLabel = new Label(
                        current.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH));
                monthLabel.getStyleClass().add("heatmap-month-label");
                monthLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #1e293b; -fx-font-weight: 600;");
                add(monthLabel, weekColumn, 7); // Row 7 for month labels at bottom
            }

            // Add cells for the week
            for (int dayOfWeek = 0; dayOfWeek < 7; dayOfWeek++) {
                StackPane cell = createCell(current, attendanceData);
                add(cell, weekColumn, dayOfWeek);
                current = current.plusDays(1);

                if (current.isAfter(endDate)) {
                    break;
                }
            }

            weekColumn++;

            if (current.isAfter(endDate)) {
                break;
            }
        }
    }

    /**
     * Create a single heatmap cell for a date.
     */
    private StackPane createCell(LocalDate date, Map<LocalDate, AttendanceStatus> attendanceData) {
        StackPane cell = new StackPane();
        cell.setMinSize(CELL_SIZE, CELL_SIZE);
        cell.setPrefSize(CELL_SIZE, CELL_SIZE);
        cell.setMaxSize(CELL_SIZE, CELL_SIZE);

        cell.getStyleClass().add("heatmap-cell");

        // Determine cell color based on attendance status
        AttendanceStatus status = attendanceData.get(date);
        final String cellStyle;

        if (status == null) {
            cellStyle = "-fx-background-color: #f1f5f9;"; // No data - light gray
            cell.getStyleClass().add("heatmap-cell-none");
        } else {
            switch (status) {
                case PRESENT:
                    cellStyle = "-fx-background-color: #10B981;"; // Green
                    cell.getStyleClass().add("heatmap-cell-present");
                    break;
                case ABSENT:
                    cellStyle = "-fx-background-color: #EF4444;"; // Red
                    cell.getStyleClass().add("heatmap-cell-absent");
                    break;
                case CANCELLED:
                    cellStyle = "-fx-background-color: #94a3b8;"; // Gray
                    cell.getStyleClass().add("heatmap-cell-cancelled");
                    break;
                default:
                    cellStyle = "-fx-background-color: #f1f5f9;";
            }
        }

        cell.setStyle(cellStyle + " -fx-border-color: #e2e8f0; -fx-border-width: 1; "
                + "-fx-border-radius: 2; -fx-background-radius: 2;");

        // Add tooltip with date and status
        String statusText = status == null ? "No class" : formatStatus(status);
        Tooltip tooltip = new Tooltip(date.format(DATE_FORMATTER) + "\n" + statusText);
        tooltip.setShowDelay(Duration.millis(300));
        tooltip.setStyle("-fx-font-size: 12px; -fx-background-color: #2d2d30; -fx-text-fill: white; "
                + "-fx-background-radius: 6; -fx-padding: 6 10;");
        Tooltip.install(cell, tooltip);

        // Hover effect
        cell.setOnMouseEntered(e -> {
            if (status != null) {
                cell.setStyle(cellStyle + " -fx-border-color: #fb923c; -fx-border-width: 2; "
                        + "-fx-border-radius: 2; -fx-background-radius: 2; -fx-scale-x: 1.2; -fx-scale-y: 1.2;");
            }
        });

        cell.setOnMouseExited(e -> {
            cell.setStyle(cellStyle + " -fx-border-color: #e2e8f0; -fx-border-width: 1; "
                    + "-fx-border-radius: 2; -fx-background-radius: 2;");
        });

        return cell;
    }

    /**
     * Format attendance status for tooltip display.
     */
    private String formatStatus(AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> "✅ Present";
            case ABSENT -> "❌ Absent";
            case CANCELLED -> "⚠️ Cancelled";
        };
    }
}

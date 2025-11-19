package com.attendancemanager.service;

import com.attendancemanager.dao.AttendanceRecordDAO;
import com.attendancemanager.dao.SubjectDAO;
import com.attendancemanager.model.AttendanceRecord;
import com.attendancemanager.model.AttendanceRecord.AttendanceStatus;
import com.attendancemanager.model.Subject;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for analytics and statistical data processing.
 */
public class AnalyticsService {

    private final AttendanceRecordDAO attendanceRecordDAO;
    private final SubjectDAO subjectDAO;

    public AnalyticsService() {
        this.attendanceRecordDAO = new AttendanceRecordDAO();
        this.subjectDAO = new SubjectDAO();
    }

    /**
     * Get monthly attendance percentages for a subject throughout a year.
     * Returns map of month names to attendance percentages.
     */
    public Map<String, Double> getMonthlyAttendancePercentages(int subjectId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<AttendanceRecord> records = attendanceRecordDAO.getRecordsInDateRange(subjectId, startDate, endDate);

        // Group by month
        Map<YearMonth, List<AttendanceRecord>> monthlyGroups = records.stream()
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getDate())));

        // Calculate percentage for each month and convert to display format
        Map<String, Double> monthlyPercentages = new LinkedHashMap<>();

        for (int month = 1; month <= 12; month++) {
            YearMonth yearMonth = YearMonth.of(year, month);
            String monthName = yearMonth.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

            List<AttendanceRecord> monthRecords = monthlyGroups.getOrDefault(yearMonth, Collections.emptyList());

            if (monthRecords.isEmpty()) {
                monthlyPercentages.put(monthName, 0.0);
            } else {
                long total = monthRecords.size();
                long present = monthRecords.stream()
                        .filter(r -> r.getStatus() == AttendanceStatus.PRESENT)
                        .count();
                double percentage = (present * 100.0) / total;
                monthlyPercentages.put(monthName, percentage);
            }
        }

        return monthlyPercentages;
    }

    /**
     * Get comparison data for all subjects.
     * Returns list of subjects sorted by current attendance percentage.
     */
    public List<Subject> getSubjectComparison() {
        List<Subject> subjects = subjectDAO.getAll();

        // Sort by attendance percentage (descending)
        subjects.sort(Comparator.comparingDouble(Subject::getCurrentPercentage).reversed());

        return subjects;
    }

    /**
     * Get heatmap data for a subject for an entire year.
     * Returns map of dates to attendance status for visualization.
     */
    public Map<LocalDate, AttendanceStatus> getYearHeatmapData(int subjectId, int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<AttendanceRecord> records = attendanceRecordDAO.getRecordsInDateRange(subjectId, startDate, endDate);

        return records.stream()
                .collect(Collectors.toMap(
                        AttendanceRecord::getDate,
                        AttendanceRecord::getStatus,
                        (existing, replacement) -> existing, // Keep first if duplicate
                        LinkedHashMap::new));
    }

    /**
     * Get heatmap data for all subjects within date range.
     * Returns map of dates to list of attendance records.
     */
    public Map<LocalDate, List<AttendanceRecord>> getAllSubjectsHeatmapData(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = attendanceRecordDAO.getAllRecordsInDateRange(startDate, endDate);

        return records.stream()
                .collect(Collectors.groupingBy(
                        AttendanceRecord::getDate,
                        LinkedHashMap::new,
                        Collectors.toList()));
    }

    /**
     * Get top performing subjects by attendance percentage.
     */
    public List<Subject> getBestPerformingSubjects(int limit) {
        List<Subject> subjects = subjectDAO.getAll();

        return subjects.stream()
                .sorted(Comparator.comparingDouble(Subject::getCurrentPercentage).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get worst performing subjects by attendance percentage.
     */
    public List<Subject> getWorstPerformingSubjects(int limit) {
        List<Subject> subjects = subjectDAO.getAll();

        return subjects.stream()
                .sorted(Comparator.comparingDouble(Subject::getCurrentPercentage))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Get overall statistics for dashboard summary.
     */
    public Map<String, Object> getOverallStatistics() {
        Map<String, Object> stats = new HashMap<>();
        List<Subject> subjects = subjectDAO.getAll();

        if (subjects.isEmpty()) {
            stats.put("totalSubjects", 0);
            stats.put("totalClasses", 0);
            stats.put("attendedClasses", 0);
            stats.put("overallPercentage", 0.0);
            stats.put("safeSubjects", 0);
            stats.put("riskSubjects", 0);
            return stats;
        }

        int totalClasses = subjects.stream().mapToInt(Subject::getTotalClasses).sum();
        int attendedClasses = subjects.stream().mapToInt(Subject::getAttendedClasses).sum();
        double overallPercentage = totalClasses > 0 ? (attendedClasses * 100.0) / totalClasses : 0.0;

        long safeCount = subjects.stream()
                .filter(s -> s.getCurrentPercentage() >= s.getTargetPercentage())
                .count();
        long riskCount = subjects.stream()
                .filter(s -> s.getCurrentPercentage() < s.getTargetPercentage())
                .count();

        stats.put("totalSubjects", subjects.size());
        stats.put("totalClasses", totalClasses);
        stats.put("attendedClasses", attendedClasses);
        stats.put("overallPercentage", overallPercentage);
        stats.put("safeSubjects", (int) safeCount);
        stats.put("riskSubjects", (int) riskCount);

        return stats;
    }

    /**
     * Data class for subject performance comparison.
     */
    public static class SubjectPerformanceData {
        private String subjectName;
        private double attendancePercentage;
        private int totalClasses;
        private int attendedClasses;

        public SubjectPerformanceData(String subjectName, double attendancePercentage, int totalClasses,
                int attendedClasses) {
            this.subjectName = subjectName;
            this.attendancePercentage = attendancePercentage;
            this.totalClasses = totalClasses;
            this.attendedClasses = attendedClasses;
        }

        public String getSubjectName() {
            return subjectName;
        }

        public double getAttendancePercentage() {
            return attendancePercentage;
        }

        public int getTotalClasses() {
            return totalClasses;
        }

        public int getAttendedClasses() {
            return attendedClasses;
        }
    }
}

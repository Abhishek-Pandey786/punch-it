package com.attendancemanager.service;

import com.attendancemanager.dao.AttendanceRecordDAO;
import com.attendancemanager.dao.SubjectDAO;
import com.attendancemanager.model.AttendanceRecord;
import com.attendancemanager.model.AttendanceRecord.AttendanceStatus;
import com.attendancemanager.model.Subject;

import java.time.LocalDate;
import java.util.List;

/**
 * Service layer for attendance management with intelligent prediction
 * algorithms.
 * This is the core business logic layer - the standout feature of the app.
 */
public class AttendanceService {
    private final SubjectDAO subjectDAO;
    private final AttendanceRecordDAO attendanceRecordDAO;

    public AttendanceService() {
        this.subjectDAO = new SubjectDAO();
        this.attendanceRecordDAO = new AttendanceRecordDAO();
    }

    /**
     * Mark attendance for a subject on a specific date.
     * Automatically updates total and attended class counts.
     */
    public boolean markAttendance(int subjectId, LocalDate date, AttendanceStatus status, String notes) {
        Subject subject = subjectDAO.getById(subjectId);
        if (subject == null) {
            return false;
        }

        // Check if record already exists
        AttendanceRecord existing = attendanceRecordDAO.getBySubjectAndDate(subjectId, date);

        if (existing != null) {
            // Update existing record
            existing.setStatus(status);
            existing.setNotes(notes);

            boolean updated = attendanceRecordDAO.update(existing);
            if (updated) {
                // Recalculate attendance counts
                updateSubjectAttendanceCounts(subjectId);
            }
            return updated;
        } else {
            // Create new record
            AttendanceRecord record = new AttendanceRecord(0, subjectId, date, status, notes);
            boolean created = attendanceRecordDAO.create(record);

            if (created) {
                // Update subject counts
                updateSubjectAttendanceCounts(subjectId);
            }
            return created;
        }
    }

    /**
     * Recalculate and update subject's attendance counts based on records.
     */
    private void updateSubjectAttendanceCounts(int subjectId) {
        int[] stats = attendanceRecordDAO.getAttendanceStats(subjectId);
        int totalClasses = stats[0] - stats[3]; // Total minus cancelled
        int attendedClasses = stats[1]; // Present count

        subjectDAO.updateAttendanceCounts(subjectId, totalClasses, attendedClasses);
    }

    /**
     * Calculate how many consecutive classes need to be attended to reach target.
     * This is a key intelligent feature.
     */
    public int calculateClassesNeededToReachTarget(Subject subject) {
        double currentPercentage = subject.getCurrentPercentage();
        double target = subject.getTargetPercentage();

        // Already at or above target
        if (currentPercentage >= target) {
            return 0;
        }

        int currentTotal = subject.getTotalClasses();
        int currentAttended = subject.getAttendedClasses();

        // If no classes yet, need to attend first class
        if (currentTotal == 0) {
            return 1;
        }

        // Calculate classes needed
        // Formula: (attended + x) / (total + x) >= target/100
        // Solving: attended + x >= (target/100) * (total + x)
        // attended + x >= target*total/100 + target*x/100
        // x - target*x/100 >= target*total/100 - attended
        // x(1 - target/100) >= target*total/100 - attended
        // x >= (target*total/100 - attended) / (1 - target/100)

        double numerator = (target * currentTotal / 100.0) - currentAttended;
        double denominator = 1 - (target / 100.0);

        if (denominator <= 0) {
            return Integer.MAX_VALUE; // Target is 100% or above
        }

        int classesNeeded = (int) Math.ceil(numerator / denominator);
        return Math.max(0, classesNeeded);
    }

    /**
     * Calculate how many classes can be safely bunked without falling below target.
     * This is another key intelligent feature.
     */
    public int calculateSafeBunks(Subject subject) {
        double currentPercentage = subject.getCurrentPercentage();
        double target = subject.getTargetPercentage();

        // Already below target
        if (currentPercentage < target) {
            return 0;
        }

        int currentTotal = subject.getTotalClasses();
        int currentAttended = subject.getAttendedClasses();

        // If no classes yet
        if (currentTotal == 0) {
            return 0;
        }

        // Calculate safe bunks
        // Formula: attended / (total + x) >= target/100
        // Solving: attended >= (target/100) * (total + x)
        // attended >= target*total/100 + target*x/100
        // attended - target*total/100 >= target*x/100
        // (attended - target*total/100) * 100/target >= x

        double maxAdditionalClasses = ((currentAttended * 100.0 / target) - currentTotal);
        int safeBunks = (int) Math.floor(maxAdditionalClasses);

        return Math.max(0, safeBunks);
    }

    /**
     * Predict attendance percentage after attending next N classes.
     */
    public double predictPercentageAfterAttending(Subject subject, int classesToAttend) {
        int newTotal = subject.getTotalClasses() + classesToAttend;
        int newAttended = subject.getAttendedClasses() + classesToAttend;

        if (newTotal == 0) {
            return 0.0;
        }

        return (newAttended * 100.0) / newTotal;
    }

    /**
     * Predict attendance percentage after bunking next N classes.
     */
    public double predictPercentageAfterBunking(Subject subject, int classesToBunk) {
        int newTotal = subject.getTotalClasses() + classesToBunk;
        int newAttended = subject.getAttendedClasses(); // No change in attended

        if (newTotal == 0) {
            return 0.0;
        }

        return (newAttended * 100.0) / newTotal;
    }

    /**
     * Get attendance status: SAFE, WARNING, or RISK based on current percentage.
     */
    public AttendanceStatusLevel getAttendanceStatusLevel(Subject subject) {
        double current = subject.getCurrentPercentage();
        double target = subject.getTargetPercentage();

        if (current >= target) {
            return AttendanceStatusLevel.SAFE;
        } else if (current >= target - 5) { // Within 5% of target
            return AttendanceStatusLevel.WARNING;
        } else {
            return AttendanceStatusLevel.RISK;
        }
    }

    /**
     * Generate a smart recommendation message for the student.
     */
    public String generateRecommendation(Subject subject) {
        int classesNeeded = calculateClassesNeededToReachTarget(subject);
        int safeBunks = calculateSafeBunks(subject);
        double current = subject.getCurrentPercentage();
        double target = subject.getTargetPercentage();

        if (current >= target) {
            if (safeBunks > 0) {
                return String.format("You're safe! You can miss %d more class%s and still maintain %.1f%%.",
                        safeBunks, safeBunks == 1 ? "" : "es", target);
            } else {
                return String.format("You're at target. Attend all classes to maintain %.1f%%.", target);
            }
        } else {
            if (classesNeeded > 0) {
                return String.format("Attend the next %d class%s to reach %.1f%%.",
                        classesNeeded, classesNeeded == 1 ? "" : "es", target);
            } else {
                return "Keep attending classes!";
            }
        }
    }

    /**
     * Get all subjects.
     */
    public List<Subject> getAllSubjects() {
        return subjectDAO.getAll();
    }

    /**
     * Get subject by ID.
     */
    public Subject getSubjectById(int id) {
        return subjectDAO.getById(id);
    }

    /**
     * Create a new subject.
     */
    public boolean createSubject(Subject subject) {
        return subjectDAO.create(subject);
    }

    /**
     * Update an existing subject.
     */
    public boolean updateSubject(Subject subject) {
        return subjectDAO.update(subject);
    }

    /**
     * Delete a subject and all its attendance records.
     */
    public boolean deleteSubject(int subjectId) {
        attendanceRecordDAO.deleteBySubjectId(subjectId);
        return subjectDAO.delete(subjectId);
    }

    /**
     * Get attendance records for a subject.
     */
    public List<AttendanceRecord> getAttendanceRecords(int subjectId) {
        return attendanceRecordDAO.getBySubjectId(subjectId);
    }

    /**
     * Get attendance records for a specific date.
     */
    public List<AttendanceRecord> getAttendanceRecordsByDate(LocalDate date) {
        return attendanceRecordDAO.getByDate(date);
    }

    /**
     * Enum for attendance status levels (for color coding).
     */
    public enum AttendanceStatusLevel {
        SAFE, // Green
        WARNING, // Yellow
        RISK // Red
    }
}

package com.attendancemanager.dao;

import com.attendancemanager.model.Subject;
import com.attendancemanager.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Subject operations.
 */
public class SubjectDAO {
    private final Connection connection;

    public SubjectDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Create a new subject in the database.
     */
    public boolean create(Subject subject) {
        String sql = "INSERT INTO subjects (name, total_classes, attended_classes, target_percentage, color) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.getTotalClasses());
            pstmt.setInt(3, subject.getAttendedClasses());
            pstmt.setDouble(4, subject.getTargetPercentage());
            pstmt.setString(5, subject.getColor());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        subject.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating subject: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieve a subject by ID.
     */
    public Subject getById(int id) {
        String sql = "SELECT * FROM subjects WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToSubject(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving subject: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve all subjects.
     */
    public List<Subject> getAll() {
        List<Subject> subjects = new ArrayList<>();
        String sql = "SELECT * FROM subjects ORDER BY name";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                subjects.add(mapResultSetToSubject(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving subjects: " + e.getMessage());
        }
        return subjects;
    }

    /**
     * Update an existing subject.
     */
    public boolean update(Subject subject) {
        String sql = "UPDATE subjects SET name = ?, total_classes = ?, attended_classes = ?, target_percentage = ?, color = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, subject.getName());
            pstmt.setInt(2, subject.getTotalClasses());
            pstmt.setInt(3, subject.getAttendedClasses());
            pstmt.setDouble(4, subject.getTargetPercentage());
            pstmt.setString(5, subject.getColor());
            pstmt.setInt(6, subject.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating subject: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a subject by ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM subjects WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting subject: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update attendance counts for a subject.
     */
    public boolean updateAttendanceCounts(int subjectId, int totalClasses, int attendedClasses) {
        String sql = "UPDATE subjects SET total_classes = ?, attended_classes = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, totalClasses);
            pstmt.setInt(2, attendedClasses);
            pstmt.setInt(3, subjectId);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance counts: " + e.getMessage());
        }
        return false;
    }

    /**
     * Map ResultSet to Subject object.
     */
    private Subject mapResultSetToSubject(ResultSet rs) throws SQLException {
        Subject subject = new Subject(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("total_classes"),
                rs.getInt("attended_classes"),
                rs.getDouble("target_percentage"),
                rs.getString("color"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            subject.setCreatedAt(timestamp.toLocalDateTime());
        }

        return subject;
    }
}

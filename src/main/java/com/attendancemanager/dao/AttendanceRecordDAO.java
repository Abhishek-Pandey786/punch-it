package com.attendancemanager.dao;

import com.attendancemanager.model.AttendanceRecord;
import com.attendancemanager.model.AttendanceRecord.AttendanceStatus;
import com.attendancemanager.util.DatabaseManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for AttendanceRecord operations.
 */
public class AttendanceRecordDAO {
    private final Connection connection;

    public AttendanceRecordDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Create a new attendance record.
     */
    public boolean create(AttendanceRecord record) {
        String sql = "INSERT INTO attendance_records (subject_id, date, status, notes) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, record.getSubjectId());
            pstmt.setString(2, record.getDate().toString());
            pstmt.setString(3, record.getStatus().name());
            pstmt.setString(4, record.getNotes());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating attendance record: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Retrieve all attendance records for a specific subject.
     */
    public List<AttendanceRecord> getBySubjectId(int subjectId) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records WHERE subject_id = ? ORDER BY date DESC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance records: " + e.getMessage());
        }
        return records;
    }

    /**
     * Retrieve attendance record for a specific subject and date.
     */
    public AttendanceRecord getBySubjectAndDate(int subjectId, LocalDate date) {
        String sql = "SELECT * FROM attendance_records WHERE subject_id = ? AND date = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.setString(2, date.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToRecord(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance record: " + e.getMessage());
        }
        return null;
    }

    /**
     * Retrieve all attendance records for a specific date.
     */
    public List<AttendanceRecord> getByDate(LocalDate date) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records WHERE date = ? ORDER BY subject_id";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, date.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance records by date: " + e.getMessage());
        }
        return records;
    }

    /**
     * Update an existing attendance record.
     */
    public boolean update(AttendanceRecord record) {
        String sql = "UPDATE attendance_records SET status = ?, notes = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, record.getStatus().name());
            pstmt.setString(2, record.getNotes());
            pstmt.setInt(3, record.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating attendance record: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete an attendance record by ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM attendance_records WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance record: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete all attendance records for a specific subject.
     */
    public boolean deleteBySubjectId(int subjectId) {
        String sql = "DELETE FROM attendance_records WHERE subject_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting attendance records: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get attendance statistics for a subject.
     */
    public int[] getAttendanceStats(int subjectId) {
        String sql = """
                    SELECT
                        COUNT(*) as total,
                        SUM(CASE WHEN status = 'PRESENT' THEN 1 ELSE 0 END) as present,
                        SUM(CASE WHEN status = 'ABSENT' THEN 1 ELSE 0 END) as absent,
                        SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled
                    FROM attendance_records
                    WHERE subject_id = ?
                """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new int[] {
                        rs.getInt("total"),
                        rs.getInt("present"),
                        rs.getInt("absent"),
                        rs.getInt("cancelled")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance stats: " + e.getMessage());
        }
        return new int[] { 0, 0, 0, 0 };
    }

    /**
     * Get attendance records for a subject within a date range.
     * Used for analytics and trends.
     */
    public List<AttendanceRecord> getRecordsInDateRange(int subjectId, LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records WHERE subject_id = ? AND date BETWEEN ? AND ? ORDER BY date ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, subjectId);
            pstmt.setString(2, startDate.toString());
            pstmt.setString(3, endDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving attendance records in date range: " + e.getMessage());
        }
        return records;
    }

    /**
     * Get all attendance records across all subjects within a date range.
     * Used for heatmap calendar view.
     */
    public List<AttendanceRecord> getAllRecordsInDateRange(LocalDate startDate, LocalDate endDate) {
        List<AttendanceRecord> records = new ArrayList<>();
        String sql = "SELECT * FROM attendance_records WHERE date BETWEEN ? AND ? ORDER BY date ASC, subject_id ASC";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, startDate.toString());
            pstmt.setString(2, endDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(mapResultSetToRecord(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all attendance records in date range: " + e.getMessage());
        }
        return records;
    }

    /**
     * Map ResultSet to AttendanceRecord object.
     */
    private AttendanceRecord mapResultSetToRecord(ResultSet rs) throws SQLException {
        AttendanceRecord record = new AttendanceRecord(
                rs.getInt("id"),
                rs.getInt("subject_id"),
                LocalDate.parse(rs.getString("date")),
                AttendanceStatus.valueOf(rs.getString("status")),
                rs.getString("notes"));

        Timestamp timestamp = rs.getTimestamp("created_at");
        if (timestamp != null) {
            record.setCreatedAt(timestamp.toLocalDateTime());
        }

        return record;
    }
}

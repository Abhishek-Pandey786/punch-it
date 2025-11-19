package com.attendancemanager.dao;

import com.attendancemanager.model.TimetableEntry;
import com.attendancemanager.util.DatabaseManager;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Timetable operations.
 */
public class TimetableDAO {
    private final Connection connection;

    public TimetableDAO() {
        this.connection = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Create a new timetable entry.
     */
    public boolean create(TimetableEntry entry) {
        String sql = "INSERT INTO timetable (subject_id, day_of_week, start_time, end_time, room) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getSubjectId());
            pstmt.setString(2, entry.getDayOfWeek().name());
            pstmt.setString(3, entry.getStartTime().toString());
            pstmt.setString(4, entry.getEndTime().toString());
            pstmt.setString(5, entry.getRoom());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating timetable entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all timetable entries with subject names.
     */
    public List<TimetableEntry> getAll() {
        String sql = """
                SELECT t.id, t.subject_id, t.day_of_week, t.start_time, t.end_time, t.room, s.name as subject_name
                FROM timetable t
                JOIN subjects s ON t.subject_id = s.id
                ORDER BY
                    CASE t.day_of_week
                        WHEN 'MONDAY' THEN 1
                        WHEN 'TUESDAY' THEN 2
                        WHEN 'WEDNESDAY' THEN 3
                        WHEN 'THURSDAY' THEN 4
                        WHEN 'FRIDAY' THEN 5
                        WHEN 'SATURDAY' THEN 6
                        WHEN 'SUNDAY' THEN 7
                    END,
                    t.start_time
                """;

        List<TimetableEntry> entries = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                TimetableEntry entry = new TimetableEntry();
                entry.setId(rs.getInt("id"));
                entry.setSubjectId(rs.getInt("subject_id"));
                entry.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week")));
                entry.setStartTime(LocalTime.parse(rs.getString("start_time")));
                entry.setEndTime(LocalTime.parse(rs.getString("end_time")));
                entry.setRoom(rs.getString("room"));
                entry.setSubjectName(rs.getString("subject_name"));
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching timetable entries: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Get timetable entries for a specific day.
     */
    public List<TimetableEntry> getByDay(DayOfWeek day) {
        String sql = """
                SELECT t.id, t.subject_id, t.day_of_week, t.start_time, t.end_time, t.room, s.name as subject_name
                FROM timetable t
                JOIN subjects s ON t.subject_id = s.id
                WHERE t.day_of_week = ?
                ORDER BY t.start_time
                """;

        List<TimetableEntry> entries = new ArrayList<>();

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, day.name());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                TimetableEntry entry = new TimetableEntry();
                entry.setId(rs.getInt("id"));
                entry.setSubjectId(rs.getInt("subject_id"));
                entry.setDayOfWeek(DayOfWeek.valueOf(rs.getString("day_of_week")));
                entry.setStartTime(LocalTime.parse(rs.getString("start_time")));
                entry.setEndTime(LocalTime.parse(rs.getString("end_time")));
                entry.setRoom(rs.getString("room"));
                entry.setSubjectName(rs.getString("subject_name"));
                entries.add(entry);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching timetable entries for day: " + e.getMessage());
        }

        return entries;
    }

    /**
     * Update a timetable entry.
     */
    public boolean update(TimetableEntry entry) {
        String sql = "UPDATE timetable SET subject_id = ?, day_of_week = ?, start_time = ?, end_time = ?, room = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, entry.getSubjectId());
            pstmt.setString(2, entry.getDayOfWeek().name());
            pstmt.setString(3, entry.getStartTime().toString());
            pstmt.setString(4, entry.getEndTime().toString());
            pstmt.setString(5, entry.getRoom());
            pstmt.setInt(6, entry.getId());

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating timetable entry: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete a timetable entry.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM timetable WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting timetable entry: " + e.getMessage());
            return false;
        }
    }
}

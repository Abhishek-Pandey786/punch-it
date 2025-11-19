package com.attendancemanager.dao;

import com.attendancemanager.model.Profile;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Profile operations.
 * Manages profile data in the shared app_config.db database.
 */
public class ProfileDAO {
    private Connection connection;

    public ProfileDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Create the profiles table if it doesn't exist.
     */
    public void createTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS profiles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    last_accessed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error creating profiles table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a new profile.
     */
    public boolean create(Profile profile) {
        String sql = "INSERT INTO profiles (name, created_at, last_accessed_at) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, profile.getName());
            pstmt.setString(2, profile.getCreatedAt().toString());
            pstmt.setString(3, profile.getLastAccessedAt().toString());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        profile.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get all profiles.
     */
    public List<Profile> getAll() {
        List<Profile> profiles = new ArrayList<>();
        String sql = "SELECT * FROM profiles ORDER BY last_accessed_at DESC, name ASC";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                profiles.add(extractProfileFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all profiles: " + e.getMessage());
        }
        return profiles;
    }

    /**
     * Get profile by ID.
     */
    public Profile getById(int id) {
        String sql = "SELECT * FROM profiles WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractProfileFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting profile by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update profile.
     */
    public boolean update(Profile profile) {
        String sql = "UPDATE profiles SET name = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, profile.getName());
            pstmt.setInt(2, profile.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete profile by ID.
     */
    public boolean delete(int id) {
        String sql = "DELETE FROM profiles WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting profile: " + e.getMessage());
        }
        return false;
    }

    /**
     * Update last accessed timestamp for a profile.
     */
    public boolean updateLastAccessed(int id) {
        String sql = "UPDATE profiles SET last_accessed_at = ? WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, LocalDateTime.now().toString());
            pstmt.setInt(2, id);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating last accessed: " + e.getMessage());
        }
        return false;
    }

    /**
     * Get the most recently accessed profile.
     */
    public Profile getLastActive() {
        String sql = "SELECT * FROM profiles ORDER BY last_accessed_at DESC LIMIT 1";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return extractProfileFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting last active profile: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get profile count.
     */
    public int getCount() {
        String sql = "SELECT COUNT(*) FROM profiles";

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting profile count: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Extract Profile object from ResultSet.
     */
    private Profile extractProfileFromResultSet(ResultSet rs) throws SQLException {
        Profile profile = new Profile();
        profile.setId(rs.getInt("id"));
        profile.setName(rs.getString("name"));
        profile.setCreatedAt(LocalDateTime.parse(rs.getString("created_at")));
        profile.setLastAccessedAt(LocalDateTime.parse(rs.getString("last_accessed_at")));
        return profile;
    }
}

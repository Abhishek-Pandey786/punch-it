package com.attendancemanager.util;

import com.attendancemanager.dao.ProfileDAO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connections and initialization.
 * Supports multi-profile system with per-profile databases.
 * Singleton pattern ensures one database connection throughout the application.
 */
public class DatabaseManager {
    private static final String APP_CONFIG_DB_URL = "jdbc:sqlite:app_config.db";
    private static final String LEGACY_DB_PATH = "attendance.db";
    private static DatabaseManager instance;

    // Shared database for profile management
    private Connection configConnection;

    // Current profile's database connection
    private Connection profileConnection;
    private int currentProfileId = -1;

    private DatabaseManager() {
        try {
            // Initialize shared config database
            configConnection = DriverManager.getConnection(APP_CONFIG_DB_URL);
            initializeConfigTables();
            System.out.println("Config database connection established successfully.");

            // Handle migration from legacy single-user database
            migrateLegacyDatabase();
        } catch (SQLException e) {
            System.err.println("Failed to connect to config database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the singleton instance of DatabaseManager.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Get the current profile's database connection.
     */
    public Connection getConnection() {
        try {
            // Reconnect if connection is closed
            if (profileConnection == null || profileConnection.isClosed()) {
                if (currentProfileId > 0) {
                    String dbUrl = getProfileDatabaseUrl(currentProfileId);
                    profileConnection = DriverManager.getConnection(dbUrl);
                } else {
                    throw new IllegalStateException("No profile selected. Call switchProfile() first.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting profile connection: " + e.getMessage());
        }
        return profileConnection;
    }

    /**
     * Get the config database connection (for profile management).
     */
    public Connection getConfigConnection() {
        try {
            if (configConnection == null || configConnection.isClosed()) {
                configConnection = DriverManager.getConnection(APP_CONFIG_DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("Error getting config connection: " + e.getMessage());
        }
        return configConnection;
    }

    /**
     * Switch to a different profile's database.
     */
    public void switchProfile(int profileId) {
        try {
            // Close current profile connection if exists
            if (profileConnection != null && !profileConnection.isClosed()) {
                profileConnection.close();
            }

            // Connect to new profile's database
            String dbUrl = getProfileDatabaseUrl(profileId);
            profileConnection = DriverManager.getConnection(dbUrl);
            currentProfileId = profileId;

            // Initialize tables for this profile if needed
            initializeProfileTables();

            System.out.println("Switched to profile " + profileId + " database successfully.");
        } catch (SQLException e) {
            System.err.println("Error switching to profile " + profileId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the current profile ID.
     */
    public int getCurrentProfileId() {
        return currentProfileId;
    }

    /**
     * Get database URL for a specific profile.
     */
    private String getProfileDatabaseUrl(int profileId) {
        return "jdbc:sqlite:profile_" + profileId + ".db";
    }

    /**
     * Initialize config database tables (profiles table).
     */
    private void initializeConfigTables() {
        ProfileDAO profileDAO = new ProfileDAO(configConnection);
        profileDAO.createTable();
        System.out.println("Config database tables initialized successfully.");
    }

    /**
     * Initialize profile-specific database tables.
     */
    private void initializeProfileTables() {
        try (Statement stmt = profileConnection.createStatement()) {
            // Subjects table
            String createSubjectsTable = """
                        CREATE TABLE IF NOT EXISTS subjects (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            name TEXT NOT NULL UNIQUE,
                            total_classes INTEGER DEFAULT 0,
                            attended_classes INTEGER DEFAULT 0,
                            target_percentage REAL DEFAULT 75.0,
                            color TEXT DEFAULT '#3498db',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                        )
                    """;

            // Attendance records table
            String createAttendanceTable = """
                        CREATE TABLE IF NOT EXISTS attendance_records (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            subject_id INTEGER NOT NULL,
                            date DATE NOT NULL,
                            status TEXT NOT NULL CHECK(status IN ('PRESENT', 'ABSENT', 'CANCELLED')),
                            notes TEXT,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE,
                            UNIQUE(subject_id, date)
                        )
                    """;

            // Settings table for profile-specific configuration
            String createSettingsTable = """
                        CREATE TABLE IF NOT EXISTS settings (
                            key TEXT PRIMARY KEY,
                            value TEXT NOT NULL
                        )
                    """;

            // Timetable table for weekly schedule
            String createTimetableTable = """
                        CREATE TABLE IF NOT EXISTS timetable (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            subject_id INTEGER NOT NULL,
                            day_of_week TEXT NOT NULL CHECK(day_of_week IN ('MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY')),
                            start_time TEXT NOT NULL,
                            end_time TEXT NOT NULL,
                            room TEXT,
                            FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
                        )
                    """;

            stmt.execute(createSubjectsTable);
            stmt.execute(createAttendanceTable);
            stmt.execute(createSettingsTable);
            stmt.execute(createTimetableTable);

            // Insert default settings if not exists
            String insertDefaultSettings = """
                        INSERT OR IGNORE INTO settings (key, value) VALUES
                        ('theme', 'light'),
                        ('default_subject_target', '75.0'),
                        ('overall_target', '85.0')
                    """;
            stmt.execute(insertDefaultSettings);

        } catch (SQLException e) {
            System.err.println("Error initializing profile tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Migrate legacy single-user database to multi-profile system.
     * Moves attendance.db to profile_1.db if it exists and no profiles exist yet.
     */
    private void migrateLegacyDatabase() {
        File legacyFile = new File(LEGACY_DB_PATH);
        ProfileDAO profileDAO = new ProfileDAO(configConnection);

        // Only migrate if legacy database exists and no profiles exist
        if (legacyFile.exists() && profileDAO.getCount() == 0) {
            try {
                System.out.println("Detected legacy database. Starting migration...");

                // Create default profile
                com.attendancemanager.model.Profile defaultProfile = new com.attendancemanager.model.Profile();
                defaultProfile.setName("My Attendance");

                if (profileDAO.create(defaultProfile)) {
                    int profileId = defaultProfile.getId();
                    File targetFile = new File("profile_" + profileId + ".db");

                    // Copy legacy database to new profile database
                    Files.copy(legacyFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                    System.out.println("Migration successful! Legacy data moved to profile " + profileId);

                    // Rename legacy file to .bak
                    File backupFile = new File(LEGACY_DB_PATH + ".bak");
                    legacyFile.renameTo(backupFile);
                    System.out.println("Legacy database backed up as " + backupFile.getName());
                }
            } catch (IOException e) {
                System.err.println("Error migrating legacy database: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Close all database connections.
     */
    public void closeConnections() {
        try {
            if (profileConnection != null && !profileConnection.isClosed()) {
                profileConnection.close();
                System.out.println("Profile database connection closed.");
            }
            if (configConnection != null && !configConnection.isClosed()) {
                configConnection.close();
                System.out.println("Config database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing connections: " + e.getMessage());
        }
    }

    /**
     * Backward compatibility method - deprecated.
     * 
     * @deprecated Use closeConnections() instead
     */
    @Deprecated
    public void closeConnection() {
        closeConnections();
    }
}

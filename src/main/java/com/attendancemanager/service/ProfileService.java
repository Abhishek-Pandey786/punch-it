package com.attendancemanager.service;

import com.attendancemanager.dao.ProfileDAO;
import com.attendancemanager.model.Profile;

import java.util.List;

/**
 * Service layer for profile management operations.
 * Handles business logic for multi-profile system.
 */
public class ProfileService {
    private ProfileDAO profileDAO;

    public ProfileService(ProfileDAO profileDAO) {
        this.profileDAO = profileDAO;
    }

    /**
     * Get all profiles sorted by last accessed (most recent first).
     */
    public List<Profile> getAllProfiles() {
        return profileDAO.getAll();
    }

    /**
     * Create a new profile with validation.
     */
    public Profile createProfile(String name) {
        // Validate name
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }

        name = name.trim();

        if (name.length() > 50) {
            throw new IllegalArgumentException("Profile name must be 50 characters or less");
        }

        // Check for duplicate names
        List<Profile> existingProfiles = profileDAO.getAll();
        for (Profile existing : existingProfiles) {
            if (existing.getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Profile with this name already exists");
            }
        }

        // Create profile
        Profile profile = new Profile();
        profile.setName(name);

        if (profileDAO.create(profile)) {
            return profile;
        }

        throw new RuntimeException("Failed to create profile");
    }

    /**
     * Update an existing profile.
     */
    public boolean updateProfile(Profile profile) {
        if (profile == null || profile.getId() <= 0) {
            throw new IllegalArgumentException("Invalid profile");
        }

        String name = profile.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Profile name cannot be empty");
        }

        name = name.trim();
        if (name.length() > 50) {
            throw new IllegalArgumentException("Profile name must be 50 characters or less");
        }

        // Check for duplicate names (excluding current profile)
        List<Profile> existingProfiles = profileDAO.getAll();
        for (Profile existing : existingProfiles) {
            if (existing.getId() != profile.getId() &&
                    existing.getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Profile with this name already exists");
            }
        }

        profile.setName(name);
        return profileDAO.update(profile);
    }

    /**
     * Delete a profile by ID.
     */
    public boolean deleteProfile(int profileId) {
        // Prevent deleting the only profile
        if (profileDAO.getCount() <= 1) {
            throw new IllegalArgumentException("Cannot delete the last profile");
        }

        return profileDAO.delete(profileId);
    }

    /**
     * Get profile by ID.
     */
    public Profile getProfileById(int id) {
        return profileDAO.getById(id);
    }

    /**
     * Update last accessed timestamp for a profile.
     */
    public boolean updateLastAccessed(int profileId) {
        return profileDAO.updateLastAccessed(profileId);
    }

    /**
     * Get the most recently accessed profile.
     */
    public Profile getLastActiveProfile() {
        return profileDAO.getLastActive();
    }

    /**
     * Ensure a default profile exists. Create one if no profiles exist.
     */
    public Profile ensureDefaultProfileExists() {
        List<Profile> profiles = profileDAO.getAll();

        if (profiles.isEmpty()) {
            // Create default profile
            Profile defaultProfile = new Profile();
            defaultProfile.setName("Default Profile");

            if (profileDAO.create(defaultProfile)) {
                return defaultProfile;
            } else {
                throw new RuntimeException("Failed to create default profile");
            }
        }

        // Return the first profile (or last active)
        Profile lastActive = profileDAO.getLastActive();
        return lastActive != null ? lastActive : profiles.get(0);
    }

    /**
     * Get total number of profiles.
     */
    public int getProfileCount() {
        return profileDAO.getCount();
    }

    /**
     * Check if profile name is available.
     */
    public boolean isNameAvailable(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        List<Profile> profiles = profileDAO.getAll();
        for (Profile profile : profiles) {
            if (profile.getName().equalsIgnoreCase(name.trim())) {
                return false;
            }
        }
        return true;
    }
}

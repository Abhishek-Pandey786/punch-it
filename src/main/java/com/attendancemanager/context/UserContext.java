package com.attendancemanager.context;

import com.attendancemanager.model.Profile;

/**
 * Singleton context to store the currently active user profile.
 * Used throughout the application to maintain profile state.
 */
public class UserContext {
    private static UserContext instance;
    private Profile currentProfile;

    private UserContext() {
        // Private constructor for singleton
    }

    /**
     * Get the singleton instance of UserContext.
     */
    public static UserContext getInstance() {
        if (instance == null) {
            synchronized (UserContext.class) {
                if (instance == null) {
                    instance = new UserContext();
                }
            }
        }
        return instance;
    }

    /**
     * Get the current active profile.
     */
    public Profile getCurrentProfile() {
        return currentProfile;
    }

    /**
     * Set the current active profile.
     */
    public void setCurrentProfile(Profile profile) {
        this.currentProfile = profile;
    }

    /**
     * Check if a profile is currently set.
     */
    public boolean hasProfile() {
        return currentProfile != null;
    }

    /**
     * Get the current profile ID.
     */
    public int getCurrentProfileId() {
        return currentProfile != null ? currentProfile.getId() : -1;
    }

    /**
     * Get the current profile name.
     */
    public String getCurrentProfileName() {
        return currentProfile != null ? currentProfile.getName() : "No Profile";
    }

    /**
     * Clear the current profile context (logout).
     */
    public void clearContext() {
        this.currentProfile = null;
    }
}

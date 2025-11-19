package com.attendancemanager.model;

import java.time.LocalDateTime;

/**
 * Represents a user profile for multi-profile support.
 * Each profile maintains separate attendance data.
 */
public class Profile {
    private int id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessedAt;

    public Profile() {
        this.createdAt = LocalDateTime.now();
        this.lastAccessedAt = LocalDateTime.now();
    }

    public Profile(int id, String name, LocalDateTime createdAt, LocalDateTime lastAccessedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null || getClass() != obj.getClass())
            return false;
        Profile profile = (Profile) obj;
        return id == profile.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }
}

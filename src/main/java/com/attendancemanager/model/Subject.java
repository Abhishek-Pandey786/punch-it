package com.attendancemanager.model;

import javafx.beans.property.*;

import java.time.LocalDateTime;

/**
 * Model class representing a Subject with JavaFX properties for UI binding.
 */
public class Subject {
    private final IntegerProperty id;
    private final StringProperty name;
    private final IntegerProperty totalClasses;
    private final IntegerProperty attendedClasses;
    private final DoubleProperty targetPercentage;
    private final StringProperty color;
    private final ObjectProperty<LocalDateTime> createdAt;

    public Subject() {
        this(0, "", 0, 0, 75.0, "#3498db");
    }

    public Subject(int id, String name, int totalClasses, int attendedClasses,
            double targetPercentage, String color) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.totalClasses = new SimpleIntegerProperty(totalClasses);
        this.attendedClasses = new SimpleIntegerProperty(attendedClasses);
        this.targetPercentage = new SimpleDoubleProperty(targetPercentage);
        this.color = new SimpleStringProperty(color);
        this.createdAt = new SimpleObjectProperty<>(LocalDateTime.now());
    }

    // ID Property
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public IntegerProperty idProperty() {
        return id;
    }

    // Name Property
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    // Total Classes Property
    public int getTotalClasses() {
        return totalClasses.get();
    }

    public void setTotalClasses(int totalClasses) {
        this.totalClasses.set(totalClasses);
    }

    public IntegerProperty totalClassesProperty() {
        return totalClasses;
    }

    // Attended Classes Property
    public int getAttendedClasses() {
        return attendedClasses.get();
    }

    public void setAttendedClasses(int attendedClasses) {
        this.attendedClasses.set(attendedClasses);
    }

    public IntegerProperty attendedClassesProperty() {
        return attendedClasses;
    }

    // Target Percentage Property
    public double getTargetPercentage() {
        return targetPercentage.get();
    }

    public void setTargetPercentage(double targetPercentage) {
        this.targetPercentage.set(targetPercentage);
    }

    public DoubleProperty targetPercentageProperty() {
        return targetPercentage;
    }

    // Color Property
    public String getColor() {
        return color.get();
    }

    public void setColor(String color) {
        this.color.set(color);
    }

    public StringProperty colorProperty() {
        return color;
    }

    // Created At Property
    public LocalDateTime getCreatedAt() {
        return createdAt.get();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt.set(createdAt);
    }

    public ObjectProperty<LocalDateTime> createdAtProperty() {
        return createdAt;
    }

    /**
     * Calculate current attendance percentage.
     */
    public double getCurrentPercentage() {
        if (totalClasses.get() == 0) {
            return 0.0;
        }
        return (attendedClasses.get() * 100.0) / totalClasses.get();
    }

    @Override
    public String toString() {
        return String.format("%s (%.1f%%)", name.get(), getCurrentPercentage());
    }
}

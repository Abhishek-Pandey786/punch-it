package com.attendancemanager.model;

import javafx.beans.property.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model class representing an Attendance Record with JavaFX properties.
 */
public class AttendanceRecord {
    private final IntegerProperty id;
    private final IntegerProperty subjectId;
    private final ObjectProperty<LocalDate> date;
    private final ObjectProperty<AttendanceStatus> status;
    private final StringProperty notes;
    private final ObjectProperty<LocalDateTime> createdAt;

    public enum AttendanceStatus {
        PRESENT, ABSENT, CANCELLED
    }

    public AttendanceRecord() {
        this(0, 0, LocalDate.now(), AttendanceStatus.PRESENT, "");
    }

    public AttendanceRecord(int id, int subjectId, LocalDate date,
            AttendanceStatus status, String notes) {
        this.id = new SimpleIntegerProperty(id);
        this.subjectId = new SimpleIntegerProperty(subjectId);
        this.date = new SimpleObjectProperty<>(date);
        this.status = new SimpleObjectProperty<>(status);
        this.notes = new SimpleStringProperty(notes);
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

    // Subject ID Property
    public int getSubjectId() {
        return subjectId.get();
    }

    public void setSubjectId(int subjectId) {
        this.subjectId.set(subjectId);
    }

    public IntegerProperty subjectIdProperty() {
        return subjectId;
    }

    // Date Property
    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    // Status Property
    public AttendanceStatus getStatus() {
        return status.get();
    }

    public void setStatus(AttendanceStatus status) {
        this.status.set(status);
    }

    public ObjectProperty<AttendanceStatus> statusProperty() {
        return status;
    }

    // Notes Property
    public String getNotes() {
        return notes.get();
    }

    public void setNotes(String notes) {
        this.notes.set(notes);
    }

    public StringProperty notesProperty() {
        return notes;
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

    @Override
    public String toString() {
        return String.format("%s - %s", date.get(), status.get());
    }
}

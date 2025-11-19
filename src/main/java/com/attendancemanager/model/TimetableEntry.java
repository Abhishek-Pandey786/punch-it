package com.attendancemanager.model;

import javafx.beans.property.*;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Model class representing a timetable entry.
 */
public class TimetableEntry {
    private final IntegerProperty id;
    private final IntegerProperty subjectId;
    private final ObjectProperty<DayOfWeek> dayOfWeek;
    private final ObjectProperty<LocalTime> startTime;
    private final ObjectProperty<LocalTime> endTime;
    private final StringProperty room;
    private final StringProperty subjectName; // For display purposes

    public TimetableEntry() {
        this.id = new SimpleIntegerProperty();
        this.subjectId = new SimpleIntegerProperty();
        this.dayOfWeek = new SimpleObjectProperty<>();
        this.startTime = new SimpleObjectProperty<>();
        this.endTime = new SimpleObjectProperty<>();
        this.room = new SimpleStringProperty();
        this.subjectName = new SimpleStringProperty();
    }

    public TimetableEntry(int id, int subjectId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime,
            String room) {
        this();
        setId(id);
        setSubjectId(subjectId);
        setDayOfWeek(dayOfWeek);
        setStartTime(startTime);
        setEndTime(endTime);
        setRoom(room);
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

    // Day of Week Property
    public DayOfWeek getDayOfWeek() {
        return dayOfWeek.get();
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek.set(dayOfWeek);
    }

    public ObjectProperty<DayOfWeek> dayOfWeekProperty() {
        return dayOfWeek;
    }

    // Start Time Property
    public LocalTime getStartTime() {
        return startTime.get();
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime.set(startTime);
    }

    public ObjectProperty<LocalTime> startTimeProperty() {
        return startTime;
    }

    // End Time Property
    public LocalTime getEndTime() {
        return endTime.get();
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime.set(endTime);
    }

    public ObjectProperty<LocalTime> endTimeProperty() {
        return endTime;
    }

    // Room Property
    public String getRoom() {
        return room.get();
    }

    public void setRoom(String room) {
        this.room.set(room);
    }

    public StringProperty roomProperty() {
        return room;
    }

    // Subject Name Property (for display)
    public String getSubjectName() {
        return subjectName.get();
    }

    public void setSubjectName(String subjectName) {
        this.subjectName.set(subjectName);
    }

    public StringProperty subjectNameProperty() {
        return subjectName;
    }
}

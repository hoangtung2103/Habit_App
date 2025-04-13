package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;


public class HabitNote {
    private String id;
    private String habitId;
    private String userId;
    private String content;
    private Timestamp createAt;

    public HabitNote() {
    }

    public HabitNote(String id, String habitId, String userId, String content, Timestamp createAt) {
        this.id = id;
        this.habitId = habitId;
        this.userId = userId;
        this.content = content;
        this.createAt = createAt;
    }

    // Getters & Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHabitId() {
        return habitId;
    }

    public void setHabitId(String habitId) {
        this.habitId = habitId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Timestamp createAt) {
        this.createAt = createAt;
    }
}

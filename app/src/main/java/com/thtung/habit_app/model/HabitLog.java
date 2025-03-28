package com.thtung.habit_app.model;

public class HabitLog {
    private String logId;
    private String habitId;
    private String userId;
    private String date;  // yyyy-MM-dd
    private boolean completed;

    public HabitLog() {
    }

    public HabitLog(String logId, String habitId, String userId, String date, boolean completed) {
        this.logId = logId;
        this.habitId = habitId;
        this.userId = userId;
        this.date = date;
        this.completed = completed;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

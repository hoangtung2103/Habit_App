package com.thtung.habit_app.model;

public class HabitLog {
    private String id;
    private String habit_id;
    private String user_id;
    private String date;  // yyyy-MM-dd
    private boolean completed;

    public HabitLog() {
    }

    public HabitLog(String id, String habit_id, String user_id, String date, boolean completed) {
        this.id = id;
        this.habit_id = habit_id;
        this.user_id = user_id;
        this.date = date;
        this.completed = completed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHabit_id() {
        return habit_id;
    }

    public void setHabit_id(String habit_id) {
        this.habit_id = habit_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

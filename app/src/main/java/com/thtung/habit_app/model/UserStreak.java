package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;
import com.google.type.Date;

import java.time.LocalDate;

public class UserStreak {
    private String id;
    private String userId;
    private String streakType; // "HABIT" hoặc "PERFECT_DAY"
    private String habitId; // Nullable
    private int currentStreak;
    private Timestamp lastCompletionDate;

    public UserStreak() {}

    public UserStreak(String userId, String streakType, String habitId, int currentStreak, Timestamp lastCompletionDate) {
        this.userId = userId;
        this.streakType = streakType;
        this.habitId = habitId;
        this.currentStreak = currentStreak;
        this.lastCompletionDate = lastCompletionDate;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getStreakType() { return streakType; }
    public void setStreakType(String streakType) { this.streakType = streakType; }
    public String getHabitId() { return habitId; }
    public void setHabitId(String habitId) { this.habitId = habitId; }
    public int getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(int currentStreak) { this.currentStreak = currentStreak; }
    public Timestamp getLastCompletionDate() { return lastCompletionDate; }
    public void setLastCompletionDate(Timestamp lastCompletionDate) { this.lastCompletionDate = lastCompletionDate; }
}
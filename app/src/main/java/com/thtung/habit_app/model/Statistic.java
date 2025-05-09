package com.thtung.habit_app.model;

public class Statistic {
    String id;
    String habit_id;
    String user_id;
    Long total_completed;
    Long last_completed;
    long streak;
    long max_streak;

    public Statistic() {
    }

    public Statistic(String id, String habit_id, String user_id, Long total_completed, Long last_completed, long streak, long max_streak) {
        this.id = id;
        this.habit_id = habit_id;
        this.user_id = user_id;
        this.total_completed = total_completed;
        this.last_completed = last_completed;
        this.streak = streak;
        this.max_streak = max_streak;
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

    public Long getTotal_completed() {  // Sửa từ long thành Long
        return total_completed;
    }

    public void setTotal_completed(Long total_completed) {
        this.total_completed = total_completed;
    }

    public Long getLast_completed() {  // Sửa từ long thành Long
        return last_completed;
    }

    public void setLast_completed(Long last_completed) {
        this.last_completed = last_completed;
    }

    public long getStreak() {
        return streak;
    }

    public void setStreak(long streak) {
        this.streak = streak;
    }

    public long getMax_streak() {
        return max_streak;
    }

    public void setMax_streak(long max_streak) {
        this.max_streak = max_streak;
    }
}
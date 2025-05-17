package com.thtung.habit_app.model;

public class BarChartData {
    private String habitId;
    private String habitName;
    private double successPercent;

    public BarChartData(String habitId, String habitName, double successPercent) {
        this.habitId = habitId;
        this.habitName = habitName;
        this.successPercent = successPercent;
    }

    public String getHabitId() {
        return habitId;
    }

    public String getHabitName() {
        return habitName;
    }

    public double getSuccessPercent() {
        return successPercent;
    }
}
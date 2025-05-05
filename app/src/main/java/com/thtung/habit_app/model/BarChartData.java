package com.thtung.habit_app.model;

public class BarChartData {
    private String habitId;
    private double successPercent;

    public BarChartData(String habitId, double successPercent) {
        this.habitId = habitId;
        this.successPercent = successPercent;
    }

    public String getHabitId() {
        return habitId;
    }

    public double getSuccessPercent() {
        return successPercent;
    }
}
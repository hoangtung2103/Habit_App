package com.thtung.habit_app.model;

public class DayModel {
    public String dayName;
    public String dayNumber;
    public boolean isSelected;

    public DayModel(String dayName, String dayNumber, boolean isSelected) {
        this.dayName = dayName;
        this.dayNumber = dayNumber;
        this.isSelected = isSelected;
    }
}


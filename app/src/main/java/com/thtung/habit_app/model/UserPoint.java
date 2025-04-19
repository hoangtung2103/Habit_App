package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;
import com.google.type.DateTime;

import java.sql.Time;

public class UserPoint {
    private String user_id;
    private int total_point;
    private Timestamp last_update;
    private Timestamp lastLoginPointAwardDate;

    public UserPoint() {
    }

    public UserPoint(String user_id, int total_point, Timestamp last_update) {
        this.user_id = user_id;
        this.total_point = total_point;
        this.last_update = last_update;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public int getTotal_point() {
        return total_point;
    }

    public void setTotal_point(int total_point) {
        this.total_point = total_point;
    }

    public Timestamp getLast_update() {
        return last_update;
    }

    public void setLast_update(Timestamp last_update) {
        this.last_update = last_update;
    }
    public Timestamp getLastLoginPointAwardDate() { return lastLoginPointAwardDate; }
    public void setLastLoginPointAwardDate(Timestamp lastLoginPointAwardDate) { this.lastLoginPointAwardDate = lastLoginPointAwardDate; }
}

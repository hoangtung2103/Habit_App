package com.thtung.habit_app.model;

import java.util.Date;

public class UserMilestone {
    public String id;
    public String user_id;
    public String milestone_id;
    public int current;
    public Date achieved_at;

    public UserMilestone() {
    }

    public UserMilestone(int current){
        this.current = current;
    }
    public UserMilestone(String user_id, String milestone_id){
        this.user_id = user_id;
        this.milestone_id = milestone_id;
        this.current = 0;
    }

    public UserMilestone(String id, String user_id, String milestone_id, int current, Date achieved_at) {
        this.id = id;
        this.user_id = user_id;
        this.milestone_id = milestone_id;
        this.current = current;
        this.achieved_at = achieved_at;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMilestone_id() {
        return milestone_id;
    }

    public void setMilestone_id(String milestone_id) {
        this.milestone_id = milestone_id;
    }

    public int getCurrent() {
        return current;
    }

    public void setCurrent(int current) {
        this.current = current;
    }

    public Date getAchieved_at() {
        return achieved_at;
    }

    public void setAchieved_at(Date achieved_at) {
        this.achieved_at = achieved_at;
    }
}

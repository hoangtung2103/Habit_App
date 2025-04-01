package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;

public class Habit {
    private String id;
    private String user_id;
    private String name;
    private String url_icon;
    private String repeat;
    private String target;
    private String remind_time;
    private String description;
    private Timestamp start_at;


    public Habit() {

    }

    public Habit(String id, String user_id, String name, String url_icon, String repeat, String target, String remind_time, String description, Timestamp start_at) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.url_icon = url_icon;
        this.repeat = repeat;
        this.target = target;
        this.remind_time = remind_time;
        this.description = description;
        this.start_at = start_at;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl_icon() {
        return url_icon;
    }

    public void setUrl_icon(String url_icon) {
        this.url_icon = url_icon;
    }

    public String getRepeat() {
        return repeat;
    }

    public void setRepeat(String repeat) {
        this.repeat = repeat;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getRemind_time() {
        return remind_time;
    }

    public void setRemind_time(String remind_time) {
        this.remind_time = remind_time;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Timestamp getStart_at() {
        return start_at;
    }

    public void setStart_at(Timestamp start_at) {
        this.start_at = start_at;
    }
}

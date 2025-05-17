package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;

public class Feedback {
    private String id;
    private String user_id;
    private String content;
    private int rating;
    private Timestamp create_at;

    public Feedback() {
    }

    public Feedback(String id, String user_id, String content, int rating, Timestamp create_at) {
        this.id = id;
        this.user_id = user_id;
        this.content = content;
        this.rating = rating;
        this.create_at = create_at;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public Timestamp getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Timestamp create_at) {
        this.create_at = create_at;
    }
}

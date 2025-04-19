package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.ServerTimestamp;

public class PointHistory {
    public String id;
    public String user_id;
    public int point;
    public String reason;
    @ServerTimestamp
    public Timestamp create_at;

    public PointHistory() {
    }

    public PointHistory(String id, String user_id, int point, String reason, Timestamp create_at) {
        this.id = id;
        this.user_id = user_id;
        this.point = point;
        this.reason = reason;
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

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getCreate_at() {
        return create_at;
    }

    public void setCreate_at(Timestamp create_at) {
        this.create_at = create_at;
    }
}

package com.thtung.habit_app.model;

import com.google.firebase.Timestamp;

public class Habit {
    private String habitID;
    private String userID;
    private String habitName;
    private String iconUrl;
    private String lapLai;
    private String mucTieu;
    private String timeNhacNho;
    private String ghiChu;
    private Timestamp ngayBD;


    public Habit() {

    }

    public Habit(String habitID, String userID, String habitName, String iconUrl, String lapLai, String mucTieu, String timeNhacNho, String ghiChu, Timestamp ngayBD) {
        this.habitID = habitID;
        this.userID = userID;
        this.habitName = habitName;
        this.iconUrl = iconUrl;
        this.lapLai = lapLai;
        this.mucTieu = mucTieu;
        this.timeNhacNho = timeNhacNho;
        this.ghiChu = ghiChu;
        this.ngayBD = ngayBD;
    }

    public String getHabitID() {
        return habitID;
    }

    public void setHabitID(String habitID) {
        this.habitID = habitID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getHabitName() {
        return habitName;
    }

    public void setHabitName(String habitName) {
        this.habitName = habitName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getLapLai() {
        return lapLai;
    }

    public void setLapLai(String lapLai) {
        this.lapLai = lapLai;
    }

    public String getMucTieu() {
        return mucTieu;
    }

    public void setMucTieu(String mucTieu) {
        this.mucTieu = mucTieu;
    }

    public String getTimeNhacNho() {
        return timeNhacNho;
    }

    public void setTimeNhacNho(String timeNhacNho) {
        this.timeNhacNho = timeNhacNho;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    public Timestamp getNgayBD() {
        return ngayBD;
    }

    public void setNgayBD(Timestamp ngayBD) {
        this.ngayBD = ngayBD;
    }
}

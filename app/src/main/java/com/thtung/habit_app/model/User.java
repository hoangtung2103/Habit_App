package com.thtung.habit_app.model;

public class User {
    private String id;
    private String name;
    private String email;
    private String avatar;
    private String birthdate;
    private String gender;
    private String description;



    public User() {

    }

    public User(String id, String name, String email, String avatar, String birthdate, String gender, String description) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avatar = avatar;
        this.birthdate = birthdate;
        this.gender = gender;
        this.description = description;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}


package com.example.fake_twitter_android;

import java.util.Date;

public class User {
    private String id;
    private String username;
    private String phone;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    private String gender;

    public User(String id, String username, String phone, String gender) {
        this.id = id;
        this.username = username;
        this.phone = phone;
        this.gender = gender;
    }

    public User() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

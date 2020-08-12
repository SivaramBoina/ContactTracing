package com.sivaram.contacttracing.helpers;

import java.sql.Timestamp;

public class User {
    String username;
    String password;
    String contact;
    String time;
    public User(){

    }

    public User(String username, String password, String contact, String time) {
        this.username = username;
        this.password = password;
        this.contact = contact;
        this.time = time;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }



    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

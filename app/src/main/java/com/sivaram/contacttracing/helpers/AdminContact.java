package com.sivaram.contacttracing.helpers;

public class AdminContact {
    String f_username;
    String f_contact;
    String f_rssi;
    String f_time;

    public AdminContact(String f_username, String f_contact, String f_rssi, String f_time) {
        this.f_username = f_username;
        this.f_contact = f_contact;
        this.f_rssi = f_rssi;
        this.f_time = f_time;
    }

    public String getF_username() {
        return f_username;
    }

    public void setF_username(String f_username) {
        this.f_username = f_username;
    }

    public String getF_contact() {
        return f_contact;
    }

    public void setF_contact(String f_contact) {
        this.f_contact = f_contact;
    }

    public String getF_rssi() {
        return f_rssi;
    }

    public void setF_rssi(String f_rssi) {
        this.f_rssi = f_rssi;
    }

    public String getF_time() {
        return f_time;
    }

    public void setF_time(String f_time) {
        this.f_time = f_time;
    }
}

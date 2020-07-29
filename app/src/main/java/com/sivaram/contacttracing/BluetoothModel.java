package com.sivaram.contacttracing;

public class BluetoothModel {
    String contact;
    String rssi;
    String time;

    public BluetoothModel() {
    }

    public String getContact() {
        return contact;
    }

    public String getRssi() {
        return rssi;
    }

    public String getTime() {
        return time;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

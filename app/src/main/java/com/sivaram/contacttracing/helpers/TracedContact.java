package com.sivaram.contacttracing.helpers;

public class TracedContact {
    String contact_id;
    String traced_contact;
    String rssi;
    String time;

    public TracedContact(){

    }
    public TracedContact(String contact_id, String traced_contact, String rssi, String time) {
        this.contact_id = contact_id;
        this.traced_contact = traced_contact;
        this.rssi = rssi;
        this.time = time;
    }

    public String getContact_id() {
        return contact_id;
    }

    public void setContact_id(String contact_id) {
        this.contact_id = contact_id;
    }

    public String getTraced_contact() {
        return traced_contact;
    }

    public void setTraced_contact(String traced_contact) {
        this.traced_contact = traced_contact;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}

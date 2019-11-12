package com.FinalProject.TomAlon.YardenShaish.Install;

import java.util.Date;

public class Install {
    private String uid;
    private StatusEnum status;
    private String name;
    private String phone;
    private String address;
    private Date time;
    private String marbleSize;

    public Install() {
        // empty constructor for populating from firebase with toObject()
    }

    public Install(String name, String phone, String address, String marbleSize, Date time, String status, String uid) {
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.marbleSize = marbleSize;
        this.time = time;
        this.uid = uid;
        setStatus(status);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getMarbleSize() {
        return marbleSize;
    }

    public void setMarbleSize(String marbleSize) {
        this.marbleSize = marbleSize;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status.toString();
    }

    public StatusEnum getStatusEnum() {
        return status;
    }

    public void setStatus(String status) {
        this.status = StatusEnum.valueOf(status.toUpperCase());
    }
}
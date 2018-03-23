package com.mrgames13.jimdo.vehiclesafe.CommonObjects;

import android.support.annotation.NonNull;

import java.util.Date;

public class Broadcast implements Comparable{

    //Konstanten
    public static final int STATE_LOCKED = Device.STATE_LOCKED;
    public static final int STATE_UNLOCKED = Device.STATE_UNLOCKED;
    public static final int STATE_STOLEN = Device.STATE_STOLEN;

    //Variablen als Objekte

    //Variablen
    private Date time_stamp;
    private String from_id;
    private String cmd;
    private double longitude;
    private double latitude;
    private int lock_mode;

    public Broadcast() {}

    public Broadcast(Date time_stamp, String from_id, String cmd, double longitude, double latitude, int lock_mode) {
        this.time_stamp = time_stamp;
        this.from_id = from_id;
        this.cmd = cmd;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lock_mode = lock_mode;
    }

    public Date getTimeStamp() {
        return time_stamp;
    }
    public void setTimeStamp(Date time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getFrom() {
        return from_id;
    }
    public void setFrom(String from_id) {
        this.from_id = from_id;
    }

    public String getCmd() {
        return cmd;
    }
    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public int getLockMode() {
        return lock_mode;
    }
    public void setLockMode(int lock_mode) {
        this.lock_mode = lock_mode;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        Broadcast other_record = (Broadcast) another;
        return getTimeStamp().compareTo(other_record.getTimeStamp());
    }
}
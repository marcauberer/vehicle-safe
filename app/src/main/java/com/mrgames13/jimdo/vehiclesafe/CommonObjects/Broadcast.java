package com.mrgames13.jimdo.vehiclesafe.CommonObjects;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Broadcast implements Comparable{

    //Konstanten
    public static final int STATE_LOCKED = 1;
    public static final int STATE_UNLOCKED = 2;
    public static final int STATE_STOLEN = 3;

    //Variablen als Objekte

    //Variablen
    private int device_id;
    private long time_stamp;
    private int lock_mode;
    private double latitude;
    private double longitude;
    private double altitude;
    private double speed;
    private boolean fix;
    private double fix_quality;

    public Broadcast() {}

    public Broadcast(int device_id, long time_stamp, int lock_mode, double latitude, double longitude, double altitude, double speed, boolean fix, double fix_quality) {
        this.device_id = device_id;
        this.time_stamp = time_stamp;
        this.lock_mode = lock_mode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.speed = speed;
        this.fix = fix;
        this.fix_quality = fix_quality;
    }

    public int getDeviceID() {
        return device_id;
    }
    public void setDeviceID(int device_id) {
        this.device_id = device_id;
    }

    public long getTimeStampLong() {
        return time_stamp;
    }
    public String getTimeStampString() {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(time_stamp));
    }
    public void setTimeStamp(long time_stamp) {
        this.time_stamp = time_stamp;
    }

    public int getLockMode() {
        return lock_mode;
    }
    public void setLockMode(int lock_mode) {
        this.lock_mode = lock_mode;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }
    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getSpeed() {
        return speed;
    }
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    public boolean isFix() {
        return fix;
    }
    public void setFix(boolean fix) {
        this.fix = fix;
    }

    public double getFixQuality() {
        return fix_quality;
    }
    public void setFixQuality(double fix_quality) {
        this.fix_quality = fix_quality;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        Broadcast other_record = (Broadcast) another;
        return getTimeStampString().compareTo(other_record.getTimeStampString());
    }
}
package com.mrgames13.jimdo.vehiclesafe.CommonObjects;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Device implements Comparable {

    //Konstanten
    public static final int TYPE_BIKE = 1;
    public static final int TYPE_MOTORCYCLE = 2;
    public static final int TYPE_CAR = 3;

    public static final int STATE_LOCKED = 10;
    public static final int STATE_UNLOCKED = 11;
    public static final int STATE_STOLEN = 12;

    //Variablen als Objekte

    //Variablen
    private String id;
    private int type;
    private String name;
    private String settings;
    private String last_update = "01.01.1970 00:00:00";
    private Double last_lat = 0.0;
    private Double last_lng = 0.0;
    private int state = STATE_UNLOCKED;

    public Device() {}

    public Device(String device_id, String name, int type, String last_update, Double last_lat, Double last_lng, String settings, int state) {
        this.id = device_id;
        this.type = type;
        this.name = name;
        this.settings = settings;
        this.last_update = last_update;
        this.last_lat = last_lat;
        this.last_lng = last_lng;
        this.state = state;
    }

    public String getDeviceID() {
        return id;
    }

    public void setType(int type) {
        this.type = type;
    }
    public int getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }

    public String getSettings() {
        return settings;
    }

    public String getDetails() {
        return "test";
    }

    public String getLastUpdate() {
        return last_update;
    }

    public Double getLat() {
        return last_lat;
    }
    public Double getLng() {
        return last_lng;
    }
    public LatLng getCoordinates() {
        return new LatLng(last_lat, last_lng);
    }
    public void setCoordinates(Double lat, Double lng) {
        this.last_lat = lat;
        this.last_lng = lng;
    }

    public int getState() {
        return state;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Device device = (Device) another;
            Date another_date = sdf.parse(device.getLastUpdate());
            Date current_date = sdf.parse(device.getLastUpdate());
            if(current_date.after(another_date)) return 1;
            return -1;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
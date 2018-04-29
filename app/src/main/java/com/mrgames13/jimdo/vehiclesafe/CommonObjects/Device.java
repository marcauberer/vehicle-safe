package com.mrgames13.jimdo.vehiclesafe.CommonObjects;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Device implements Comparable {

    //Konstanten
    public static final int TYPE_BIKE = 1;
    public static final int TYPE_MOTORCYCLE = 2;
    public static final int TYPE_CAR = 3;

    //Variablen als Objekte

    //Variablen
    private int id;
    private int type;
    private String name;
    private String description;
    private String settings;

    public Device() {}

    public Device(int device_id, String name, String description, int type, String settings) {
        this.id = device_id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.settings = settings;
    }

    public int getDeviceID() {
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

    public String getDescription() {
        return description;
    }

    @Override
    public int compareTo(@NonNull Object another) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            Device device = (Device) another;
            Date another_date = sdf.parse(device.getName());
            Date current_date = sdf.parse(getName());
            if(current_date.after(another_date)) return 1;
            return -1;
        } catch (Exception e) {}
        return 0;
    }
}
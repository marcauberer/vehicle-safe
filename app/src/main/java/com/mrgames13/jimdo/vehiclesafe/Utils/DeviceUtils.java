package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.content.Context;

import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeviceUtils {

    //Konstanten

    //Variablen als Objekte

    //Utils-Pakete
    private StorageUtils su;
    private ServerMessagingUtils smu;

    //Variablen

    public DeviceUtils(Context context) {
        this.su = new StorageUtils(context);
        this.smu = new ServerMessagingUtils(context);
    }

    public ArrayList<Device> loadDevices() {
        return su.getAllDevices();
    }

    public Device loadSingleDevice(int device_id) {
        return su.getDevice(device_id);
    }

    public ArrayList<Device> loadDevicesFromServer() {
        ArrayList<Device> devices = new ArrayList<>();
        try{
            String devices_str = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=getdevices");
            if(!devices_str.equals("no_devices")) {
                devices_str = devices_str.substring(1);
                List<String> list = Arrays.asList(devices_str.split(";"));
                su.clearDevices();
                for(String device : list) {
                    try{
                        //Auseinandernehmen
                        int index1 = device.indexOf("~");
                        int index2 = device.indexOf("~", index1 +1);
                        int index3 = device.indexOf("~", index2 +1);
                        int index4 = device.indexOf("~", index3 +1);
                        int index5 = device.indexOf("~", index4 +1);
                        int index6 = device.indexOf("~", index5 +1);
                        int index7 = device.indexOf("~", index6 +1);
                        int index8 = device.indexOf("~", index7 +1);
                        int index9 = device.indexOf("~", index8 +1);
                        //Daten des Gerätes auslesen
                        int device_id = Integer.parseInt(device.substring(0, index1));
                        String device_name = device.substring(index1 +1, index2);
                        String device_description = device.substring(index2 +1, index3);
                        int device_type = Integer.parseInt(device.substring(index3 +1, index4));
                        String settings = device.substring(index4 +1, index5);
                        //Letzten Broadcast auslesen
                        long timestamp = Long.parseLong(device.substring(index5 +1, index6));
                        int lock_mode = Integer.parseInt(device.substring(index6 +1, index7));
                        double last_lat = Double.parseDouble(device.substring(index7 +1, index8));
                        double last_lng = Double.parseDouble(device.substring(index8 +1, index9));
                        double last_alt = Double.parseDouble(device.substring(index9 +1));
                        Broadcast b = new Broadcast(device_id, timestamp, lock_mode, last_lat, last_lng, last_alt, 0, true, 0);
                        su.addBroadcastIfTimestampNotExists(b);

                        Device dev = new Device(device_id, device_name, device_description, device_type, settings);
                        devices.add(dev);
                        su.addDevice(dev);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                su.clearDevices();
            }
        } catch (Exception e) {}
        Collections.sort(devices);
        return devices;
    }
}
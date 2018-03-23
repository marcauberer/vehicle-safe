package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.content.Context;

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
                        String device_id = device.substring(0, index1);
                        String device_name = device.substring(index1 +1, index2);
                        int device_type = Integer.parseInt(device.substring(index2 +1, index3));
                        String last_update = device.substring(index3 +1, index4);
                        Double last_lat = Double.parseDouble(device.substring(index4 +1, index5));
                        Double last_lng = Double.parseDouble(device.substring(index5 +1, index6));
                        String settings = device.substring(index6 +1, index7);
                        int device_state = Integer.parseInt(device.substring(index7 +1));

                        Device dev = new Device(device_id, device_name, device_type, last_update, last_lat, last_lng, settings, device_state);
                        devices.add(dev);
                        su.addDevice(dev);
                    } catch (Exception e) {}
                }
            } else {
                su.clearDevices();
            }
        } catch (Exception e) {}
        Collections.sort(devices);
        return devices;
    }
}
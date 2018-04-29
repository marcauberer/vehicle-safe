package com.mrgames13.jimdo.vehiclesafe.FirebaseMessaging;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.App.TrackingActivity;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.Fragments.BigMapFragment;
import com.mrgames13.jimdo.vehiclesafe.Fragments.VehicleFragment;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.DeviceUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.NotificationUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;

public class FCMMessagingService extends FirebaseMessagingService {
    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private StorageUtils su;
    private NotificationUtils nu;
    private DeviceUtils du;

    //Variablen

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
        nu = new NotificationUtils(this);
        du = new DeviceUtils(this);
        su = new StorageUtils(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            //Command ermitteln
            String command = remoteMessage.getData().get("command");
            if (command.equals("send_broadcast")) {
                Log.i("VS", "Got broadcast");
                //Daten der Push-Nachricht abfragen
                int device_id = Integer.parseInt(remoteMessage.getData().get("device_id"));
                long time_stamp = Long.parseLong(remoteMessage.getData().get("time_stamp"));
                double latitude = Double.parseDouble(remoteMessage.getData().get("latitude"));
                double longitude = Double.parseDouble(remoteMessage.getData().get("longitude"));
                double altitude = Double.parseDouble(remoteMessage.getData().get("altitude"));
                int lock_mode = Integer.parseInt(remoteMessage.getData().get("lock_mode"));
                double speed = Double.parseDouble(remoteMessage.getData().get("speed"));
                boolean fix = Integer.parseInt(remoteMessage.getData().get("fix")) > 0;
                double fix_quality = Double.parseDouble(remoteMessage.getData().get("fix_quality"));

                //Broadcast speichern
                Broadcast broadcast = new Broadcast(device_id, time_stamp, lock_mode, latitude, longitude, altitude, speed, fix, fix_quality);
                su.addBroadcast(broadcast);

                //Zugehöriges Gerät laden
                Device device = du.loadSingleDevice(device_id);

                //MapActivity ggf. aktualisieren
                TrackingActivity.own_instance.refresh(broadcast);

                //VehicleFragment ggf. aktualisieren
                if(MainActivity.active_fragment instanceof VehicleFragment) ((VehicleFragment) MainActivity.active_fragment).refresh(broadcast);

                //BigMapFragment ggf. aktualisieren
                if(MainActivity.active_fragment instanceof BigMapFragment) ((BigMapFragment) MainActivity.active_fragment).refresh(broadcast);

                if(lock_mode == Broadcast.STATE_STOLEN) {
                    //Wenn das Gerät gestohlen wurde, Nachricht in der Statusleiste anzeigen
                    nu.displayNotification(device_id, res.getString(R.string.device_stolen), device.getName() + " " + res.getString(R.string.device_stolen_m), new Intent(this, MainActivity.class));
                }
            } else if (command.equals("announce_update")) {
                //Daten der Push-Nachricht abfragen
                String version = remoteMessage.getData().get("version");
                String message_text = remoteMessage.getData().get("message");
                Intent i = new Intent(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + this.getPackageName())));
                nu.displayNotification(res.getString(R.string.update_to_version) + " " + version, message_text, Constants.ID_ANNOUNCE_UPDATE, i, nu.PRIORITY_NORMAL, 0, new long[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Log.e("ChatLet", "onDeletedMessages");
    }
}
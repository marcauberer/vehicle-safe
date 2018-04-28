package com.mrgames13.jimdo.vehiclesafe.FirebaseMessaging;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.DeviceUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.NotificationUtils;

public class FCMMessagingService extends FirebaseMessagingService {
    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private NotificationUtils nu;
    private DeviceUtils du;

    //Variablen

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
        nu = new NotificationUtils(this);
        du = new DeviceUtils(this);
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
                String device_id = remoteMessage.getData().get("device_id");
                String time_stamp = remoteMessage.getData().get("time_stamp");
                String latitude = remoteMessage.getData().get("latitude");
                String longitude = remoteMessage.getData().get("longitude");
                String altitude = remoteMessage.getData().get("altitude");
                int lock_mode = Integer.parseInt(remoteMessage.getData().get("lock_mode"));
                String speed = remoteMessage.getData().get("speed");
                int fix = Integer.parseInt(remoteMessage.getData().get("fix"));
                String fix_quality = remoteMessage.getData().get("fix_quality");

                Device device = du.loadSingleDevice(device_id);

                nu.displayNotification(res.getString(R.string.device_stolen), device.getName() + " " + res.getString(R.string.device_stolen_m), new Intent(this, MainActivity.class));
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
package com.mrgames13.jimdo.vehiclesafe.FirebaseMessaging;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.NotificationUtils;

public class FCMMessagingService extends FirebaseMessagingService {
    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private NotificationUtils nu;

    //Variablen

    @Override
    public void onCreate() {
        super.onCreate();
        res = getResources();
        nu = new NotificationUtils(this);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            //Command ermitteln
            String command = remoteMessage.getData().get("command");
            if (command.equals("send_broadcast")) {
                //Daten der Push-Nachricht abfragen
                String from_id = remoteMessage.getData().get("from_id");
                String cmd = remoteMessage.getData().get("cmd");
                String longitude = remoteMessage.getData().get("longitude");
                String latitude = remoteMessage.getData().get("latitude");
                String lock_mode = remoteMessage.getData().get("lock_mode");
                String time_stamp = remoteMessage.getData().get("time_stamp");




                nu.displayNotification("Test", "Test", new Intent(this, MainActivity.class));
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
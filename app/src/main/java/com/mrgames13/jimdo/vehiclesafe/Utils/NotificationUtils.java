package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.mrgames13.jimdo.vehiclesafe.R;

import static android.content.Context.NOTIFICATION_SERVICE;

public class NotificationUtils {
    //Konstanten
        //IDs


        //Priorities
        public final int PRIORITY_MAX = 2;
        public final int PRIORITY_HIGH = 1;
        public final int PRIORITY_NORMAL = 0;
        public final int PRIORITY_LOW = -1;
        public final int PRIORITY_MIN = -2;
        //Vibrations
        public final int VIBRATION_SHORT = 300;
        public final int VIBRATION_LONG = 600;
        //Lights
        public final int LIGHT_SHORT = 500;
        public final int LIGHT_LONG = 1000;


    //Variablen als Objekte
    private Context context;
    private NotificationManager nm;
    private Resources res;

    //Variablen

    //Konstruktor
    public NotificationUtils(Context context) {
        this.context = context;
        res = context.getResources();
        nm = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
    }

    public void displayNotification(String title, String message, int id, Intent i, int priority, int light_lenght, long[] vibration) {
        //Notification aufbauen
        NotificationCompat.Builder n = buildNotification(title, message);
        n.setAutoCancel(true);
        if(i != null) {
            PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
            n.setContentIntent(pi);
        }
        //ID ermitteln
        if(id == 0) id = (int) ((Math.random()) * Integer.MAX_VALUE);
        if(priority == PRIORITY_HIGH) {
            n.setPriority(NotificationCompat.PRIORITY_HIGH);
            n.setLights(res.getColor(R.color.colorPrimary), light_lenght, light_lenght);
            n.setVibrate(vibration);
        } else if(priority == PRIORITY_NORMAL) {
            n.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        } else if(priority == PRIORITY_LOW) {
            n.setPriority(NotificationCompat.PRIORITY_LOW);
        }
        //n.setChannelId(Constants.NC_SYSTEM);
        nm.notify(id, n.build());
    }

    public void displayNotification(String title, String message, Intent i) {
        //Notification aufbauen
        NotificationCompat.Builder n = buildNotification(title, message);
        n.setAutoCancel(true);
        if(i != null) {
            PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
            n.setContentIntent(pi);
        }
        //ID ermitteln
        int id = (int) ((Math.random()) * Integer.MAX_VALUE);
        //n.setChannelId(Constants.NC_SYSTEM);
        nm.notify(id, n.build());
    }

    public void displayNotification(int id, String title, String message, Intent i) {
        //Notification aufbauen
        NotificationCompat.Builder n = buildNotification(title, message);
        n.setAutoCancel(true);
        if(i != null) {
            PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
            n.setContentIntent(pi);
        }
        //ID ermitteln
        //n.setChannelId(Constants.NC_SYSTEM);
        nm.notify(id, n.build());
    }

    public void displayProgressMessage(String title, String message, int id, int progress, Intent i, int priority) {
        if(priority == PRIORITY_MIN) priority = NotificationCompat.PRIORITY_MIN;
        if(priority == PRIORITY_LOW) priority = NotificationCompat.PRIORITY_LOW;
        if(priority == PRIORITY_NORMAL) priority = NotificationCompat.PRIORITY_DEFAULT;
        if(priority == PRIORITY_HIGH) priority = NotificationCompat.PRIORITY_HIGH;
        if(priority == PRIORITY_MAX) priority = NotificationCompat.PRIORITY_MAX;
        PendingIntent pi = PendingIntent.getActivity(context, 0, i, 0);
        Notification n = new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false)
                .setPriority(priority)
                .setOngoing(true)
                .setProgress(100, progress, false)
                .setContentIntent(pi)
                //.setChannelId(Constants.NC_SYSTEM)
                .build();
        nm.notify(id, n);
    }

    public void clearNotification(int id) {
        nm.cancel(id);
    }

    public void clearNotifications() {
        nm.cancelAll();
    }

    private NotificationCompat.Builder buildNotification(String title, String message) {
        return new NotificationCompat.Builder(context)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setColor(res.getColor(R.color.colorPrimary));
    }

    public void createNotificationChannel(String channel_id, String name, String description, int importance, boolean with_badges) {
        if(Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channel_id, name, importance);
            channel.setDescription(description);
            channel.setShowBadge(with_badges);
            nm.createNotificationChannel(channel);
        }
    }

    public void deleteNotificationChannel(String channel_id) {
        if(Build.VERSION.SDK_INT >= 26) nm.deleteNotificationChannel(channel_id);
    }
}
package com.mrgames13.jimdo.vehiclesafe.FirebaseMessaging;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;

public class FCMInstanceIdService extends FirebaseInstanceIdService {
    //Konstanten
    public static final String token_preference_key = "fcm_token";
    public static final String topic_all = "all";

    //Variablen als Objekte
    private StorageUtils su;

    //Variablen

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        //Token in den SharedPreferences speichern
        su = new StorageUtils(this);
        su.putString(token_preference_key, FirebaseInstanceId.getInstance().getToken());

        //Topic 'all' abonnieren
        FirebaseMessaging.getInstance().subscribeToTopic(topic_all);

        //Topic '<Acc_ID>' abonnieren
        String app_id = su.getString("AccID");
        if(!app_id.equals("")) FirebaseMessaging.getInstance().subscribeToTopic(app_id);

        //Stoppen
        stopSelf();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        //Topic '<Acc_ID>' abonnieren
        String app_id = su.getString("AccID");
        if(!app_id.equals("")) FirebaseMessaging.getInstance().subscribeToTopic(app_id);
        Log.i("ChatLet", "Subscribed to Topic '" + app_id + "'");
    }
}
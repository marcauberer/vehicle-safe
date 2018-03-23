package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mrgames13.jimdo.vehiclesafe.R;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Tools {

    //Konstanten

    //Variablen als Objekte

    //Variablen

    public Tools() {}

    public static String encodeWithMd5(final String s) {
        try {
            //Hash erstellen
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            //Hex-String erstellen
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2) h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {}
        return "";
    }

    public static boolean isPlayServiceAvailable(Activity activity) {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(activity);
        if(isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if(api.isUserResolvableError(isAvailable)) {
            Dialog d = api.getErrorDialog(activity, isAvailable, 0);
            d.show();
        } else {
            Toast.makeText(activity, activity.getResources().getString(R.string.play_services_error), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public static void makeAppRestart(Activity activity) {
        Intent i = activity.getBaseContext().getPackageManager().getLaunchIntentForPackage(activity.getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivity(i);
    }
}
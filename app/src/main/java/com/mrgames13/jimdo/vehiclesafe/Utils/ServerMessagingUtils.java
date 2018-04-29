package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.content.Context;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.mrgames13.jimdo.vehiclesafe.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.net.ssl.HttpsURLConnection;

public class ServerMessagingUtils {

    //Konstanten
    private final String SERVER_ADRESS = "https://mrgames-server.de/vehicle_safe/";
    private final String SERVER_MAIN_SCRIPT = SERVER_ADRESS + "ServerScript.php";

    //Variablen als Objekte
    private Context context;
    private Resources res;
    private ConnectivityManager cm;
    private WifiManager wifiManager;
    private Handler handler;
    private URL url;

    //Variablen

    public ServerMessagingUtils(Context context) {
        this.context = context;
        this.cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.res = context.getResources();
        //URL erstellen
        try { url = new URL(SERVER_MAIN_SCRIPT); } catch (MalformedURLException e) {}
    }

    public String sendRequest(View v, final String param) {
        if(isInternetAvailable()) {
            try {
                //Connection aufbauen
                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setFixedLengthStreamingMode(param.getBytes().length);
                connection.setDoOutput(true);
                connection.setDoInput(true);
                //Anfrage senden
                OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
                out.write(param);
                out.flush();
                out.close();
                //Antwort empfangen
                InputStream in = connection.getInputStream();
                String answer = getAnswerFromInputStream(in);
                //Connection schließen
                connection.disconnect();
                Log.i("VS", "Answer from Server: '" + answer.replace("<br>", "").trim() + "'");
                //Antwort zurückgeben
                return answer.replace("<br>", "").trim();
            } catch (IOException e) {
                return sendRequest(v, param);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if(v != null) checkConnection(v);
        }
        return "";
    }

    public long ping(String acc_id) {
        //Zeit berechnen
        long start = System.currentTimeMillis();
        try { sendRequest(null, "acc_id="+ URLEncoder.encode(acc_id, "UTF-8")+"&command=ping"); } catch (UnsupportedEncodingException e) {}
        long end = System.currentTimeMillis();
        return end - start;
    }

    private String getAnswerFromInputStream(InputStream in) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder sb = new StringBuilder();

        String currentLine;
        while((currentLine = reader.readLine()) != null) {
            sb.append(currentLine);
            sb.append("\n");
        }
        return sb.toString();
    }

    public void checkConnection(View view) {
        if(!isInternetAvailable()) {
            Snackbar.make(view, context.getResources().getString(R.string.internet_is_not_available), Snackbar.LENGTH_LONG)
                    .setAction(R.string.activate_wifi, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            wifiManager.setWifiEnabled(true);
                        }
                    })
                    .show();
        }
    }

    public boolean isInternetAvailable() {
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnectedOrConnecting();
    }
}
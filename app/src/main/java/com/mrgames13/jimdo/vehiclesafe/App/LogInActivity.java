package com.mrgames13.jimdo.vehiclesafe.App;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.SimpleTextWatcherUtils;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import java.net.URLEncoder;

public class LogInActivity extends AppCompatActivity {

    //Konstanten
    private final int REQ_REGISTER = 10001;

    //Variablen als Objekte
    private Resources res;
    private Toolbar toolbar;
    private StorageUtils su;
    private ServerMessagingUtils smu;

    //Komponenten
    private TextView et_id;
    private TextView et_password;
    private CheckBox cb_keep_logged_in;
    private Button btn_login;
    private TextView tv_forgot_password;
    private Button btn_register;
    private ProgressBar pb_login_in_progress;

    //Variablen
    private String result;
    private String android_version;
    private String app_version;
    public static boolean finish;
    private boolean keep_logged_in;
    private String acc_id;
    private String username;
    private String acc_state;
    private String password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Resourcen initialisieren
        res = getResources();

        //Toolbar initialisieren
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //StorageUtils initialisieren
        su = new StorageUtils(this);

        //ServerMessagingUtils initialisieren
        smu = new ServerMessagingUtils(this);

        //AndroidVersion herausfinden
        android_version = Build.VERSION.RELEASE;

        //AppVersion herausfinden
        try { app_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (PackageManager.NameNotFoundException e) {}

        //Komponenten initialisieren
        et_id = (EditText) findViewById(R.id.login_id);
        et_password = (EditText) findViewById(R.id.login_password);
        tv_forgot_password = findViewById(R.id.forgot_password);
        btn_login = findViewById(R.id.login_login);
        btn_register = findViewById(R.id.login_register);
        cb_keep_logged_in = findViewById(R.id.login_keep_logged_in);
        pb_login_in_progress = findViewById(R.id.login_in_progress);

        et_id.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable s) {
                btn_login.setEnabled(s.length() > 0 && et_password.getText().toString().length() > 0);
                cb_keep_logged_in.setEnabled(s.length() > 0 && et_password.getText().toString().length() > 0);
            }
        });
        et_password.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable s) {
                btn_login.setEnabled(s.length() > 0 && et_id.getText().length() > 0);
                cb_keep_logged_in.setEnabled(s.length() > 0 && et_id.getText().length() > 0);
            }
        });
        et_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                String acc_id = et_id.getText().toString().trim();
                password = Tools.encodeWithMd5(et_password.getText().toString());
                keep_logged_in = cb_keep_logged_in.isChecked();
                login(acc_id, password);
                return true;
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String acc_id = et_id.getText().toString().trim();
                password = Tools.encodeWithMd5(et_password.getText().toString());
                keep_logged_in = cb_keep_logged_in.isChecked();
                login(acc_id, password);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(LogInActivity.this, RegisterActivity.class), REQ_REGISTER);
            }
        });

        tv_forgot_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(LogInActivity.this, res.getString(R.string.reset_password_m), Toast.LENGTH_LONG).show();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(su.getString("SupportUrl", res.getString(R.string.url_support))));
                startActivity(i);
            }
        });

        smu.checkConnection(findViewById(R.id.container));
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Toolbar Text und Farbe setzen
        getSupportActionBar().setTitle(res.getString(R.string.title_activity_login));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQ_REGISTER && resultCode == RESULT_OK) {
            if(data.hasExtra("AutoLogin") && data.getBooleanExtra("AutoLogin", false)) {
                username = data.getStringExtra("Username");
                password = data.getStringExtra("Password");
                keep_logged_in = data.getBooleanExtra("KeepLogged", false);

                login(username, password);
            }
        }
    }

    private void login(final String username_entry, final String password_entry) {
        if(smu.isInternetAvailable()) {
            pb_login_in_progress.setVisibility(View.VISIBLE);
            //Komponenten unveränderbar machen
            enableComponents(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        result = smu.sendRequest(findViewById(R.id.container), "acc_id=" + URLEncoder.encode(username_entry, "UTF-8") + "&command=login&password=" + URLEncoder.encode(password_entry, "UTF-8") + "&androidversion=" + URLEncoder.encode(android_version, "UTF-8") + "&appversion=" + URLEncoder.encode(app_version, "UTF-8"));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(result.equals("account is not existing")) {
                                    Toast.makeText(LogInActivity.this, res.getString(R.string.account_does_not_exist), Toast.LENGTH_SHORT).show();
                                    pb_login_in_progress.setVisibility(View.GONE);
                                    //Komponenten sichtbar machen
                                    enableComponents(true);
                                } else if(result.equals("password wrong")) {
                                    Toast.makeText(LogInActivity.this, res.getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                                    pb_login_in_progress.setVisibility(View.GONE);
                                    //Komponenten sichtbar machen
                                    enableComponents(true);
                                } else {
                                    try{
                                        //Result String auseinandernehmen + verarbeiten
                                        int index1 = result.indexOf("~");
                                        int index2 = result.indexOf("~", index1 +1);
                                        acc_id = result.substring(0, index1);
                                        username = result.substring(index1 +1, index2);
                                        acc_state = result.substring(index2 +1);
                                        //Altes FirebaseTopic deabbonnieren
                                        if(!su.getString("AccID").equals("")) FirebaseMessaging.getInstance().unsubscribeFromTopic(su.getString("AccID"));
                                        //Accountstates abfragen
                                        if(acc_state.startsWith("1")) { //Account funktioniert normal
                                            Log.i("VS", "Normal account state");
                                            saveDataAndLogin();
                                        } else if(acc_state.startsWith("2")) { //Account wurde gesperrt
                                            String output = acc_state.length() == 1 ? res.getString(R.string.account_blocked_m) : acc_state.substring(1);
                                            AlertDialog d = new AlertDialog.Builder(LogInActivity.this)
                                                    .setTitle(res.getString(R.string.account_blocked_t))
                                                    .setMessage(output)
                                                    .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int i) {
                                                            dialog.dismiss();
                                                            finish();
                                                        }
                                                    })
                                                    .create();
                                            d.show();
                                        }
                                    } catch(Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(LogInActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                        enableComponents(true);
                                        pb_login_in_progress.setVisibility(View.GONE);
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LogInActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                enableComponents(true);
                                pb_login_in_progress.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            }).start();
        } else {
            smu.checkConnection(findViewById(R.id.container));
        }
    }

    private void enableComponents(boolean e) {
        findViewById(R.id.login_id).setEnabled(e);
        findViewById(R.id.login_password).setEnabled(e);
        findViewById(R.id.login_login).setEnabled(e);
        findViewById(R.id.login_register).setEnabled(e);
        findViewById(R.id.login_keep_logged_in).setEnabled(e);
        findViewById(R.id.forgot_password).setEnabled(e);
    }

    private void saveDataAndLogin() {
        //Eingeloggt Bleiben speichern
        su.putBoolean("KeepLoggedIn", keep_logged_in);
        //Accountdaten in die SharedPreferences eintragen
        su.putString("AccID", acc_id);
        su.putString("Username", username);
        Log.d("VS", "Password: " + password);
        su.putString("Password", password);
        su.putString("AccState", acc_state);
        //FirebaseTopic abbonnieren
        FirebaseMessaging.getInstance().subscribeToTopic(acc_id);
        //Toast ausgeben
        Toast.makeText(LogInActivity.this, res.getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
        finish();
        overridePendingTransition(R.anim.in_login, R.anim.out_login);
    }
}

package com.mrgames13.jimdo.vehiclesafe.App;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mrgames13.jimdo.vehiclesafe.HelpClasses.SimpleTextWatcherUtils;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ColorUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    //Konstanten

    //Variablen als Objekte
    private Toolbar toolbar;
    private Resources res;
    private EditText et_username;
    private EditText et_password;
    private EditText et_repeat_password;
    private ProgressBar pb_register_in_progress;
    private CheckBox cb_auto_login;
    private CheckBox cb_keep_logged_in;
    private Button btn_register;
    private StorageUtils su;
    private ServerMessagingUtils smu;
    private ColorUtils clru;

    //Variablen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Toolber initialisieren
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Resourcen initialisieren
        res = getResources();

        //StorageUtils initialisieren
        su = new StorageUtils(this);

        //ServerMessagingUtils initialisieren
        smu = new ServerMessagingUtils(this);

        //ColorUtils initialisieren
        clru = new ColorUtils(res);

        //Komponenten initialisieren
        et_username = (EditText) findViewById(R.id.register_username);
        et_password = (EditText) findViewById(R.id.register_password);
        et_repeat_password = (EditText) findViewById(R.id.register_repeat_password);
        pb_register_in_progress = (ProgressBar) findViewById(R.id.register_in_progress);
        cb_auto_login = (CheckBox) findViewById(R.id.register_auto_login);
        cb_keep_logged_in = (CheckBox) findViewById(R.id.register_keep_logged_in);
        btn_register =(Button) findViewById(R.id.register_register);

        et_username.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable editable) {
                btn_register.setEnabled(et_username.getText().toString().trim().length() > 0 && et_password.getText().toString().trim().length() > 0 && et_repeat_password.getText().toString().trim().length() > 0);
            }
        });
        et_password.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable editable) {
                btn_register.setEnabled(et_username.getText().toString().trim().length() > 0 && et_password.getText().toString().trim().length() > 0 && et_repeat_password.getText().toString().trim().length() > 0);
            }
        });
        et_repeat_password.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable editable) {
                btn_register.setEnabled(et_username.getText().toString().trim().length() > 0 && et_password.getText().toString().trim().length() > 0 && et_repeat_password.getText().toString().trim().length() > 0);
            }
        });

        cb_auto_login.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                cb_keep_logged_in.setEnabled(b);
            }
        });
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Infos aus den Komponenten filtern
                String username = et_username.getText().toString().trim();
                String password = et_password.getText().toString();
                String rp_password = et_repeat_password.getText().toString();
                //Übereinstimmung von Passwörtern der Daten überprüfen
                if(rp_password.equals(password)) {
                    //Registration initiieren
                    register(username, Tools.encodeWithMd5(password), cb_auto_login.isChecked(), cb_keep_logged_in.isChecked());
                } else {
                    Toast.makeText(RegisterActivity.this, res.getString(R.string.passwords_does_not_match), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)  finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        //Toolbar Text und Farbe setzen
        getSupportActionBar().setTitle(res.getString(R.string.title_activity_register));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void register(final String username, final String password, final boolean auto_login, final boolean keep_logged_in) {
        if(smu.isInternetAvailable()) {
            pb_register_in_progress.setVisibility(View.VISIBLE);
            enableComponents(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        //Account anlegen
                        final String result = smu.sendRequest(findViewById(R.id.container), "acc_id=" + URLEncoder.encode(username, "UTF-8") + "&command=newaccount&username=" + URLEncoder.encode(username, "UTF-8") + "&password=" + URLEncoder.encode(password, "UTF-8"));
                        //Toast und AlertDialog anzeigen
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(result.length() > 0) {
                                    Toast.makeText(RegisterActivity.this, res.getString(R.string.account_successfully_created), Toast.LENGTH_SHORT).show();
                                    Intent i = new Intent(RegisterActivity.this, LogInActivity.class);
                                    i.putExtra("AutoLogin", auto_login);
                                    i.putExtra("Username", username);
                                    i.putExtra("Password", password);
                                    i.putExtra("KeepLogged", keep_logged_in);
                                    setResult(RESULT_OK, i);
                                    finish();
                                } else {
                                    enableComponents(true);
                                    Toast.makeText(RegisterActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } catch(Exception e) {
                        enableComponents(true);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            enableComponents(true);
                            pb_register_in_progress.setVisibility(View.GONE);
                        }
                    });
                }
            }).start();
        } else {
            smu.checkConnection(findViewById(R.id.container));
        }
    }

    private void enableComponents(boolean e) {
        et_username.setEnabled(e);
        et_password.setEnabled(e);
        et_repeat_password.setEnabled(e);
        btn_register.setEnabled(e);
    }
}
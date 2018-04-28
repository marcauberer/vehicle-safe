package com.mrgames13.jimdo.vehiclesafe.App;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import net.margaritov.preference.colorpicker.ColorPickerDialog;

import java.net.URLEncoder;

public class EditDeviceActivity extends AppCompatActivity {

    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private Toolbar toolbar;
    private ColorPickerDialog color_picker;
    private View reveal_view;
    private View reveal_background_view;
    private EditText new_name;
    private EditText new_description;
    private Button choose_color;
    private ImageView iv_color;

    //UtilsPakete
    private StorageUtils su;
    private ServerMessagingUtils smu;

    //Variablen
    private boolean pressedOnce;
    private int current_color;
    private String device_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_device);

        //Toolbar initialisieren
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Resourcen initialisieren
        res = getResources();

        //StorageUtils initialisieren
        su = new StorageUtils(this);

        //ServerMessagingUtils initialisieren
        smu = new ServerMessagingUtils(this);

        //RevealView initialisieren
        reveal_view = findViewById(R.id.reveal);
        reveal_background_view = findViewById(R.id.reveal_background);

        //Komponenten initialisieren
        new_name = findViewById(R.id.et_name);
        new_description = findViewById(R.id.et_description);
        choose_color = findViewById(R.id.choose_color);
        iv_color = findViewById(R.id.color);
        iv_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickColor();
            }
        });
        choose_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickColor();
            }
        });

        //Variablen initialisieren
        current_color = res.getColor(R.color.colorPrimary);

        //Intent-Extras abfragen
        if(getIntent().hasExtra("DeviceID")) device_id = getIntent().getStringExtra("DeviceID");
        if(getIntent().hasExtra("Name")) new_name.setText(getIntent().getStringExtra("Name"));
        if(getIntent().hasExtra("Description")) new_description.setText(getIntent().getStringExtra("Description"));
        if(getIntent().hasExtra("Color")) {
            current_color = getIntent().getIntExtra("Color", res.getColor(R.color.colorPrimary));
            iv_color.setColorFilter(current_color, PorterDuff.Mode.SRC);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setTitle(res.getString(R.string.title_activity_add_device));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_device, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onKeyDown(KeyEvent.KEYCODE_BACK, null);
        } else if(id == R.id.action_done) {
            //Änderungen speichern
            AlertDialog d = new AlertDialog.Builder(this)
                    .setCancelable(true)
                    .setTitle(res.getString(R.string.save_settings_t))
                    .setMessage(res.getString(R.string.save_settings_m))
                    .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(res.getString(R.string.save_settings_t), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if(su.getString(device_id + "_code", "no_code").equals("no_code")) {
                                final EditText input = new EditText(EditDeviceActivity.this);
                                input.setFilters(new InputFilter[] {new InputFilter.LengthFilter(8)});
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                                input.setLayoutParams(lp);

                                AlertDialog d = new AlertDialog.Builder(EditDeviceActivity.this)
                                        .setCancelable(true)
                                        .setTitle(res.getString(R.string.device_code_))
                                        .setView(input)
                                        .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                saveSettingsToServer(Tools.encodeWithMd5(input.getText().toString().trim()));
                                            }
                                        })
                                        .setNegativeButton(res.getString(R.string.cancel), null)
                                        .create();
                                d.show();
                            } else {
                                saveSettingsToServer(su.getString(device_id + "_code"));
                            }
                        }
                    })
                    .create();
            d.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickColor() {
        color_picker = new ColorPickerDialog(EditDeviceActivity.this, current_color);
        color_picker.setAlphaSliderVisible(false);
        color_picker.setHexValueEnabled(true);
        color_picker.setTitle(res.getString(R.string.choose_color));
        color_picker.setOnColorChangedListener(new ColorPickerDialog.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                current_color = color;
                animateAppAndStatusBar(color);
                iv_color.setColorFilter(color, PorterDuff.Mode.SRC);
            }
        });
        color_picker.show();
    }

    private void saveSettingsToServer(final String code) {
        final ProgressDialog pd = new ProgressDialog(EditDeviceActivity.this);
        pd.setMessage(res.getString(R.string.please_wait_));
        pd.setCancelable(false);
        pd.show();
        //Einstellungs-String generieren
        final String settings = "0";
        //Einstellungen auf dem Server speichern
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    final String result = smu.sendRequest(null, "acc_id=" + su.getString("AccID") + "&command=savedevicesettings&device_id=" + URLEncoder.encode(String.valueOf(device_id), "UTF-8") + "&device_code=" + URLEncoder.encode(code, "UTF-8") + "&device_name=" + URLEncoder.encode(new_name.getText().toString().trim(), "UTF-8") + "&device_description=" + URLEncoder.encode(new_description.getText().toString().trim(), "UTF-8") + "&device_settings=" + URLEncoder.encode(settings, "UTF-8"));
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(result.equals(Constants.ACTION_SUCCESSFUL_RESULT)) {
                                su.putString(device_id + "_code", code);
                                pd.dismiss();
                                su.putInt(device_id + "_color", current_color);
                                finish();
                            } else {
                                su.putString(device_id + "_code", "no_code");
                                pd.dismiss();
                                Toast.makeText(EditDeviceActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (Exception e) {
                    pd.dismiss();
                    Toast.makeText(EditDeviceActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!pressedOnce) {
                pressedOnce = true;
                Toast.makeText(EditDeviceActivity.this, R.string.tap_again_to_dismiss, Toast.LENGTH_SHORT).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pressedOnce = false;
                    }
                }, 2500);
            } else {
                pressedOnce = false;
                onBackPressed();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void animateAppAndStatusBar(final int toColor) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            Animator animator = ViewAnimationUtils.createCircularReveal(reveal_view, toolbar.getWidth() / 2, toolbar.getHeight() / 2, 0, toolbar.getWidth() / 2 + 50);
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    reveal_view.setBackgroundColor(toColor);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    reveal_background_view.setBackgroundColor(toColor);
                }
            });

            animator.setDuration(480);
            animator.start();
            reveal_view.setVisibility(View.VISIBLE);
        } else {
            reveal_view.setBackgroundColor(toColor);
            reveal_background_view.setBackgroundColor(toColor);
        }
    }
}
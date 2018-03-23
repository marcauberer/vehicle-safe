package com.mrgames13.jimdo.vehiclesafe.App;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.SimpleTextWatcherUtils;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import java.net.URLEncoder;

public class AddDeviceActivity extends AppCompatActivity {

    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private Toolbar toolbar;

    //UtilsPakete
    private StorageUtils su;
    private ServerMessagingUtils smu;

    //Variablen

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        //Toolbar initialisieren
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Resourcen initialisieren
        res = getResources();

        //StorageUtils initialisieren
        su = new StorageUtils(this);

        //ServerMessagingUtils initialisieren
        smu = new ServerMessagingUtils(this);

        //Komponenten initialisieren
        final Button add_device = (Button) findViewById(R.id.add_device);
        final EditText device_id = (EditText) findViewById(R.id.device_id);
        final ImageView device_id_info = (ImageView) findViewById(R.id.device_id_info);
        final EditText device_code = (EditText) findViewById(R.id.device_code);
        final ImageView device_code_info = (ImageView) findViewById(R.id.device_code_info);

        device_id.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable editable) {
                if(device_id.getText().length() == Constants.DEVICE_ID_LENGTH) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(device_id.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    device_code.requestFocus();
                    //Ist das Gerät auf dem Server registriert?
                    if(smu.isInternetAvailable()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    final String result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=isdeviceexisting&device_id=" + URLEncoder.encode(device_id.getText().toString(), "UTF-8"));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(result.equals(Constants.ACTION_SUCCESSFUL_RESULT)) {
                                                device_id_info.setImageResource(R.drawable.check_white);
                                            } else {
                                                device_id_info.setImageResource(R.drawable.clear_white);
                                            }
                                        }
                                    });
                                } catch (Exception e) {}
                            }
                        }).start();
                    }
                } else {
                    device_id_info.setImageResource(R.drawable.info_outline_black);
                }
                add_device.setEnabled(device_id.getText().toString().length() == Constants.DEVICE_ID_LENGTH && device_code.getText().toString().length() == Constants.DEVICE_CODE_LENGTH);
            }
        });

        device_code.addTextChangedListener(new SimpleTextWatcherUtils() {
            @Override
            public void afterTextChanged(Editable editable) {
                if(device_code.getText().length() == Constants.DEVICE_CODE_LENGTH) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(device_id.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
                    //Ist der Code für das Gerät richtig?
                    if(smu.isInternetAvailable()) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    final String result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=devicecodevalidation&device_id=" + URLEncoder.encode(device_id.getText().toString(), "UTF-8") + "&device_code=" + URLEncoder.encode(Tools.encodeWithMd5(device_code.getText().toString()), "UTF-8"));
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(result.equals(Constants.ACTION_SUCCESSFUL_RESULT)) {
                                                device_code_info.setImageResource(R.drawable.check_white);
                                            } else {
                                                device_code_info.setImageResource(R.drawable.clear_white);
                                            }
                                        }
                                    });
                                } catch (Exception e) {}
                            }
                        }).start();
                    }
                } else {
                    device_code_info.setImageResource(R.drawable.info_outline_black);
                }
                add_device.setEnabled(device_id.getText().toString().length() == Constants.DEVICE_ID_LENGTH && device_code.getText().toString().length() == Constants.DEVICE_CODE_LENGTH);
            }
        });

        device_id_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog d = new AlertDialog.Builder(AddDeviceActivity.this)
                        .setCancelable(true)
                        .setMessage(res.getString(R.string.device_id_info))
                        .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
                d.show();
            }
        });

        device_code_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog d = new AlertDialog.Builder(AddDeviceActivity.this)
                        .setCancelable(true)
                        .setMessage(res.getString(R.string.device_code_info))
                        .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(res.getString(R.string.reset_code), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(Intent.ACTION_VIEW);
                                i.setData(Uri.parse(su.getString("SupportUrl", res.getString(R.string.url_support))));
                                startActivity(i);
                                Toast.makeText(AddDeviceActivity.this, res.getString(R.string.reset_code_m), Toast.LENGTH_LONG).show();
                            }
                        })
                        .create();
                d.show();
            }
        });

        add_device.setEnabled(false);
        add_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(smu.isInternetAvailable()) {
                    AlertDialog d = new AlertDialog.Builder(AddDeviceActivity.this)
                            .setCancelable(true)
                            .setTitle(res.getString(R.string.title_activity_add_device))
                            .setMessage(res.getString(R.string.add_device_info))
                            .setPositiveButton(res.getString(R.string.title_activity_add_device), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    //Gerät hinzufügen
                                    final ProgressDialog pd = new ProgressDialog(AddDeviceActivity.this);
                                    pd.setMessage(res.getString(R.string.please_wait_));
                                    pd.setCancelable(false);
                                    pd.show();
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try{
                                                final String result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=adddevice&device_id=" + URLEncoder.encode(device_id.getText().toString(), "UTF-8") + "&device_code=" + URLEncoder.encode(Tools.encodeWithMd5(device_code.getText().toString()), "UTF-8"));
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if(result.equals("already linked")) {
                                                            pd.dismiss();
                                                            Toast.makeText(AddDeviceActivity.this, res.getString(R.string.already_linked), Toast.LENGTH_SHORT).show();
                                                        } else if(result.equals(Constants.ACTION_SUCCESSFUL_RESULT)) {
                                                            pd.dismiss();
                                                            su.putString(device_id.getText().toString() + "_code", Tools.encodeWithMd5(device_code.getText().toString()));
                                                            Toast.makeText(AddDeviceActivity.this, res.getString(R.string.successfully_linked), Toast.LENGTH_SHORT).show();
                                                            finish();
                                                        } else {
                                                            pd.dismiss();
                                                            Toast.makeText(AddDeviceActivity.this, res.getString(R.string.id_or_code_wrong), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                            } catch (Exception e) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        pd.dismiss();
                                                        Toast.makeText(AddDeviceActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }
                                    }).start();
                                }
                            })
                            .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
                    d.show();
                } else {
                    smu.checkConnection(findViewById(R.id.container));
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getSupportActionBar().setTitle(res.getString(R.string.title_activity_add_device));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }
}

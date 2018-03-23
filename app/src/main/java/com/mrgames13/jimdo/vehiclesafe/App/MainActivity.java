package com.mrgames13.jimdo.vehiclesafe.App;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.Fragments.DevicesFragment;
import com.mrgames13.jimdo.vehiclesafe.Fragments.BigMapFragment;
import com.mrgames13.jimdo.vehiclesafe.Fragments.VehicleFragment;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.DeviceUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.NotificationUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;

import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    //Konstanten
    private final int REQ_LOCATION_PERMISSION = 10001;
    private final int REQ_LOCATION_ENABLED = 10002;

    //Variablen als Objekte
    private Resources res;
    private Toolbar toolbar;
    private DrawerLayout drawer_layout_gesamt;
    private ActionBarDrawerToggle drawer_toggle;
    private NavigationView navView;
    private ViewGroup container;
    private FragmentManager fragmentManager;
    public ArrayList<Device> devices;
    public static MainActivity own_instance;
    public Device selected_device;
    private TextView header_data;

    //Utils-Pakete
    public ServerMessagingUtils smu;
    private StorageUtils su;
    private NotificationUtils nu;
    private DeviceUtils du;

    //Variablen
    private String android_version;
    private String app_version;
    private int app_version_code;
    private boolean pressedOnce;
    private String result;
    private int selected_menu_item_id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Eigene Instanz initialisieren
        own_instance = this;

        if (getIntent().getBooleanExtra("EXIT", false)) {
            finish();
            return;
        }

        //Resourcen initialisieren
        res = getResources();

        //Toolbar initialisieren
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(res.getString(R.string.title_activity_main));
        setSupportActionBar(toolbar);

        //StorageUtils initialisieren
        su = new StorageUtils(this);

        //ServerMessagingUtils initialisieren
        smu = new ServerMessagingUtils(this);

        //NotificationUtils initialisieren
        nu = new NotificationUtils(this);

        //DeviceUtils intiailsieren
        du = new DeviceUtils(this);

        //FragmentManager initialisieren
        fragmentManager = getSupportFragmentManager();

        //AndroidVersion herausfinden
        android_version = Build.VERSION.RELEASE;

        //AppVersion herausfinden
        try { app_version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName; } catch (PackageManager.NameNotFoundException e1) {}
        try { app_version_code = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode; } catch (PackageManager.NameNotFoundException e1) {}

        //Komponenten initialisieren
        container = findViewById(R.id.container);
        //DrawerLayout initialisieren
        drawer_layout_gesamt = findViewById(R.id.drawer_layout_gesamt);
        drawer_toggle = new ActionBarDrawerToggle(MainActivity.this, drawer_layout_gesamt, 0, 0);
        drawer_layout_gesamt.setDrawerListener(drawer_toggle);
        navView = findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                drawer_layout_gesamt.closeDrawers();
                if(selected_menu_item_id != menuItem.getItemId()) {
                    if(menuItem.getItemId() == R.id.drawer_item_settings) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                            }
                        }, 350);
                    } else {
                        selected_menu_item_id = menuItem.getItemId();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if(selected_menu_item_id == R.id.drawer_item_device_manager) {
                                    menuItem.setChecked(true);
                                    toolbar.setTitle(res.getString(R.string.device_mgmt));
                                    launchDevicesFragment();
                                } else if(selected_menu_item_id == R.id.drawer_item_map) {
                                    menuItem.setChecked(true);
                                    toolbar.setTitle(res.getString(R.string.map));
                                    startTracking();
                                } else {
                                    menuItem.setChecked(true);
                                    su.putInt("startpage_device", selected_menu_item_id);
                                    for(Device d : devices) {
                                        if(selected_menu_item_id == Integer.parseInt(d.getDeviceID())) launchVehicleFragement(d);
                                    }
                                }
                                invalidateOptionsMenu();
                            }
                        }, 350);
                    }
                }
                return false;
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        drawer_toggle.syncState();
        View header_view = navView.getHeaderView(0);
        header_data = header_view.findViewById(R.id.header_data);

        //ServerInfo abrufen
        getServerInfo();

        //LogInActivity aufrufen, wenn nötig
        if (!su.getBoolean("KeepLoggedIn") && !getIntent().getBooleanExtra("LoggedIn", false)) {
            startActivity(new Intent(MainActivity.this, LogInActivity.class));
        } else {
            //Im Hintergrund Login durchführen
            login(su.getString("AccID"), su.getString("Password"), android_version, app_version);
            //FirebaseTopic abbonnieren
            FirebaseMessaging.getInstance().subscribeToTopic(su.getString("AccID"));
            //App-Initialisierung aufrufen
            initializeApp();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        devices = du.loadDevices();
        new Thread(new Runnable() {
            @Override
            public void run() {
                devices = du.loadDevicesFromServer();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateNavigationMenuDevices();
                    }
                });
            }
        }).start();
        updateNavigationMenuDevices();

        //Startseite ansteuern
        if(selected_menu_item_id == -1) {
            int startpage = Integer.parseInt(su.getString("startpage", "0"));
            if(startpage == 0) {
                selected_menu_item_id = R.id.drawer_item_device_manager;
                toolbar.setTitle(res.getString(R.string.device_mgmt));
                launchDevicesFragment();
            } else if(startpage == 1) {
                selected_menu_item_id = R.id.drawer_item_map;
                toolbar.setTitle(res.getString(R.string.map));
                launchMapFragment();
            } else if(startpage == 2) {
                selected_menu_item_id = su.getInt("startpage_device", 0);
                for(Device d : devices) {
                    if(selected_menu_item_id == Integer.parseInt(d.getDeviceID())) launchVehicleFragement(d);
                }
            }
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawer_toggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawer_toggle.onConfigurationChanged(new Configuration());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(selected_menu_item_id == R.id.drawer_item_map) {
            getMenuInflater().inflate(R.menu.menu_main_map, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_center_device_pos) {
            BigMapFragment.own_instance.nextDeviceFocus();
        } else if(id == R.id.action_center_own_pos) {
            BigMapFragment.own_instance.goToOwnLocation();
        } else if (id == R.id.action_maptype_normal) {
            BigMapFragment.own_instance.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (id == R.id.action_maptype_terrain) {
            BigMapFragment.own_instance.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.action_maptype_satellite) {
            BigMapFragment.own_instance.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (id == R.id.action_maptype_hybrid) {
            BigMapFragment.own_instance.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else if(drawer_toggle.onOptionsItemSelected(item)) {
            return true;
        } else if(id == R.id.action_logout) {
            logout();
        } else if(id == R.id.action_exit) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!pressedOnce) {
                pressedOnce = true;
                Toast.makeText(MainActivity.this, R.string.tap_again_to_exit_app, Toast.LENGTH_SHORT).show();
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

    private void initializeApp() {
        //System-Channel initialisieren
        nu.createNotificationChannel(Constants.NC_SYSTEM, res.getString(R.string.nc_system), res.getString(R.string.nc_system_m), NotificationManager.IMPORTANCE_MAX, false);


    }

    private void getServerInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=getserverinfo");
                    //Result String auseinandernehmen + verarbeiten
                    int index1 = result.indexOf("~");
                    int index2 = result.indexOf("~", index1 +1);
                    int index3 = result.indexOf("~", index2 +1);
                    int index4 = result.indexOf("~", index3 +1);
                    int index5 = result.indexOf("~", index4 +1);
                    int index6 = result.indexOf("~", index5 +1);
                    int index7 = result.indexOf("~", index6 +1);
                    int index8 = result.indexOf("~", index7 +1);
                    final String client_name = result.substring(0, index1);
                    final int server_state = Integer.parseInt(result.substring(index1 +1, index2));
                    final int min_appversion = Integer.parseInt(result.substring(index2 +1, index3).replace(".", ""));
                    final int newest_appversion = Integer.parseInt(result.substring(index3 +1, index4).replace(".", ""));
                    final int min_admin_version = Integer.parseInt(result.substring(index4 +1, index5).replace(".", ""));
                    final int newest_admin_version = Integer.parseInt(result.substring(index5 +1, index6).replace(".", ""));
                    final String support_url = result.substring(index6 +1, index7);
                    final String owners = result.substring(index7 +1, index8);
                    final String user_msg = result.substring(index8 +1);
                    //Parameter abspeichern
                    su.putString("SupportUrl", support_url);
                    su.putInt("ServerState", server_state);
                    su.putInt("MinAppVersion", min_appversion);
                    su.putInt("NewestAppVersion", newest_appversion);
                    su.putString("UserMsg", user_msg);
                    //ServerInfo verarbeiten
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            parseServerInfo(client_name, server_state, min_appversion, newest_appversion, support_url, owners, user_msg);
                        }
                    });
                } catch (Exception e) {}
            }
        }).start();
    }

    private void parseServerInfo(String client_name, int server_state, int min_app_version, int newest_app_version, String support_url, String owners, String user_msg) {
        //ServerState verarbeiten
        if(server_state == 2) {
            AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setTitle(res.getString(R.string.offline_t))
                    .setMessage(res.getString(R.string.offline_m))
                    .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .create();
            d.show();
        } else if(server_state == 3) {
            AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setTitle(res.getString(R.string.waiting_t))
                    .setMessage(res.getString(R.string.waiting_m))
                    .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .create();
            d.show();
        } else if(server_state == 4) {
            AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                    .setCancelable(false)
                    .setTitle(res.getString(R.string.support_end_t))
                    .setMessage(res.getString(R.string.support_end_m))
                    .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    })
                    .create();
            d.show();
        } else {
            //AppVersion überprüfen
            if(app_version_code < min_app_version) {
                AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                        .setCancelable(false)
                        .setTitle(res.getString(R.string.update_necessary_t))
                        .setMessage(res.getString(R.string.update_necessary_m))
                        .setPositiveButton(res.getString(R.string.download_update), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                }
                                finish();
                            }
                        })
                        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .create();
                d.show();
            } else if(app_version_code < newest_app_version) {
                Snackbar.make(findViewById(R.id.container), res.getString(R.string.update_available), Snackbar.LENGTH_SHORT)
                        .setAction(res.getString(R.string.download), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));
                                }
                            }
                        })
                        .show();
            }
        }
    }

    private void login(final String acc_id, final String password, final String android_version, final String app_version) {
        //Internetverbindung überprüfen (Wenn ja, einloggen ; Wenn nein Fehlermeldung anzeigen)
        if(smu.isInternetAvailable()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        result = smu.sendRequest(findViewById(R.id.container), "acc_id=" + URLEncoder.encode(acc_id, "UTF-8") + "&command=login&password=" + URLEncoder.encode(password, "UTF-8") + "&androidversion=" + URLEncoder.encode(android_version, "UTF-8") + "&appversion=" + URLEncoder.encode(app_version, "UTF-8"));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(result.equals("account is not existing")) {
                                    startActivity(new Intent(MainActivity.this, LogInActivity.class));
                                } else if(result.equals("password wrong")) {
                                    startActivity(new Intent(MainActivity.this, LogInActivity.class));
                                } else {
                                    try{
                                        //Result String auseinandernehmen + verarbeiten
                                        int index1 = result.indexOf("~");
                                        int index2 = result.indexOf("~", index1 +1);
                                        String username = result.substring(index1 +1, index2);
                                        String acc_state = result.substring(index2 +1);
                                        //Accountstates abfragen
                                        if(acc_state.startsWith("1")) { //Account funktioniert normal
                                            //Senden und Empfangen von Nachrichten aktivieren
                                            su.putBoolean("CanSendMessages", true);
                                            su.putBoolean("CanReceiveMessages", true);
                                            //Accountdaten in die SharedPreferences eintragen
                                            su.putString("Username", username);
                                            su.putString("Password", password);
                                            su.putString("AccState", acc_state);
                                            //Nutzername im NavigationDrawer eintragen
                                            header_data.setText(res.getString(R.string.logged_in_as) + " " + username);
                                            //Aktuelles Datum ermitteln und in die Prefs eintragen
                                            Date date = new Date(System.currentTimeMillis());
                                            DateFormat formatierer = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.GERMANY);
                                            su.putString("LastConnected", formatierer.format(date));
                                            //FirebaseTopic abbonnieren
                                            FirebaseMessaging.getInstance().subscribeToTopic(su.getString("AccID"));
                                        } else if(acc_state.startsWith("2")) { //Account wurde gesperrt
                                            //Senden und Empfangen von Nachrichten deaktivieren
                                            su.putBoolean("CanSendMessages", false);
                                            su.putBoolean("CanReceiveMessages", false);
                                            //Accountdaten in die SharedPreferences eintragen
                                            su.putString("Username", username);
                                            su.putString("Password", password);
                                            su.putString("AccState", acc_state);
                                            //Dialog anzeigen
                                            String output = acc_state.length() == 1 ? res.getString(R.string.account_blocked_m) : acc_state.substring(1);
                                            AlertDialog d = new AlertDialog.Builder(MainActivity.this)
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
                                            //FirebaseTopic abbonnieren
                                            FirebaseMessaging.getInstance().subscribeToTopic(su.getString("AccID"));
                                        }
                                    } catch(Exception e) {
                                        Toast.makeText(MainActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        });
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }).start();
        } else {
            if(su.getString("AccState").equals("2")) {
                AlertDialog d = new AlertDialog.Builder(MainActivity.this)
                        .setTitle(res.getString(R.string.account_blocked_t))
                        .setMessage(res.getString(R.string.account_blocked_m))
                        .setPositiveButton(res.getString(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .create();
                d.show();
            } else if(su.getString("AccState").equals("3")) {
                Toast.makeText(MainActivity.this, res.getString(R.string.account_blocked_receiving), Toast.LENGTH_LONG).show();
            } else if(su.getString("AccState").equals("4")) {
                Toast.makeText(MainActivity.this, res.getString(R.string.account_blocked_sending), Toast.LENGTH_LONG).show();
            } else if(su.getString("AccState").equals("5")) {
                Toast.makeText(MainActivity.this, res.getString(R.string.account_blocked_both), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQ_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
            if(manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                startActivity(new Intent(this, TrackingActivity.class));
            } else {
                android.app.AlertDialog d = new android.app.AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle(res.getString(R.string.gps_not_enabled_t))
                        .setMessage(res.getString(R.string.gps_not_enabled_m))
                        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, res.getString(R.string.please_enable_gps), Toast.LENGTH_LONG).show();
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQ_LOCATION_ENABLED);
                            }
                        })
                        .create();
                d.show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_LOCATION_ENABLED) startTracking();
    }

    private void logout() {
        su.putBoolean("KeepLoggedIn", false);
        //FirebaseTopic deabbonnieren
        FirebaseMessaging.getInstance().unsubscribeFromTopic(su.getString("AccID"));
        //LogIn-Activity starten
        startActivity(new Intent(MainActivity.this, LogInActivity.class));
        overridePendingTransition(R.anim.in_login, R.anim.out_logout);
    }

    private void launchDevicesFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new DevicesFragment();
        fragmentTransaction.add(R.id.container, fragment);
        container.removeAllViews();
        fragmentTransaction.commit();
    }

    public void launchVehicleFragement(Device device) {
        selected_device = device;
        toolbar.setTitle(device.getName());
        navView.getMenu().findItem(Integer.parseInt(device.getDeviceID())).setChecked(true);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = new VehicleFragment();
        fragmentTransaction.add(R.id.container, fragment);
        container.removeAllViews();
        fragmentTransaction.commit();
    }

    public void launchMapFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BigMapFragment fragment = new BigMapFragment();
        fragmentTransaction.add(R.id.container, fragment);
        container.removeAllViews();
        fragmentTransaction.commit();
    }

    private void updateNavigationMenuDevices() {
        Menu m = navView.getMenu();
        for(Device d : devices) {
            m.removeItem(Integer.parseInt(d.getDeviceID()));
            int icon_id = R.drawable.directions_bike;
            if(d.getType() == Device.TYPE_MOTORCYCLE) {
                icon_id = R.drawable.directions_motorcycle;
            } else if(d.getType() == Device.TYPE_CAR) {
                icon_id = R.drawable.directions_car;
            }
            m.add(R.id.menu_vehicles, Integer.parseInt(d.getDeviceID()), Menu.NONE, d.getName()).setIcon(icon_id).setCheckable(true);
        }
        navView.setCheckedItem(selected_menu_item_id);
    }

    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if(manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                launchMapFragment();
            } else {
                android.app.AlertDialog d = new android.app.AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setTitle(res.getString(R.string.gps_not_enabled_t))
                        .setMessage(res.getString(R.string.gps_not_enabled_m))
                        .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                Toast.makeText(MainActivity.this, res.getString(R.string.please_enable_gps), Toast.LENGTH_LONG).show();
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQ_LOCATION_ENABLED);
                            }
                        })
                        .create();
                d.show();
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
        }
    }
}
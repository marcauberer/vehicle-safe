package com.mrgames13.jimdo.vehiclesafe.App;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import java.util.ArrayList;

public class TrackingActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    //Konstanten

    //Variablen als Objekte
    public static TrackingActivity own_instance;
    private Resources res;
    private Toolbar toolbar;
    private StorageUtils su;
    private ServerMessagingUtils smu;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Device device;
    private LocationRequest locationRequest;
    private LatLng ownPosition;
    private LatLng devicePosition;
    private Marker ownPositionMarker;
    private Marker devicePositionMarker;
    private Circle accuracy_circle;
    private LocationManager locManager;
    private Handler h;
    private Snackbar snackbar;
    private Broadcast last_broadcast;
    private ArrayList<Broadcast> broadcast_history;
    private ArrayList<Marker> history_markers = new ArrayList<>();

    //Variablen
    private boolean history_show;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Tools.isPlayServiceAvailable(this)) {
            setContentView(R.layout.activity_tracking);

            //Eigene Instanz initialisieren
            own_instance = this;

            //Device-Objekt übertragen
            if (MainActivity.own_instance.selected_device != null) device = MainActivity.own_instance.selected_device;

            //Resourcen initialisieren
            res = getResources();

            //StorageUtils initialisieren
            su = new StorageUtils(this);
            last_broadcast = su.getLastBroadcast(device.getDeviceID());
            if(last_broadcast == null) {
                Toast.makeText(this, res.getString(R.string.no_update_yet), Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            //ServerMessagingUtils initialisieren
            smu = new ServerMessagingUtils(this);

            //LocationManager initialisieren
            locManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

            //Handler initialisieren
            h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if(snackbar == null) {
                            snackbar = Snackbar.make(findViewById(R.id.container), res.getString(R.string.gps_not_available), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.activate_gps, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(TrackingActivity.this, res.getString(R.string.please_enable_gps), Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    });
                            snackbar.show();
                        }
                    } else if(!smu.isInternetAvailable()) {
                        if(snackbar == null) {
                            snackbar = Snackbar.make(findViewById(R.id.container), res.getString(R.string.internet_is_not_available), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.activate_wifi, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                            wifiManager.setWifiEnabled(true);
                                        }
                                    });
                            snackbar.show();
                        }
                    } else {
                        if(snackbar != null) {
                            snackbar.dismiss();
                            snackbar = null;
                        }
                    }
                    h.postDelayed(this, Constants.WIFI_GPS_CHECK_PERIOD);
                }
            }, Constants.WIFI_GPS_CHECK_PERIOD);

            //Map initialisieren
            initMap();
        } else {
            //TODO: Error-Seite anzeigen mit setContentView
        }

        //Toolbar initialisieren
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(res.getString(R.string.title_activity_tracking));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onStart() {
        super.onStart();
        if (googleApiClient != null && googleApiClient.isConnected())
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient != null && googleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (device.getType() == Device.TYPE_MOTORCYCLE) {
            getMenuInflater().inflate(R.menu.menu_tracking_motorcycle, menu);
        } else if (device.getType() == Device.TYPE_CAR) {
            getMenuInflater().inflate(R.menu.menu_tracking_car, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_tracking_bike, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        } else if (id == R.id.action_center_own_pos) {
            goToOwnLocation();
        } else if (id == R.id.action_center_device_pos) {
            goToDeviceLocation();
        } else if (id == R.id.action_show_hide_history) {
            history_show = !history_show;
            updateMode();
        } else if (id == R.id.action_maptype_normal) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        } else if (id == R.id.action_maptype_terrain) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        } else if (id == R.id.action_maptype_satellite) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else if (id == R.id.action_maptype_hybrid) {
            googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);
    }

    private void setDevicePosition(Broadcast last_broadcast) {
        devicePosition = new LatLng(last_broadcast.getLatitude(), last_broadcast.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 15));

        if(devicePositionMarker != null) {
            devicePositionMarker.setPosition(devicePosition);
            devicePositionMarker.setSnippet(last_broadcast.getTimeStampString());
        } else {
            MarkerOptions opts = new MarkerOptions()
                    .title(device.getName())
                    .snippet(last_broadcast.getTimeStampString())
                    .position(devicePosition);
            devicePositionMarker = googleMap.addMarker(opts);
        }
    }

    private void drawHistory(ArrayList<Broadcast> history) {
        PolylineOptions options = new PolylineOptions();
        options.color(Color.CYAN);
        options.width(5);
        options.visible(true);

        history_markers.clear();
        for(Broadcast b : history) {
            LatLng current_loc = new LatLng(b.getLatitude(), b.getLongitude());
            options.add(new LatLng(b.getLatitude(), b.getLongitude()));
            googleMap.addPolyline(options);

            MarkerOptions opts = new MarkerOptions()
                    .title(device.getName())
                    .snippet(b.getTimeStampString())
                    .icon(b.getTimeStampLong() == history.get(history.size() -1).getTimeStampLong() ? BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED) : BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                    .flat(true)
                    .position(current_loc);
            history_markers.add(googleMap.addMarker(opts));
        }
    }

    private void setOwnPosition(final Location loc) {
        ownPosition = new LatLng(loc.getLatitude(), loc.getLongitude());

        if(ownPositionMarker != null) {
            ownPositionMarker.setVisible(true);
            ownPositionMarker.setPosition(ownPosition);
            ownPositionMarker.setSnippet(String.valueOf(ownPosition.latitude) + ", " + String.valueOf(ownPosition.longitude));
        } else {
            MarkerOptions opts = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(res.getString(R.string.own_location))
                    .snippet(String.valueOf(ownPosition.latitude) + ", " + String.valueOf(ownPosition.longitude))
                    .position(ownPosition);
            ownPositionMarker = googleMap.addMarker(opts);
        }

        if (accuracy_circle != null) {
            accuracy_circle.setCenter(ownPosition);

            ValueAnimator anim = ValueAnimator.ofFloat(Float.valueOf(String.valueOf(accuracy_circle.getRadius())), loc.getAccuracy());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float val = (Float) valueAnimator.getAnimatedValue();
                    accuracy_circle.setRadius(val);
                }
            });
            anim.setDuration(1000);
            anim.start();
        } else {
            CircleOptions options = new CircleOptions()
                    .center(ownPosition)
                    .radius(loc.getAccuracy())
                    .fillColor(0x330000FF)
                    .strokeColor(0x550000FF)
                    .strokeWidth(2);
            accuracy_circle = googleMap.addCircle(options);
        }
    }

    private void goToOwnLocation() {
        if (ownPosition != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ownPosition, 15));
    }

    private void goToDeviceLocation() {
        if (devicePosition != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(devicePosition, 15));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(Integer.parseInt(su.getString("tracking_own_position_interval", "3")) * 1000);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        //TODO: Error-Meldung anzeigen
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //TODO: Error-Meldung anzeigen
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        //Standard-Kartentyp einstellen
        int maptype = Integer.parseInt(su.getString("maptype", "0"));
        if(maptype == 1) googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        if(maptype == 2) googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        if(maptype == 3) googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        //Info-Adapter setzen
        if(googleMap != null) {
            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    if(marker.getTitle().equals(res.getString(R.string.own_location))) return null;
                    View v = getLayoutInflater().inflate(R.layout.device_info_window, null);
                    ImageView device_icon = v.findViewById(R.id.device_icon);
                    TextView device_name = v.findViewById(R.id.device_name);
                    TextView device_coordinates = v.findViewById(R.id.device_coordinates);
                    TextView device_last_update = v.findViewById(R.id.device_last_update);

                    LatLng ll = marker.getPosition();
                    if(device.getType() == Device.TYPE_MOTORCYCLE) {
                        device_icon.setImageResource(R.drawable.directions_motorcycle);
                    } else if(device.getType() == Device.TYPE_CAR) {
                        device_icon.setImageResource(R.drawable.directions_car);
                    }
                    device_coordinates.setText(String.valueOf(ll.latitude) + ", " + String.valueOf(ll.longitude));
                    device_name.setText(device.getName());
                    device_last_update.setText(last_broadcast.getTimeStampString());

                    return v;
                }
            });
        }

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        setDevicePosition(last_broadcast);
    }

    @Override
    public void onLocationChanged(Location loc) {
        if(loc == null) {
            Toast.makeText(this, res.getString(R.string.error), Toast.LENGTH_SHORT).show();
        } else {
            setOwnPosition(loc);
        }
    }

    private void updateMode() {
        if(history_show) {
            ownPositionMarker.setVisible(false);
            broadcast_history = su.getBroadcastHistory(device.getDeviceID());
            drawHistory(broadcast_history);
        } else {
            setDevicePosition(last_broadcast);
        }
    }

    public void refresh(final Broadcast broadcast) {
        last_broadcast = broadcast;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setDevicePosition(broadcast);
            }
        });
    }
}

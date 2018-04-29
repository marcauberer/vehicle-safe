package com.mrgames13.jimdo.vehiclesafe.Fragments;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.Tools;

import java.util.ArrayList;

public class BigMapFragment extends Fragment implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, LocationListener {

    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private View content_view;
    private MapView mapView;
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Marker ownLocationMarker;
    private LatLng ownLocation;
    private ArrayList<Device> devices;
    private ArrayList<Marker> deviceLocationMarkers = new ArrayList<>();
    private ArrayList<Broadcast> last_broadcasts = new ArrayList<>();
    private Circle accuracy_circle;
    public static BigMapFragment own_instance;
    private LocationManager locManager;
    private Handler h;
    private Snackbar snackbar;

    //Utils-Pakete
    private StorageUtils su;
    private ServerMessagingUtils smu;

    //Variablen
    private int current_focus = -1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Eigene Instanz initialisieren
        own_instance = this;

        if (Tools.isPlayServiceAvailable(getActivity())) {
            //Resourcen initialisieren
            res = getResources();

            //StorageUtils initalisieren
            su = new StorageUtils(getActivity());

            //ServerMessagingUtils initialisieren
            smu = new ServerMessagingUtils(getActivity());

            //LocationManager initialisieren
            locManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

            //Handler initialisieren
            h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(!locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        if(snackbar == null) {
                            snackbar = Snackbar.make(content_view, res.getString(R.string.gps_not_available), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.activate_gps, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Toast.makeText(getActivity(), res.getString(R.string.please_enable_gps), Toast.LENGTH_LONG).show();
                                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                        }
                                    });
                            snackbar.show();
                        }
                    } else if(!smu.isInternetAvailable()) {
                        if(snackbar == null) {
                            snackbar = Snackbar.make(content_view, res.getString(R.string.internet_is_not_available), Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.activate_wifi, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            WifiManager wifiManager = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
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

            //Devices übernehmen
            devices = MainActivity.own_instance.devices;

            //Letzte Broadcasts laden
            last_broadcasts.clear();
            for(Device d : devices) last_broadcasts.add(su.getLastBroadcast(d.getDeviceID()));
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        content_view = inflater.inflate(R.layout.fragment_map, container, false);

        //Map initialisieren
        initMap();

        return content_view;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStart() {
        super.onStart();
        if(mapView != null) mapView.onStart();
        if(googleApiClient != null && googleApiClient.isConnected()) LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mapView != null) mapView.onStop();
        if(googleApiClient != null && googleApiClient.isConnected()) LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null) mapView.onDestroy();
    }

    private void initMap() {
        mapView = content_view.findViewById(R.id.map_view);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
    }

    private void setDeviceLocation(int index, double latitude, double longitude) {
        if(index == devices.size() -1) goToDeviceLocation(index);

        if(deviceLocationMarkers.size() > index && deviceLocationMarkers.get(index) != null) {
            deviceLocationMarkers.get(index).setPosition(new LatLng(latitude, longitude));
            deviceLocationMarkers.get(index).setSnippet(last_broadcasts.get(index).getTimeStampString());
        } else {
            MarkerOptions opts = new MarkerOptions()
                    .title(devices.get(index).getName())
                    .snippet(last_broadcasts.get(index).getTimeStampString())
                    .position(new LatLng(latitude, longitude));
            if(deviceLocationMarkers.size() < index +1) {
                Marker m = googleMap.addMarker(opts);
                m.setTag(index);
                deviceLocationMarkers.add(m);
            } else {
                deviceLocationMarkers.set(index, googleMap.addMarker(opts));
            }
        }
    }

    private void setOwnLocation(Location loc) {
        ownLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

        if(ownLocationMarker != null) {
            ownLocationMarker.setPosition(ownLocation);
            ownLocationMarker.setSnippet(String.valueOf(ownLocation.latitude) + ", " + String.valueOf(ownLocation.longitude));
        } else {
            MarkerOptions opts = new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    .title(res.getString(R.string.own_location))
                    .snippet(String.valueOf(ownLocation.latitude) + ", " + String.valueOf(ownLocation.longitude))
                    .position(ownLocation);
            ownLocationMarker = googleMap.addMarker(opts);
        }
        if(accuracy_circle != null) {
            accuracy_circle.setCenter(ownLocation);

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
                    .center(ownLocation)
                    .radius(loc.getAccuracy())
                    .fillColor(0x330000FF)
                    .strokeColor(0x550000FF)
                    .strokeWidth(2);
            accuracy_circle = googleMap.addCircle(options);
        }
    }

    public void goToOwnLocation() {
        if(ownLocation != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(ownLocation, 15));
    }

    public void nextDeviceFocus() {
        current_focus = current_focus < devices.size() -1 ? current_focus +1 : 0;
        goToDeviceLocation(current_focus);
    }

    private void goToDeviceLocation(int index) {
        if(devices.get(index) != null && last_broadcasts.get(index) != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(last_broadcasts.get(index).getLatitude(), last_broadcasts.get(index).getLongitude()), 15));
    }

    public void setMapType(int mapType) {
        googleMap.setMapType(mapType);
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
    public void onLocationChanged(Location loc) {
        if(loc == null) {
            Toast.makeText(getActivity(), "Can't get current location", Toast.LENGTH_SHORT).show();
        } else {
            setOwnLocation(loc);
        }
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

                    Device device = devices.get((Integer) marker.getTag());
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
                    device_last_update.setText(last_broadcasts.get((Integer) marker.getTag()).getTimeStampString());

                    return v;
                }
            });
        }

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        googleApiClient.connect();

        for(int i = 0; i < last_broadcasts.size(); i++) {
            if(last_broadcasts.get(i) != null) setDeviceLocation(i, last_broadcasts.get(i).getLatitude(), last_broadcasts.get(i).getLongitude());
        }
    }

    public void refresh(final Broadcast broadcast) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for(int i = 0; i < last_broadcasts.size(); i++) {
                    if(last_broadcasts.get(i).getDeviceID() == broadcast.getDeviceID()) {
                        last_broadcasts.set(i, broadcast);
                        setDeviceLocation(i, broadcast.getLatitude(), broadcast.getLongitude());
                    }
                }
            }
        });
    }
}
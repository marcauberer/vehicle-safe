package com.mrgames13.jimdo.vehiclesafe.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mrgames13.jimdo.vehiclesafe.Adapters.VehicleViewPagerAdapter;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.App.TrackingActivity;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;

public class VehicleFragment extends Fragment {

    //Konstanten
    private final int REQ_LOCATION_PERMISSION = 10001;
    private final int REQ_LOCATION_ENABLED = 10002;

    //Variablen als Objekte
    private Resources res;
    private ViewPager viewpager;
    private TabLayout tablayout;
    private View content_view;

    //Utils-Pakete
    private StorageUtils su;

    //Variablen


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Resourcen initialisieren
        res = getResources();

        //StorageUtils initalisieren
        su = new StorageUtils(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        content_view = inflater.inflate(R.layout.fragment_vehicle, container, false);

        //Komponenten initialisieren
        viewpager = content_view.findViewById(R.id.vehicle_viewpager);
        viewpager.setAdapter(new VehicleViewPagerAdapter(getActivity(), getChildFragmentManager(), MainActivity.own_instance.selected_device, su.getLastBroadcast(MainActivity.own_instance.selected_device.getDeviceID())));
        viewpager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int pos) {
                if(pos == 0) {
                    //Detail-Tab ausgewählt

                } else if(pos == 1) {
                    //Aktionen-Tab ausgewählt

                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        tablayout = content_view.findViewById(R.id.vehicle_tablayout);
        tablayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tablayout.setupWithViewPager(viewpager);
        tablayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewpager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        FloatingActionButton start_tracking = content_view.findViewById(R.id.start_tracking);
        start_tracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTracking();
            }
        });

        return content_view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_LOCATION_ENABLED) startTracking();
    }

    private void startTracking() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager manager = (LocationManager) getActivity().getSystemService( Context.LOCATION_SERVICE );
            if(manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                startActivity(new Intent(getActivity(), TrackingActivity.class));
            } else {
                AlertDialog d = new AlertDialog.Builder(getActivity())
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
                                Toast.makeText(getContext(), res.getString(R.string.please_enable_gps), Toast.LENGTH_LONG).show();
                                startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), REQ_LOCATION_ENABLED);
                            }
                        })
                        .create();
                d.show();
            }
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, REQ_LOCATION_PERMISSION);
        }
    }

    public void refresh(final Broadcast broadcast) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewpager.setAdapter(new VehicleViewPagerAdapter(getActivity(), getChildFragmentManager(), MainActivity.own_instance.selected_device, broadcast));
            }
        });
    }
}
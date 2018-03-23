package com.mrgames13.jimdo.vehiclesafe.Fragments;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mrgames13.jimdo.vehiclesafe.Adapters.DevicesRecyclerViewAdapter;
import com.mrgames13.jimdo.vehiclesafe.App.AddDeviceActivity;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.DeviceUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;

import java.util.ArrayList;

public class DevicesFragment extends Fragment {

    //Konstanten

    //Variablen als Objekte
    private Resources res;
    private DevicesRecyclerViewAdapter devices_adapter;
    private RecyclerView.LayoutManager devices_manager;
    private RecyclerView devices_view;
    private ArrayList<Device> devices;
    private View content_view;

    //Utils-Pakete
    private StorageUtils su;
    private DeviceUtils du;

    //Variablen

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Resourcen initialisieren
        res = getResources();

        //StorageUtils initalisieren
        su = new StorageUtils(getActivity());

        //DeviceUtils initialisieren initialisieren
        du = new DeviceUtils(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        content_view = inflater.inflate(R.layout.fragment_devices, container, false);

        //Recyclerview initialisieren
        devices_view = content_view.findViewById(R.id.devices_recyclerview);
        devices_manager = new LinearLayoutManager(getActivity());
        devices_view.setLayoutManager(devices_manager);
        devices_view.setHasFixedSize(true);

        //FloatingActionButton initialisieren
        FloatingActionButton fab_add = content_view.findViewById(R.id.fab_add_device);
        fab_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), AddDeviceActivity.class));
            }
        });

        return content_view;
    }

    @Override
    public void onStart() {
        super.onStart();

        devices = su.getAllDevices();
    }

    @Override
    public void onResume() {
        super.onResume();

        loadDevices();
        if(MainActivity.own_instance.smu.isInternetAvailable()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    devices = du.loadDevicesFromServer();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            devices_adapter = new DevicesRecyclerViewAdapter(getActivity(), res, devices, DevicesRecyclerViewAdapter.MODE_EDIT);
                            devices_view.setAdapter(devices_adapter);
                            content_view.findViewById(R.id.no_data).setVisibility(devices_adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                        }
                    });
                }
            }).start();
        }
    }

    private void loadDevices() {
        devices = su.getAllDevices();
        devices_adapter = new DevicesRecyclerViewAdapter(getActivity(), res, devices, DevicesRecyclerViewAdapter.MODE_EDIT);
        devices_view.setAdapter(devices_adapter);
        if(devices_adapter.getItemCount() == 0) content_view.findViewById(R.id.no_data).setVisibility(View.VISIBLE);
    }
}
package com.mrgames13.jimdo.vehiclesafe.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VehicleViewPagerAdapter extends FragmentPagerAdapter {

    //Konstanten

    //Variablen als Objekte
    private static Resources res;
    private ArrayList<String> tabTitles = new ArrayList<>();
    private static Device device;

    //Variablen

    public VehicleViewPagerAdapter(Activity activity, FragmentManager manager, Device device) {
        super(manager);
        res = activity.getResources();
        this.device = device;
        tabTitles.add(res.getString(R.string.details));
        tabTitles.add(res.getString(R.string.actions));
    }

    @Override
    public Fragment getItem(int pos) {
        if(pos == 0) return new DetailsFragment();
        if(pos == 1) return new ActionsFragment();
        return null;
    }

    @Override
    public int getCount() {
        return tabTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int pos) {
        return tabTitles.get(pos);
    }

    //-------------------------------------------Fragmente------------------------------------------

    public static class DetailsFragment extends Fragment {
        //Konstanten

        //Variablen als Objekte
        public static View contentView;
        private TextView name;
        private TextView id;
        private TextView last_update;
        private TextView status;
        private ImageView status_image;
        private TextView lat;
        private TextView lng;
        private TextView lbl_address;

        //Variablen

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            contentView = inflater.inflate(R.layout.tab_details, null);

            //Komponenten initialisieren
            name = contentView.findViewById(R.id.name);
            name.setText(res.getString(R.string.device_name_) + " " + device.getName());
            id = contentView.findViewById(R.id.id);
            id.setText(res.getString(R.string.device_id_) + " " + device.getDeviceID());
            last_update = contentView.findViewById(R.id.last_update);
            last_update.setText(res.getString(R.string.last_update_) + " " + device.getLastUpdate());
            status = contentView.findViewById(R.id.status);
            status_image = contentView.findViewById(R.id.status_image);
            lat = contentView.findViewById(R.id.lat);
            lat.setText(String.valueOf(device.getLat()));
            lng = contentView.findViewById(R.id.lng);
            lng.setText(String.valueOf(device.getLng()));
            lbl_address = contentView.findViewById(R.id.address);

            try{
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(device.getLat(), device.getLng(), 1);
                if(addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    lbl_address.setText(address + ", " + city);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return contentView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }

    public static class ActionsFragment extends Fragment {
        //Konstanten

        //Variablen als Objekte
        public static View contentView;

        //Variablen

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            contentView = inflater.inflate(R.layout.tab_actions, null);

            //Komponenten initialisieren
            final SwitchCompat enable_alarm = contentView.findViewById(R.id.enable_alarm);
            enable_alarm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean enabled) {
                    if(enabled) {
                        AlertDialog d = new AlertDialog.Builder(getContext())
                                .setCancelable(true)
                                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialogInterface) {
                                        enable_alarm.setChecked(false);
                                    }
                                })
                                .setTitle(res.getString(R.string.alarm))
                                .setMessage(res.getString(R.string.alarm_m))
                                .setPositiveButton(res.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        dialog.dismiss();
                                    }
                                })
                                .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int i) {
                                        enable_alarm.setChecked(false);
                                    }
                                })
                                .create();
                        d.show();
                    }
                }
            });

            return contentView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }
    }
}
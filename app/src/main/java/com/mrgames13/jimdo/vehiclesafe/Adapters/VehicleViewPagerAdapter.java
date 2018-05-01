package com.mrgames13.jimdo.vehiclesafe.Adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
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
    private static Broadcast last_broadcast;

    //Variablen

    public VehicleViewPagerAdapter(Activity activity, FragmentManager manager, Device device, Broadcast last_broadcast) {
        super(manager);
        res = activity.getResources();
        VehicleViewPagerAdapter.device = device;
        VehicleViewPagerAdapter.last_broadcast = last_broadcast;
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
        private TextView description;
        private TextView id;
        private TextView last_update;
        private TextView status;
        private ImageView status_image;
        private TextView lat;
        private TextView lng;
        private TextView alt;
        private TextView speed;
        private TextView lbl_address;

        //Variablen

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            contentView = inflater.inflate(R.layout.tab_details, null);

            //Komponenten initialisieren
            name = contentView.findViewById(R.id.name);

            description = contentView.findViewById(R.id.description);
            description.setText(device.getDescription());
            id = contentView.findViewById(R.id.id);
            last_update = contentView.findViewById(R.id.last_update);
            status = contentView.findViewById(R.id.status);
            status_image = contentView.findViewById(R.id.status_image);
            lat = contentView.findViewById(R.id.lat);
            lng = contentView.findViewById(R.id.lng);
            alt = contentView.findViewById(R.id.alt);
            speed = contentView.findViewById(R.id.speed);
            if(last_broadcast != null) speed.setText(String.valueOf(last_broadcast.getSpeed()));
            lbl_address = contentView.findViewById(R.id.address);

            name.setText(res.getString(R.string.device_name_) + " " + device.getName());
            id.setText(res.getString(R.string.device_id_) + " " + device.getDeviceID());
            last_update.setText(last_broadcast == null ? res.getString(R.string.no_update_yet) : res.getString(R.string.last_update_) + " " + last_broadcast.getTimeStampString());
            lat.setText(last_broadcast == null ? "0.0" : String.valueOf(last_broadcast.getLatitude()));
            lng.setText(last_broadcast == null ? "0.0" : String.valueOf(last_broadcast.getLongitude()));
            alt.setText(last_broadcast == null ? "0.0" : String.valueOf(last_broadcast.getAltitude()));

            if(last_broadcast != null) updateData();

            return contentView;
        }

        @Override
        public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
        }

        private void updateData() {
            //Adresse herausfinden
            try{
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(last_broadcast.getLatitude(), last_broadcast.getLongitude(), 1);
                if(addresses.size() > 0) {
                    String address = addresses.get(0).getAddressLine(0);
                    String city = addresses.get(0).getLocality();
                    lbl_address.setText(address + ", " + city);
                    lbl_address.setVisibility(View.VISIBLE);
                } else {
                    lbl_address.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                lbl_address.setVisibility(View.GONE);
            }

            //Status herausfinden
            if(last_broadcast.getLockMode() == Broadcast.STATE_LOCKED) {
                status.setText(res.getString(R.string.state_locked));
                status.setTextColor(res.getColor(R.color.locked));
                status_image.setImageResource(R.drawable.lock_outline);
                status_image.setColorFilter(ContextCompat.getColor(getContext(), R.color.locked), PorterDuff.Mode.SRC_IN);
            } else if(last_broadcast.getLockMode() == Broadcast.STATE_UNLOCKED) {
                status.setText(res.getString(R.string.state_unlocked));
                status.setTextColor(res.getColor(R.color.unlocked));
                status_image.setImageResource(R.drawable.lock_open);
                status_image.setColorFilter(ContextCompat.getColor(getContext(), R.color.unlocked), PorterDuff.Mode.SRC_IN);
            } else if(last_broadcast.getLockMode() == Broadcast.STATE_STOLEN) {
                status.setText(res.getString(R.string.state_stolen));
                status.setTextColor(res.getColor(R.color.stolen));
                status_image.setImageResource(R.drawable.alarm);
                status_image.setColorFilter(ContextCompat.getColor(getContext(), R.color.stolen), PorterDuff.Mode.SRC_IN);
                Animation a = AnimationUtils.loadAnimation(getContext(), R.anim.shaking);
                status_image.startAnimation(a);
            }
        }

        public void refresh(Broadcast broadcast) {
            last_broadcast = broadcast;
            last_update.setText(broadcast.getTimeStampString());
            lat.setText(String.valueOf(broadcast.getLatitude()));
            lng.setText(String.valueOf(broadcast.getLongitude()));
            alt.setText(String.valueOf(broadcast.getAltitude()));
            speed.setText(String.valueOf(broadcast.getSpeed()));
            updateData();
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
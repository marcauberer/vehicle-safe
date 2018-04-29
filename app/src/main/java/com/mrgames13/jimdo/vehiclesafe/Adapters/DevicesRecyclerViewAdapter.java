package com.mrgames13.jimdo.vehiclesafe.Adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mrgames13.jimdo.vehiclesafe.App.EditDeviceActivity;
import com.mrgames13.jimdo.vehiclesafe.App.MainActivity;
import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;
import com.mrgames13.jimdo.vehiclesafe.HelpClasses.Constants;
import com.mrgames13.jimdo.vehiclesafe.R;
import com.mrgames13.jimdo.vehiclesafe.Utils.ServerMessagingUtils;
import com.mrgames13.jimdo.vehiclesafe.Utils.StorageUtils;

import java.net.URLEncoder;
import java.util.ArrayList;

public class DevicesRecyclerViewAdapter extends RecyclerView.Adapter<DevicesRecyclerViewAdapter.ViewHolderClass> {

    //Konstanten
    public static final int MODE_DISPLAY = 10001;
    public static final int MODE_EDIT = 10002;

    //Variablen als Objekte
    private Resources res;
    private Context context;
    private ArrayList<Device> devices;
    private Handler h = new Handler();

    //Utils-Pakete
    private StorageUtils su;
    private ServerMessagingUtils smu;

    //Variablen
    private int mode = MODE_DISPLAY;

    public DevicesRecyclerViewAdapter(Context context, Resources res, ArrayList<Device> devices, int mode) {
        this.res = res;
        this.context = context;
        this.su = new StorageUtils(context);
        this.smu = new ServerMessagingUtils(context);
        this.devices = devices;
        this.mode = mode;
    }

    public class ViewHolderClass extends RecyclerView.ViewHolder {
        //Komponenten
        RelativeLayout item_container;
        ImageView item_image;
        TextView item_name;
        TextView item_details;
        ImageView item_edit;
        ImageView item_delete;
        //Variablen

        public ViewHolderClass(View itemView) {
            super(itemView);
            //Oberflächenkomponenten initialisieren
            item_container = itemView.findViewById(R.id.item_container);
            item_image = itemView.findViewById(R.id.item_image);
            item_name = itemView.findViewById(R.id.item_name);
            item_details = itemView.findViewById(R.id.item_data);
            item_edit = itemView.findViewById(R.id.item_edit);
            item_delete = itemView.findViewById(R.id.item_delete);
        }
    }

    @Override
    public ViewHolderClass onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device, null);
        return new ViewHolderClass(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolderClass holder, final int pos) {
        final Device device = devices.get(pos);

        //OnClick-Listener für Container setzen
        if(mode == MODE_EDIT) {
            holder.item_container.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Device öffnen
                    MainActivity.own_instance.launchVehicleFragement(device);
                }
            });
        }

        //Icon setzen
        if(device.getType() == Device.TYPE_MOTORCYCLE) {
            holder.item_image.setImageResource(R.drawable.directions_motorcycle);
        } else if(device.getType() == Device.TYPE_CAR) {
            holder.item_image.setImageResource(R.drawable.directions_car);
        }

        //Name setzen
        holder.item_name.setText(device.getName().equals("no_name") ? res.getString(R.string.no_name) : device.getName());

        //Details setzen
        holder.item_details.setText(device.getDescription());

        //Wenn der Adapter sich im EDIT_MODE befindet, edit und delete einblenden
        if(mode == MODE_EDIT) {
            holder.item_edit.setVisibility(View.VISIBLE);
            holder.item_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Item bearbeiten
                    Intent i = new Intent(context, EditDeviceActivity.class);
                    i.putExtra("DeviceID", device.getDeviceID());
                    if(!device.getName().equals("no_name")) i.putExtra("Name", device.getName());
                    if(!device.getName().equals("no_description")) i.putExtra("Description", device.getDescription());
                    if(su.getInt(device.getDeviceID() + "_color", -1) != -1) i.putExtra("Color", su.getInt(device.getDeviceID() + "_color"));
                    context.startActivity(i);
                }
            });
            holder.item_delete.setVisibility(View.VISIBLE);
            holder.item_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Item löschen
                    AlertDialog d = new AlertDialog.Builder(context)
                            .setCancelable(true)
                            .setTitle(res.getString(R.string.delete_device_link_t))
                            .setMessage(res.getString(R.string.delete_device_link_m))
                            .setNegativeButton(res.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveButton(res.getString(R.string.delete_link), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    if(smu.isInternetAvailable()) {
                                        //Verknüpfung löschen
                                        final ProgressDialog pd = new ProgressDialog(context);
                                        pd.setCancelable(false);
                                        pd.setMessage(res.getString(R.string.please_wait_));
                                        pd.show();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                try{
                                                    final String result = smu.sendRequest(null, "acc_id=" + URLEncoder.encode(su.getString("AccID"), "UTF-8") + "&command=removedevice&device_id=" + URLEncoder.encode(String.valueOf(device.getDeviceID()), "UTF-8") + "&password=" + URLEncoder.encode(su.getString("Password"), "UTF-8"));
                                                    h.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            pd.dismiss();
                                                            if(result.equals(Constants.ACTION_SUCCESSFUL_RESULT)) {
                                                                Toast.makeText(context, res.getString(R.string.deleted_link_successfully), Toast.LENGTH_SHORT).show();
                                                                devices.remove(devices.get(pos));
                                                                notifyItemRemoved(pos);
                                                            } else {
                                                                Toast.makeText(context, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                    });
                                                } catch (Exception e) {
                                                    pd.dismiss();
                                                    Toast.makeText(context, res.getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        }).start();
                                    } else {
                                        smu.checkConnection(holder.itemView);
                                    }
                                }
                            })
                            .create();
                    d.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
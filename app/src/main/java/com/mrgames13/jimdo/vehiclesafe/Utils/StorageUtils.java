package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Device;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

public class StorageUtils extends SQLiteOpenHelper {

    //Konstanten
    private final String DEFAULT_STRING_VALUE = "";
    private final int DEFAULT_INT_VALUE = 0;
    private final boolean DEFAULT_BOOLEAN_VALUE = false;
    public static final String TABLE_DEVICES = "Devices";
    public static final String TABLE_BROADCASTS = "Broadcasts";

    //Variablen als Objekte
    private SharedPreferences prefs;
    private SharedPreferences.Editor e;

    //Variablen

    public StorageUtils(Context context) {
        super(context, "database.db", null, 1);
        this.prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    //------------------------------------Shared Preferences----------------------------------------

    public void putString(String name, String value) {
        e = prefs.edit();
        e.putString(name, value);
        e.commit();
    }
    public String getString(String name) {
        return prefs.getString(name, DEFAULT_STRING_VALUE);
    }
    public String getString(String name, String default_value) {
        return prefs.getString(name, default_value);
    }

    public void putInt(String name, int value) {
        e = prefs.edit();
        e.putInt(name, value);
        e.commit();
    }
    public int getInt(String name) {
        return prefs.getInt(name, DEFAULT_INT_VALUE);
    }
    public int getInt(String name, int default_value) {
        return prefs.getInt(name, default_value);
    }

    public void putBoolean(String name, boolean value) {
        e = prefs.edit();
        e.putBoolean(name, value);
        e.commit();
    }
    public boolean getBoolean(String name) {
        return prefs.getBoolean(name, DEFAULT_BOOLEAN_VALUE);
    }
    public boolean getBoolean(String name, boolean default_value) {
        return prefs.getBoolean(name, default_value);
    }

    public void putStringSet(String name, Set<String> value) {
        e = prefs.edit();
        e.putStringSet(name, value);
        e.commit();
    }
    public Set<String> getStringSet(String name) {
        return prefs.getStringSet(name, null);
    }

    public void removePair(String name) {
        e = prefs.edit();
        e.remove(name);
        e.commit();
    }

    public void clear() {
        prefs.edit().clear().commit();
    }

    //------------------------------------------------Datenbank---------------------------------------------

    @Override
    public void onCreate(SQLiteDatabase db) {
        try{
            //Tabellen erstellen
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES + " (device_id text, device_name text, device_description text, device_type integer, last_update text, last_lat text, last_lng text, last_alt text, settings text, device_state integer);");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BROADCASTS + " (bc_id text, time_stamp text, from_id text, cmd text, longitude real, latitude real, lock_mode integer);");
        } catch (Exception e) {
            Log.e("VS", "Database creation error: ", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {

        }
    }

    public long addRecord(String table, ContentValues values) {
        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(table, null, values);
        //db.close();
        return id;
    }

    public void removeRecord(String table, String id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(table, "ROWID", new String[] {id});
        //db.close();
    }

    public void execSQL(String command) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(command);
        //db.close();
    }

    //----------------------------------------------Devices-----------------------------------------

    public void addDevice(Device device) {
        ContentValues values = new ContentValues();
        values.put("device_id", device.getDeviceID());
        values.put("device_name", device.getName());
        values.put("device_description", device.getDescription());
        values.put("device_type", device.getType());
        values.put("last_update", device.getLastUpdate());
        values.put("last_lat", device.getLat());
        values.put("last_lng", device.getLng());
        values.put("last_alt", device.getAlt());
        values.put("settings", device.getSettings());
        values.put("device_state", device.getState());
        addRecord(TABLE_DEVICES, values);
    }

    public void clearDevices() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DEVICES, "", null);
    }

    public ArrayList<Device> getAllDevices() {
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DEVICES, null);
            ArrayList<Device> devices = new ArrayList<>();
            while(cursor.moveToNext()) {
                devices.add(new Device(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getLong(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getDouble(7), cursor.getString(8), cursor.getInt(9)));
            }
            cursor.close();
            Collections.sort(devices);
            return devices;
        } catch (Exception e) {
            Log.e("VS", "Error loading devices", e);
        }
        return new ArrayList<>();
    }

    public Device getDevice(String device_id) {
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DEVICES + " WHERE device_id='" + device_id + "'", null);
            Device device = null;
            while(cursor.moveToNext()) {
                device = new Device(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getLong(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getDouble(7), cursor.getString(8), cursor.getInt(9));
            }
            cursor.close();
            return device;
        } catch (Exception e) {
            Log.e("VS", "Error loading device", e);
        }
        return null;
    }

    //----------------------------------------------Broadcasts--------------------------------------

    /*public ArrayList<Broadcast> getAllBroadcasts(String device_id) {
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BROADCASTS + " WHERE from_id='" + device_id + "'", null);
            ArrayList<Broadcast> broadcasts = new ArrayList<>();
            while(cursor.moveToNext()) {
                Date
                broadcasts.add(new Broadcast(, device_id, cursor.getString(3), cursor.getLong(4), cursor.getLong(5), cursor.getInt(6)));
            }
            cursor.close();
            Collections.sort(broadcasts);
            return broadcasts;
        } catch (Exception e) {
            Log.e("VS", "Error loading broadcasts", e);
        }
        return new ArrayList<>();
    }*/
}
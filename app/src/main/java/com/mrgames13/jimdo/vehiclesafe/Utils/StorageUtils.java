package com.mrgames13.jimdo.vehiclesafe.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.mrgames13.jimdo.vehiclesafe.CommonObjects.Broadcast;
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
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES + " (device_id integer, device_name text, device_description text, device_type integer, settings text);");
            db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_BROADCASTS + " (device_id integer, time_stamp integer, lock_mode integer, latitude real, longitude real, altitude real, speed real, fix integer, fix_quality real);");
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
        values.put("settings", device.getSettings());
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
                devices.add(new Device(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4)));
            }
            cursor.close();
            Collections.sort(devices);
            return devices;
        } catch (Exception e) {
            Log.e("VS", "Error loading devices", e);
        }
        return new ArrayList<>();
    }

    public Device getDevice(int device_id) {
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_DEVICES + " WHERE device_id=" + String.valueOf(device_id), null);
            Device device = null;
            while(cursor.moveToNext()) {
                device = new Device(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getString(4));
            }
            cursor.close();
            return device;
        } catch (Exception e) {
            Log.e("VS", "Error loading device", e);
        }
        return null;
    }

    //----------------------------------------------Broadcasts--------------------------------------

    public void addBroadcast(Broadcast broadcast) {
        ContentValues values = new ContentValues();
        values.put("device_id", broadcast.getDeviceID());
        values.put("time_stamp", broadcast.getTimeStampLong());
        values.put("lock_mode", broadcast.getLockMode());
        values.put("latitude", broadcast.getLatitude());
        values.put("longitude", broadcast.getLongitude());
        values.put("altitude", broadcast.getAltitude());
        values.put("speed", broadcast.getSpeed());
        values.put("fix", broadcast.isFix());
        values.put("fix_quality", broadcast.getFixQuality());
        addRecord(TABLE_BROADCASTS, values);
    }

    public boolean addBroadcastIfTimestampNotExists(Broadcast broadcast) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT time_stamp FROM " + TABLE_BROADCASTS + " WHERE device_id=" + String.valueOf(broadcast.getDeviceID()) + " AND time_stamp=" + String.valueOf(broadcast.getTimeStampLong()), null);
        if(cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put("device_id", broadcast.getDeviceID());
            values.put("time_stamp", broadcast.getTimeStampLong());
            values.put("lock_mode", broadcast.getLockMode());
            values.put("latitude", broadcast.getLatitude());
            values.put("longitude", broadcast.getLongitude());
            values.put("altitude", broadcast.getAltitude());
            values.put("speed", broadcast.getSpeed());
            values.put("fix", broadcast.isFix());
            values.put("fix_quality", broadcast.getFixQuality());
            addRecord(TABLE_BROADCASTS, values);
            return true;
        }
        return false;
    }

    public ArrayList<Broadcast> getAllBroadcasts(int device_id) {
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BROADCASTS + " WHERE device_id=" + String.valueOf(device_id), null);
            ArrayList<Broadcast> broadcasts = new ArrayList<>();
            while(cursor.moveToNext()) {
                Broadcast b = new Broadcast();
                b.setDeviceID(cursor.getInt(0));
                b.setTimeStamp(cursor.getLong(1));
                b.setLockMode(cursor.getInt(2));
                b.setLatitude(cursor.getDouble(3));
                b.setLongitude(cursor.getDouble(4));
                b.setAltitude(cursor.getDouble(5));
                b.setSpeed(cursor.getDouble(6));
                b.setFix(cursor.getInt(7) > 0);
                b.setFixQuality(cursor.getDouble(8));
                broadcasts.add(b);
            }
            cursor.close();
            Collections.sort(broadcasts);
            return broadcasts;
        } catch (Exception e) {
            Log.e("VS", "Error loading broadcasts", e);
        }
        return new ArrayList<>();
    }

    public ArrayList<Broadcast> getBroadcastHistory(int device_id) {
        ArrayList<Broadcast> history = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BROADCASTS + " WHERE device_id=" + String.valueOf(device_id), null);
        while(cursor.moveToNext()) {
            Broadcast b = new Broadcast();
            b.setDeviceID(cursor.getInt(0));
            b.setTimeStamp(cursor.getLong(1));
            b.setLockMode(cursor.getInt(2));
            b.setLatitude(cursor.getDouble(3));
            b.setLongitude(cursor.getDouble(4));
            b.setAltitude(cursor.getDouble(5));
            b.setSpeed(cursor.getDouble(6));
            b.setFix(cursor.getInt(7) > 0);
            b.setFixQuality(cursor.getDouble(8));
            history.add(b);
        }
        cursor.close();
        Collections.sort(history);
        return history;
    }

    public Broadcast getLastBroadcast(int device_id) {
        try{
            SQLiteDatabase db = getReadableDatabase();
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BROADCASTS + " WHERE device_id=" + String.valueOf(device_id) + " ORDER BY time_stamp DESC", null);
            while(cursor.moveToNext()) {
                Broadcast b = new Broadcast();
                b.setDeviceID(cursor.getInt(0));
                b.setTimeStamp(cursor.getLong(1));
                b.setLockMode(cursor.getInt(2));
                b.setLatitude(cursor.getDouble(3));
                b.setLongitude(cursor.getDouble(4));
                b.setAltitude(cursor.getDouble(5));
                b.setSpeed(cursor.getDouble(6));
                b.setFix(cursor.getInt(7) > 0);
                b.setFixQuality(cursor.getDouble(8));
                cursor.close();
                return b;
            }
        } catch (Exception e) {
            Log.e("VS", "Error loading broadcasts", e);
        }
        return null;
    }
}
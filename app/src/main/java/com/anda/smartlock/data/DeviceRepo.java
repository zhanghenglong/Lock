package com.anda.smartlock.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by anda on 2016/11/9.
 */

public class DeviceRepo {
    private DatabaseHelper dbHelper;

    public DeviceRepo(Context context){
        dbHelper=new DatabaseHelper(context);
    }

    public int insert(Device device) {
        //打开连接，写入数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(Device.KEY_MAC, device.mac);
        values.put(Device.KEY_NAME, device.name);

        long device_id = db.insert(Device.TABLE,null,values);
        db.close();
        return (int)device_id;
    }

    public void delete(int devic_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(Device.TABLE,Device.KEY_ID+"=?", new String[]{String.valueOf(devic_id)});
        db.close();
    }

    public void update(Device device) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(Device.KEY_MAC, device.mac);
        values.put(Device.KEY_NAME, device.name);

        db.update(Device.TABLE,values,Device.KEY_ID+"=?",new String[] { String.valueOf(device.device_id) });
        db.close();

    }

    public ArrayList<HashMap<String, String>> getDeviceList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT "+
                Device.KEY_ID+","+
                Device.KEY_MAC+","+
                Device.KEY_NAME+","+
                " FROM "+Device.TABLE;
        ArrayList<HashMap<String,String>> deviceList = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> device = new HashMap<String,String>();
                device.put("id",cursor.getString(cursor.getColumnIndex(Device.KEY_ID)));
                device.put("mac",cursor.getString(cursor.getColumnIndex(Device.KEY_MAC)));
                deviceList.add(device);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return deviceList;
    }

    public Cursor queryDataFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT *"+
                " FROM "+Device.TABLE;
        ArrayList<HashMap<String,String>> visitorList = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery(selectQuery,null);

        //   cursor.close();
        //    db.close();
        return cursor;
    }

    public Device getDeviceById(int Id){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                Device.KEY_ID+","+
                Device.KEY_MAC+","+
                Device.KEY_NAME+","+
                " FROM "+Device.TABLE
                + " WHERE " +
                Device.KEY_ID + "=?";
        int iCount = 0;
        Device device = new Device();
        Cursor cursor = db.rawQuery(selectQuery,new String[]{String.valueOf(Id)});
        if(cursor.moveToFirst()){
            do{
                device.device_id = cursor.getInt(cursor.getColumnIndex(Device.KEY_ID));
                device.mac = cursor.getString(cursor.getColumnIndex(Device.KEY_MAC));
                device.name = cursor.getString(cursor.getColumnIndex(Device.KEY_NAME));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return device;
    }
}

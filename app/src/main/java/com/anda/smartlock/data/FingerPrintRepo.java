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

public class FingerPrintRepo {
    private DatabaseHelper dbHelper;

    public FingerPrintRepo(Context context){
        dbHelper=new DatabaseHelper(context);
    }

    public int insert(FingerPrint fp) {
        //打开连接，写入数据
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FingerPrint.KEY_ADDRESS, fp.address);
        values.put(FingerPrint.KEY_DEVICE_MAC, fp.device_mac);
        values.put(FingerPrint.KEY_NAME, fp.name);

        long fp_id = db.insert(FingerPrint.TABLE,null,values);
        db.close();
        return (int)fp_id;
    }

    public void delete(int fp_id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(FingerPrint.TABLE,FingerPrint.KEY_FINGERPRINT_ID+"=?", new String[]{String.valueOf(fp_id)});
        db.close();
    }

    public void update(FingerPrint fp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(FingerPrint.KEY_ADDRESS, fp.address);
        values.put(FingerPrint.KEY_DEVICE_MAC, fp.device_mac);
        values.put(FingerPrint.KEY_NAME, fp.name);

        db.update(FingerPrint.TABLE,values,FingerPrint.KEY_FINGERPRINT_ID+"=?",new String[] { String.valueOf(fp.fp_id) });
        db.close();

    }

    public ArrayList<HashMap<String, String>> getFingerPrintList(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT "+
                FingerPrint.KEY_FINGERPRINT_ID+","+
                FingerPrint.KEY_ADDRESS+","+
                FingerPrint.KEY_DEVICE_MAC+","+
                FingerPrint.KEY_NAME+
                " FROM "+FingerPrint.TABLE;
        ArrayList<HashMap<String,String>> fingerPrintList = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery(selectQuery,null);

        if(cursor.moveToFirst()){
            do{
                HashMap<String,String> fingerPrint = new HashMap<String,String>();
                fingerPrint.put("id",cursor.getString(cursor.getColumnIndex(FingerPrint.KEY_FINGERPRINT_ID)));
                fingerPrint.put("name",cursor.getString(cursor.getColumnIndex(FingerPrint.KEY_NAME)));
                fingerPrintList.add(fingerPrint);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return fingerPrintList;
    }

    public Cursor queryDataFromDB() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selectQuery = "SELECT *"+
                " FROM "+FingerPrint.TABLE;
        ArrayList<HashMap<String,String>> fingerPrintList = new ArrayList<HashMap<String, String>>();
        Cursor cursor = db.rawQuery(selectQuery,null);

        //   cursor.close();
        //    db.close();
        return cursor;
    }

    public FingerPrint getFingerPrintById(int Id){
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        String selectQuery="SELECT "+
                FingerPrint.KEY_FINGERPRINT_ID+","+
                FingerPrint.KEY_ADDRESS+","+
                FingerPrint.KEY_DEVICE_MAC+","+
                FingerPrint.KEY_NAME+
                " FROM "+FingerPrint.TABLE
                + " WHERE " +
                Device.KEY_ID + "=?";
        int iCount = 0;
        FingerPrint fingerPrint = new FingerPrint();
        Cursor cursor = db.rawQuery(selectQuery,new String[]{String.valueOf(Id)});
        if(cursor.moveToFirst()){
            do{
                fingerPrint.fp_id = cursor.getInt(cursor.getColumnIndex(FingerPrint.KEY_FINGERPRINT_ID));
                fingerPrint.device_mac = cursor.getString(cursor.getColumnIndex(FingerPrint.KEY_DEVICE_MAC));
                fingerPrint.address = cursor.getString(cursor.getColumnIndex(FingerPrint.KEY_ADDRESS));
                fingerPrint.name = cursor.getString(cursor.getColumnIndex(FingerPrint.KEY_NAME));
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return fingerPrint;
    }
}

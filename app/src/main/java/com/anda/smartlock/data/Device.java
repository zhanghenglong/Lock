package com.anda.smartlock.data;

/**
 * Created by anda on 2016/11/9.
 */

public class Device {
    public static final String TABLE = "Device";

    //表的各域名
    public static final String KEY_ID = "id";
    public static final String KEY_MAC = "mac";
    public static final String KEY_NAME = "name";

    public int device_id;
    public String mac;
    public String name;
}

package com.anda.smartlock.data;

/**
 * Created by anda on 2016/11/9.
 */

public class FingerPrint {
    public static final String TABLE = "FingerPrint";

    //表的各域名
    public static final String KEY_FINGERPRINT_ID = "id";
    public static final String KEY_DEVICE_MAC = "device_mac";
    public static final String KEY_ADDRESS = "address";
    public static final String KEY_NAME = "name";

    public int fp_id;
    public String device_mac;
    public String address;
    public String name;
}

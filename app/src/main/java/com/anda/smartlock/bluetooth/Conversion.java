package com.anda.smartlock.bluetooth;

import android.util.Log;

import java.util.Formatter;

/**
 * Created by 10087421 on 2016/6/20.
 */
public class Conversion {
    public static byte loUint16(short v) {
        return (byte) (v & 0xFF);
    }

    public static byte hiUint16(short v) {
        return (byte) (v >> 8);
    }

    public static short buildUint16(byte hi, byte lo) {
        return (short) ((hi << 8) + (lo & 0xff));
    }

    public static String BytetohexString(byte[] b, int len) {
        StringBuilder sb = new StringBuilder(b.length * (2 + 1));
        Formatter formatter = new Formatter(sb);

        for (int i = 0; i < len; i++) {
            if (i < len - 1)
                formatter.format("%02X:", b[i]);
            else
                formatter.format("%02X", b[i]);

        }
        formatter.close();

        return sb.toString();
    }

    static String BytetohexString(byte[] b, boolean reverse) {
        StringBuilder sb = new StringBuilder(b.length * (2 + 1));
        Formatter formatter = new Formatter(sb);

        if (!reverse) {
            for (int i = 0; i < b.length; i++) {
                if (i < b.length - 1)
                    formatter.format("%02X:", b[i]);
                else
                    formatter.format("%02X", b[i]);

            }
        } else {
            for (int i = (b.length - 1); i >= 0; i--) {
                if (i > 0)
                    formatter.format("%02X:", b[i]);
                else
                    formatter.format("%02X", b[i]);

            }
        }
        formatter.close();

        return sb.toString();
    }

    // Convert hex String to Byte
    public static int hexStringtoByte(String sb, byte[] results) {

        int i = 0;
        boolean j = false;

        if (sb != null) {
            for (int k = 0; k < sb.length(); k++) {
                if (((sb.charAt(k)) >= '0' && (sb.charAt(k) <= '9')) || ((sb.charAt(k)) >= 'a' && (sb.charAt(k) <= 'f'))
                        || ((sb.charAt(k)) >= 'A' && (sb.charAt(k) <= 'F'))) {
                    if (j) {
                        results[i] += (byte) (Character.digit(sb.charAt(k), 16));
                        i++;
                    } else {
                        results[i] = (byte) (Character.digit(sb.charAt(k), 16) << 4);
                    }
                    j = !j;
                }
            }
        }
        return i;
    }

    public static boolean isAsciiPrintable(String str) {
        if (str == null) {
            return false;
        }
        int sz = str.length();
        for (int i = 0; i < sz; i++) {
            if (isAsciiPrintable(str.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    private static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length()/2;
        Log.d("length",""+length);
        char[] hexChars = hexString.toCharArray();
        Log.d("char length",""+hexChars.length);
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
            int test = d[i] & 0xFF;
            Log.d("int value",""+test);
        }
        return d;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static byte[] hex2Bytes(String src) {
        byte[] res = new byte[src.length() / 2];
        char[] chs = src.toCharArray();
        for (int i = 0, c = 0; i < chs.length; i += 2, c++) {
            res[c] = (byte) (Integer.parseInt(new String(chs, i, 2), 16));
        }
        return res;
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static int[] byte2Int(byte[] src) {
        if (src == null || src.length <= 0) {
            return null;
        }
        int len = src.length;
        int[] tmp = new int[len];
        for(int i=0; i<len; i++) {
            tmp[i] = src[i] & 0xFF;
            Log.d("Int value",""+tmp[i]);
        }
        return tmp;
    }
}

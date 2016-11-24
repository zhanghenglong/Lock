package com.anda.smartlock.tools;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by anda on 2016/11/24.
 */

public class LogWriter {
    private final static LogWriter mLogWriter =
            new LogWriter(Environment.getExternalStorageDirectory() + File.separator + "lockLogs.txt");

    private static Writer mWriter;

    private static SimpleDateFormat df;

    private LogWriter(String file_path) {
        try {
            File mFile = new File(file_path);
            mWriter = new BufferedWriter(new FileWriter(file_path), 2048);
            df = new SimpleDateFormat("[yy-MM-dd hh:mm:ss]: ");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static LogWriter getInstance() {
        return mLogWriter;
    }

    public static void showLog(String msg) {
        try {
            mLogWriter.print(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() throws IOException {
        mWriter.close();
    }

    public void print(String log) throws IOException {
        mWriter.write(df.format(new Date()));
        mWriter.write(log);
        mWriter.write("\n");
        mWriter.flush();
    }

    public void print(Class cls, String log) throws IOException { //如果还想看是在哪个类里可以用这个方法
        mWriter.write(df.format(new Date()));
        mWriter.write(cls.getSimpleName() + " ");
        mWriter.write(log);
        mWriter.write("\n");
        mWriter.flush();
    }
}

package com.anda.smartlock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by 10087421 on 2016/7/25.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    //数据库版本号
    private static final int DATABASE_VERSION=1;

    //数据库名称
    private static final String DATABASE_NAME="lock.db";

    private static final String Device_TABLE = "device";
    private static final String FingerPrint_TABLE = "fingerPrint";

    public DatabaseHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    /**
     * Create a helper object to create, open, and/or manage a database.
     * This method always returns very quickly.  The database is not actually
     * created or opened until one of {@link #getWritableDatabase} or
     * {@link #getReadableDatabase} is called.
     *
     * @param context to use to open or create the database
     * @param name    of the database file, or null for an in-memory database
     * @param factory to use for creating cursor objects, or null for the default
     * @param version number of the database (starting at 1); if the database is older,
     *                {@link #onUpgrade} will be used to upgrade the database; if the database is
     *                newer, {@link #onDowngrade} will be used to downgrade the database
     */
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建数据表
        String CREATE_TABLE_DEVICE = "CREATE TABLE "+ Device.TABLE+"("
                +Device.KEY_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +Device.KEY_MAC+" TEXT, "
                +Device.KEY_NAME+" TEXT)";
        db.execSQL(CREATE_TABLE_DEVICE);

        //创建数据表
        String CREATE_TABLE_FINGERPRINT = "CREATE TABLE "+ FingerPrint.TABLE+"("
                +FingerPrint.KEY_FINGERPRINT_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "
                +FingerPrint.KEY_DEVICE_MAC+" TEXT, "
                +FingerPrint.KEY_ADDRESS+" TEXT, "
                +FingerPrint.KEY_NAME+" TEXT)";
        db.execSQL(CREATE_TABLE_FINGERPRINT);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p/>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //如果旧表存在，删除，所以数据将会消失
        db.execSQL("DROP TABLE IF EXISTS "+ Device.TABLE);
        db.execSQL("DROP TABLE IF EXISTS "+ FingerPrint.TABLE);

        //再次创建表
        onCreate(db);

    }
}
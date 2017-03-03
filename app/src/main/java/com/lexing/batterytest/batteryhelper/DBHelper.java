package com.lexing.batterytest.batteryhelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Ray on 2017/1/22.
 */
//process.a
public class DBHelper extends SQLiteOpenHelper {
    private static final String[] columns = new String[]{"package_name"};
    private static volatile DBHelper instance = null;
    private final Object lock = new Object();
    private boolean isEmpty = false;

    private DBHelper(Context context) {
        super(context, "IgnoreApp", null, 1);
    }

    public static synchronized DBHelper getInstance(Context context) {
        synchronized (DBHelper.class) {
            if (instance == null) {
                instance = new DBHelper(context);
            }
        }
        return instance;
    }

    //void a(ArrayList<String>)
    public void insert(ArrayList<String> arrayList) {
        synchronized (this.lock) {
            SQLiteDatabase writableDatabase = getWritableDatabase();
            writableDatabase.beginTransaction();
            ContentValues contentValues = new ContentValues();
            try {
                for (String str : arrayList) {
                    contentValues.put("package_name", str);
                    writableDatabase.insert("applicationKey", null, contentValues);
                }
                writableDatabase.setTransactionSuccessful();
                writableDatabase.endTransaction();
            } catch (Exception e) {
                writableDatabase.endTransaction();
            }
        }
    }

    //boolean a()
    public boolean isEmpty() {
        return isEmpty;
    }

    //HashSet<String> b()
    public HashSet<String> query() {
        HashSet<String> hashSet;
        Cursor query;
        synchronized (this.lock) {
            hashSet = new HashSet<>();
            try {
                query = getReadableDatabase().query("applicationKey", columns, null, null, null, null, null);
                if (query != null) {
                    try {
                        if (query.moveToFirst()) {
                            do {
                                String pkgName = query.getString(0);
                                if (!TextUtils.isEmpty(pkgName)) {
                                    hashSet.add(pkgName);
                                }
                            } while (query.moveToNext());
                        }
                    } catch (Exception e) {
                        try {
                            query.close();
                        } catch (Exception e2) {
                        }
                        return hashSet;
                    }
                }
                if (query != null) {
                    try {
                        query.close();
                    } catch (Exception e4) {
                        return hashSet;
                    }
                }
            } catch (Exception e5) {
                return hashSet;
            }
        }
        return hashSet;
    }

    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        synchronized (this.lock) {
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS applicationKey(_id INTEGER PRIMARY KEY AUTOINCREMENT, package_name TEXT, UNIQUE (package_name) ON CONFLICT REPLACE)");
            isEmpty = true;
        }
    }

    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        synchronized (this.lock) {
            sQLiteDatabase.execSQL("DROP TABLE IF EXISTS applicationKey");
            sQLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS applicationKey(_id INTEGER PRIMARY KEY AUTOINCREMENT, package_name TEXT, UNIQUE (package_name) ON CONFLICT REPLACE)");
            isEmpty = true;
        }
    }
}


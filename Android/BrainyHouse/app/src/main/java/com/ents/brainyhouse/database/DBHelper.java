package com.ents.brainyhouse.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {
    final String TAG = DBHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "BrainyHouse.db";

    private static final String FRIDGE_TABLE_NAME = "fridge";
    public static final String FRIDGE_COLUMN_ID = "id";
    public static final String FRIDGE_COLUMN_NAME = "name";
    public static final String FRIDGE_COLUMN_RFID = "rfid";
    public static final String FRIDGE_COLUMN_COUNT = "count";

    private static final String SECURITY_TABLE_NAME = "security";
    public static final String SECURITY_COLUMN_ID = "id";
    public static final String SECURITY_COLUMN_NAME = "name";
    public static final String SECURITY_COLUMN_RFID = "rfid";

    private static final String CREATE_FRIDGE_TABLE =
            "create table " + FRIDGE_TABLE_NAME + "(" +
                    FRIDGE_COLUMN_ID + " integer primary key, " +
                    FRIDGE_COLUMN_NAME + " text, " +
                    FRIDGE_COLUMN_RFID + " text, " +
                    FRIDGE_COLUMN_COUNT + " integer)";


    private static final String CREATE_SECURITY_TABLE =
            "create table " + SECURITY_TABLE_NAME + "(" +
                    SECURITY_COLUMN_ID + " integer primary key, " +
                    SECURITY_COLUMN_NAME + " text, " +
                    SECURITY_COLUMN_RFID + " text)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_FRIDGE_TABLE);
        db.execSQL(CREATE_SECURITY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + FRIDGE_TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + SECURITY_TABLE_NAME);
        onCreate(db);
    }

    public boolean addFridgeItem(String name, String rfid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_NAME, name);
        contentValues.put(FRIDGE_COLUMN_RFID, rfid);
        contentValues.put(FRIDGE_COLUMN_COUNT, 0);

        db.insert(FRIDGE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean addFridgeItem(FridgeItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_NAME, item.getName());
        contentValues.put(FRIDGE_COLUMN_RFID, item.getRFID());
        contentValues.put(FRIDGE_COLUMN_COUNT, item.getCount());

        db.insert(FRIDGE_TABLE_NAME, null, contentValues);
        return true;
    }

    public Cursor getFridgeItemByID(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_ID + " = " + id, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getFridgeItemByRFID(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_RFID + " = " + rfid, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public int getFridgeItemIDByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_NAME + " = " + name, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        return cursor.getColumnIndex(FRIDGE_COLUMN_ID);
    }

    public int getNumberOfRowsInFridgeTable() {
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, FRIDGE_TABLE_NAME);
        return numRows;
    }

    public boolean updateFridgeItem(FridgeItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_NAME, item.getName());
        contentValues.put(FRIDGE_COLUMN_RFID, item.getRFID());
        contentValues.put(FRIDGE_COLUMN_COUNT, item.getCount());

        db.update(FRIDGE_TABLE_NAME, contentValues, "id = ? ", new String[]{Integer.toString(item.getID())});
        return true;
    }

    public boolean updateFrdgeItem(String rfid, int count) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_COUNT, count);

        db.update(FRIDGE_TABLE_NAME, contentValues, "rfid = ? ", new String[]{rfid});
        return true;
    }

    public Integer deleteFridgeItem(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FRIDGE_TABLE_NAME, "id = ? ", new String[]{Integer.toString(id)});
    }

    public Integer deleteFridgeItem(FridgeItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FRIDGE_TABLE_NAME, "id = ? ", new String[]{Integer.toString(item.getID())});
    }

    public boolean deleteAllFridgeItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        while (cursor.isAfterLast() == false) {
            deleteFridgeItem(cursor.getColumnIndex(FRIDGE_COLUMN_ID));
            cursor.moveToNext();
        }

        return true;
    }

    public ArrayList<FridgeItem> getAllFridgeItems() {
        ArrayList arrayList = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        while (cursor.isAfterLast() == false) {
            int id = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_NAME));
            String rfid = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_RFID));
            int count = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_COUNT));

            FridgeItem item = new FridgeItem(id, name, rfid, count);
            arrayList.add(item);
            cursor.moveToNext();
        }
        return arrayList;
    }


    public boolean addSecurityItem(String rfid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(SECURITY_COLUMN_RFID, rfid);

        db.insert(SECURITY_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean addSecurityItem(SecurityItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_RFID, item.getRFID());

        db.insert(SECURITY_TABLE_NAME, null, contentValues);
        return true;
    }


    public ArrayList<SecurityItem> getAllSecurityItems() {
        ArrayList arrayList = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SECURITY_TABLE_NAME, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        while (cursor.isAfterLast() == false) {
            int id = cursor.getInt(cursor.getColumnIndex(SECURITY_COLUMN_ID));
            String rfid = cursor.getString(cursor.getColumnIndex(SECURITY_COLUMN_RFID));

            SecurityItem item = new SecurityItem(id, rfid);
            arrayList.add(item);
            cursor.moveToNext();
        }
        return arrayList;
    }


    public boolean deleteAllSecurityItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + SECURITY_TABLE_NAME, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        while (cursor.isAfterLast() == false) {
            deleteFridgeItem(cursor.getColumnIndex(SECURITY_COLUMN_ID));
            cursor.moveToNext();
        }

        return true;
    }

    public Integer deleteSecurityItem(Integer id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(SECURITY_TABLE_NAME, "id = ? ", new String[]{Integer.toString(id)});
    }

    public Integer deleteSecurityItem(SecurityItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(FRIDGE_TABLE_NAME, "id = ? ", new String[]{Integer.toString(item.getID())});
    }

}
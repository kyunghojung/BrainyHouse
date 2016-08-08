package com.imes.iothome.database;

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

    private static final String DATABASE_NAME = "IOTHome.db";

    private static final String FRIDGE_TABLE_NAME = "fridge";
    public static final String FRIDGE_COLUMN_ID = "id";
    public static final String FRIDGE_COLUMN_NAME = "name";
    public static final String FRIDGE_COLUMN_RFID = "rfid";
    public static final String FRIDGE_COLUMN_SHELFLIFE = "shelfLife";
    public static final String FRIDGE_COLUMN_AMOUNT = "amount";

    private static final String USER_TABLE_NAME = "user";
    public static final String USER_COLUMN_ID = "id";
    public static final String USER_COLUMN_NAME = "name";
    public static final String USER_COLUMN_RFID = "rfid";

    private static final String CREATE_FRIDGE_TABLE =
            "create table " + FRIDGE_TABLE_NAME + "(" +
                    FRIDGE_COLUMN_ID + " integer primary key, " +
                    FRIDGE_COLUMN_NAME + " text, " +
                    FRIDGE_COLUMN_RFID + " text, " +
                    FRIDGE_COLUMN_SHELFLIFE + " text, " +
                    FRIDGE_COLUMN_AMOUNT + " integer)";


    private static final String CREATE_SECURITY_TABLE =
            "create table " + USER_TABLE_NAME + "(" +
                    USER_COLUMN_ID + " integer primary key, " +
                    USER_COLUMN_NAME + " text, " +
                    USER_COLUMN_RFID + " text)";

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
        db.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        onCreate(db);
    }

    public boolean addFridgeItem(String name, String rfid, String shelfLife) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_NAME, name);
        contentValues.put(FRIDGE_COLUMN_RFID, rfid);
        contentValues.put(FRIDGE_COLUMN_SHELFLIFE, shelfLife);
        contentValues.put(FRIDGE_COLUMN_AMOUNT, 0);

        db.insert(FRIDGE_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean addFridgeItem(FridgeItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_NAME, item.getName());
        contentValues.put(FRIDGE_COLUMN_RFID, item.getRFID());
        contentValues.put(FRIDGE_COLUMN_SHELFLIFE, item.getShelfLife());
        contentValues.put(FRIDGE_COLUMN_AMOUNT, item.getAmount());

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

    public FridgeItem getFridgeItemByRFID(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_RFID + " = " + "\""+rfid+"\"", null);
        FridgeItem item = null;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            int id = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_NAME));
            String shelfLife = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_SHELFLIFE));
            int count = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_AMOUNT));

            item = new FridgeItem(id, name, rfid, shelfLife, count);
        }

        return item;
    }


    public FridgeItem getFridgeItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_NAME + " = " + "\""+name+"\"", null);
        FridgeItem item = null;

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();

            int id = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_ID));
            String rfid = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_RFID));
            String shelfLife = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_SHELFLIFE));
            int count = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_AMOUNT));

            item = new FridgeItem(id, name, rfid, shelfLife, count);
        }

        return item;
    }

    public boolean isExistFridgeItemRFID(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_RFID + " = " + "\"" + rfid + "\"", null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }

    public boolean isExistFridgeItemName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + FRIDGE_TABLE_NAME + " where " + FRIDGE_COLUMN_NAME + " = " + "\"" + name + "\"", null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
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
        contentValues.put(FRIDGE_COLUMN_SHELFLIFE, item.getShelfLife());
        contentValues.put(FRIDGE_COLUMN_AMOUNT, item.getAmount());

        db.update(FRIDGE_TABLE_NAME, contentValues, "id = ? ", new String[]{Integer.toString(item.getID())});
        return true;
    }

    public boolean updateFrdgeItem(String rfid, int amount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_AMOUNT, amount);

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
            String shelfLife = cursor.getString(cursor.getColumnIndex(FRIDGE_COLUMN_SHELFLIFE));

            int amount = cursor.getInt(cursor.getColumnIndex(FRIDGE_COLUMN_AMOUNT));

            FridgeItem item = new FridgeItem(id, name, rfid, shelfLife, amount);
            arrayList.add(item);
            cursor.moveToNext();
        }
        return arrayList;
    }


    public boolean addUser(String name, String rfid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(USER_COLUMN_NAME, name);
        contentValues.put(USER_COLUMN_RFID, rfid);

        db.insert(USER_TABLE_NAME, null, contentValues);
        return true;
    }

    public boolean addUser(UserItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(FRIDGE_COLUMN_RFID, item.getRFID());

        db.insert(USER_TABLE_NAME, null, contentValues);
        return true;
    }


    public ArrayList<UserItem> getAllUser() {
        ArrayList arrayList = new ArrayList();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE_NAME, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        while (cursor.isAfterLast() == false) {
            int id = cursor.getInt(cursor.getColumnIndex(USER_COLUMN_ID));
            String name = cursor.getString(cursor.getColumnIndex(USER_COLUMN_NAME));
            String rfid = cursor.getString(cursor.getColumnIndex(USER_COLUMN_RFID));

            UserItem item = new UserItem(id, name, rfid);
            arrayList.add(item);
            cursor.moveToNext();
        }
        return arrayList;
    }


    public boolean deleteAllUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE_NAME, null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        while (cursor.isAfterLast() == false) {
            deleteFridgeItem(cursor.getColumnIndex(USER_COLUMN_ID));
            cursor.moveToNext();
        }

        return true;
    }

    public Integer deleteUser(String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(USER_TABLE_NAME, "name = ? ", new String[]{name});
    }

    public Integer deleteUser(UserItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(USER_TABLE_NAME, "id = ? ", new String[]{Integer.toString(item.getID())});
    }

    public boolean isExistRFID(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE_NAME + " where " + USER_COLUMN_RFID + " = " + "\"" + rfid + "\"", null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }

    public boolean isExistName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE_NAME + " where " + USER_COLUMN_NAME + " = " + "\"" + name + "\"", null);

        if (cursor != null && cursor.getCount() > 0) {
            return true;
        }
        return false;
    }

    public String getUserNameByRfid(String rfid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + USER_TABLE_NAME + " where " + USER_COLUMN_RFID + " = " + "\"" + rfid + "\"", null);

        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
        }
        String name = cursor.getString(cursor.getColumnIndex(USER_COLUMN_NAME));

        return name;
    }


}
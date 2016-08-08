package com.imes.iothome.database;

public class UserItem {
    private int mID;
    private String mName;
    private String mRFID;

    UserItem(int id, String name, String rfid) {
        mID = id;
        mName = name;
        mRFID = rfid;
    }
    public String getRFID() {
        return mRFID;
    }

    public void setRFID(String rfid) {
        this.mRFID = rfid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getID() {
        return mID;
    }

    public void setID(int id) {
        this.mID = id;
    }
}

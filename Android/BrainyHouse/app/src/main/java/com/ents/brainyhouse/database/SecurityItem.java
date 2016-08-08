package com.ents.brainyhouse.database;

public class SecurityItem {
    private int mID;
    private String mRFID;

    SecurityItem(int id, String rfid) {
        mID = id;
        mRFID = rfid;
    }
    public String getRFID() {
        return mRFID;
    }

    public void setRFID(String rfid) {
        this.mRFID = rfid;
    }

    public int getID() {
        return mID;
    }

    public void setID(int id) {
        this.mID = id;
    }
}

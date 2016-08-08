package com.ents.brainyhouse.database;

public class FridgeItem {
    private int mID;
    private String mName;
    private String mRFID;
    private int mCount;

    FridgeItem(int id, String name, String rfid, int count){
        mID = id;
        mName = name;
        mRFID = rfid;
        mCount = count;
    }

    public int getID() {
        return mID;
    }

    public void setID(int mId) {
        this.mID = mId;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getRFID() {
        return mRFID;
    }

    public void setRFID(String mRFID) {
        this.mRFID = mRFID;
    }

    public int getCount() {
        return mCount;
    }

    public void setCount(int mCount) {
        this.mCount = mCount;
    }



}

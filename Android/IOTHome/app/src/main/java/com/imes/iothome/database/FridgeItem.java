package com.imes.iothome.database;

public class FridgeItem {
    private int mID;
    private String mName;
    private String mRFID;
    private String mShelfLife;
    private int mAmount;

    FridgeItem(int id, String name, String rfid, String shelfLife, int amount){
        mID = id;
        mName = name;
        mRFID = rfid;
        mShelfLife = shelfLife;
        mAmount = amount;
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

    public String getShelfLife() {
        return mShelfLife;
    }

    public void setShelfLife(String shelfLife) {
        this.mShelfLife = shelfLife;
    }

    public int getAmount() {
        return mAmount;
    }

    public void setAmount(int amount) {
        this.mAmount = amount;
    }



}

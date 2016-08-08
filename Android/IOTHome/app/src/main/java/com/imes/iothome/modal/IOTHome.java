package com.imes.iothome.modal;

import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences;

import com.imes.iothome.database.DBHelper;

public class IOTHome {
    private final String TAG = IOTHome.class.getSimpleName();
    public volatile static IOTHome instance = null;

    public final static String DEVICE_1_NAME = "IOTHOM1";
    public final static String DEVICE_2_NAME = "IOTHOM2";

    public final static String FRAGMENT_DAHSBOARD = "DASHBOARD";
    public final static String FRAGMENT_LED_CONTROL = "LIGHT";
    public final static String FRAGMENT_AIRCON_CONTROL = "AIRCON";
    public final static String FRAGMENT_DOORLOCK_CONTROL = "DOORLOCK";
    public final static String FRAGMENT_CURTAIN_CONTROL = "CURTAIN";
    public final static String FRAGMENT_GAS_CONTROL = "GAS";
    public final static String FRAGMENT_CCTV_CONTROL = "CCTV";
    public final static String FRAGMENT_CCTV_CONNECT = "CCTV_CONNECT";
    public final static String FRAGMENT_FRIDGE_CONTROL = "FRIDGE";
    public final static String FRAGMENT_SECURITY_CONTROL = "SECURITY";
    public final static String FRAGMENT_SECURITY_LOCKED = "LOCKED";
    public final static String FRAGMENT_LOADING_POPUP = "LOADING";
    public final static String FRAGMENT_SETTING_USER = "USER_ㄴㄸㅆ쌰ㅜㅎ";
    public final static String FRAGMENT_SETTING_FRIDGE = "FRIDGE_SETTING";


    public static final String SHAREAD_DATA_NAME = "sharedData";
    public static final String SHAREAD_DATA_ADMIN_CARD= "adminID";

    private boolean mLEDStatus = false;
    private boolean mCurtainStatus = false;
    private boolean mFanStatus = false;
    private boolean mGasStatus = false;
    private boolean mGasValveStatus = true;
    private boolean mDoorStatus = false;
    private boolean mPirStatus = false;
    private boolean mBT1Status = false;
    private boolean mBT2Status = false;
    private String mCurrentUser = "Unknown";

    private String mLuxValue = "0";
    private String mCurrentTempValue = "0";
    private String mSettingTempValue = "0";
    private String mHumiValue = "0";

    private DBHelper mDBHelper = null;

    private String mAdminCardNumber = "null";

    public void setLuxValue(String value) {
        mLuxValue = value;
    }

    public String getLuxValue() {
        return mLuxValue;
    }

    public void setCurrentTempValue(String value) {
        mCurrentTempValue = value;
    }

    public String getCurrentTempValue() {
        return mCurrentTempValue;
    }

    public void setSettingTempValue(String value) {
        mSettingTempValue = value;
    }

    public String getSettingTempValue() {
        return mSettingTempValue;
    }

    public void setHumiValue(String value) {
        mHumiValue = value;
    }

    public String getHumiValue() {
        return mHumiValue;
    }

    public void setLEDStatus(boolean onOff) {
        mLEDStatus = onOff;
    }

    public boolean getLEDStatus() {
        return mLEDStatus;
    }

    public void setCurtainStatus(boolean onOff) {
        mCurtainStatus = onOff;
    }

    public boolean getCurtainStatus() {
        return mCurtainStatus;
    }

    public void setFanStatus(boolean onOff) {
        mFanStatus = onOff;
    }

    public boolean getFanStatus() {
        return mFanStatus;
    }

    public void setGasStatus(boolean onOff) {
        mGasStatus = onOff;
    }

    public boolean getGasStatus() {
        return mGasStatus;
    }

    public void setGasValveStatus(boolean onOff) {
        mGasValveStatus = onOff;
    }

    public boolean getGasValveStatus() {
        return mGasValveStatus;
    }

    public void setDoorStatus(boolean onOff) {
        mDoorStatus = onOff;
    }

    public boolean getDoorStatus() {
        return mDoorStatus;
    }

    public void setBT1Status(boolean onOff) {
        mBT1Status = onOff;
    }

    public boolean getBT1Status() {
        return mBT1Status;
    }

    public void setBT2Status(boolean onOff) {
        mBT2Status = onOff;
    }

    public boolean getBT2Status() {
        return mBT2Status;
    }

    public void setPirStatus(boolean onOff) {
        mPirStatus = onOff;
    }

    public boolean getPirStatus() {
        return mPirStatus;
    }

    public void setCurrentUser(String user) {
        mCurrentUser = user;
    }

    public String getCurrentUser() {
        return mCurrentUser;
    }

    public void setDBHelper(DBHelper dbHelper) {
        mDBHelper = dbHelper;
    }

    public DBHelper getDBHelper() {
        return mDBHelper;
    }

    public void setAdminCardNumber(Context context, String id) {
        mAdminCardNumber = id;

        SharedPreferences pref = context.getSharedPreferences(SHAREAD_DATA_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SHAREAD_DATA_ADMIN_CARD, id);
        editor.commit();
    }


    public String getAdminCardNumber(Context context)
    {
        SharedPreferences pref;

        if(mAdminCardNumber.equals("null"))
        {
            pref = context.getSharedPreferences(SHAREAD_DATA_NAME, Context.MODE_PRIVATE);
            mAdminCardNumber = pref.getString(SHAREAD_DATA_ADMIN_CARD, null);
        }

        return mAdminCardNumber;
    }

    public static IOTHome getInstance() {
        if (instance == null) {
            synchronized (IOTHome.class) {
                if (instance == null) {
                    instance = new IOTHome();
                }
            }
        }
        return instance;
    }

}

package com.ents.brainyhouse.modal;

import android.bluetooth.BluetoothManager;

public class BrainyHouse {
    private final String TAG = BrainyHouse.class.getSimpleName();
    public volatile static BrainyHouse instance = null;

    public final static String DEVICE_1_NAME = "Brainy1";
    public final static String DEVICE_2_NAME = "Brainy2";

    private BluetoothManager mBluetoothManager = null;

    public BluetoothManager getBluetoothManager() {
        return mBluetoothManager;
    }

    public void setBluetoothManager(BluetoothManager bluetoothManager) {
        mBluetoothManager = bluetoothManager;
    }

    public static BrainyHouse getInstance() {
        if (instance == null) {
            synchronized (BrainyHouse.class) {
                if (instance == null) {
                    instance = new BrainyHouse();
                }
            }
        }
        return instance;
    }

}

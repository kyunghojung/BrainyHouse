package com.ents.brainyhouse.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.ble.BleConnectionManager;
import com.ents.brainyhouse.ble.BleConnectionManagerDelegate;
import com.ents.brainyhouse.ble.BleMultiConnector;
import com.ents.brainyhouse.cam.CamConnectActivity;
import com.ents.brainyhouse.modal.BrainyHouse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class DashBoardActivity extends FragmentActivity
        implements BleConnectionManagerDelegate, ActivityCommunicator {

    public static final String TAG = DashBoardActivity.class.getSimpleName();;

    public final static String COMMAND_CONNECT_DEVICE = "com.ents.smarthome.COMMAND_CONNECT_DEVICE";
    public final static String COMMAND_CONNECTED_DEVICE = "com.ents.smarthome.COMMAND_CONNECTED_DEVICE";
    public final static String COMMAND_DISCONNECT_DEVICE = "com.ents.smarthome.COMMAND_DISCONNECT_DEVICE";
    public final static String COMMAND_DISCONNECTED_DEVICE = "com.ents.smarthome.COMMAND_DISCONNECTED_DEVICE";
    public final static String COMMAND_RECEIVE_DATA = "com.ents.smarthome.COMMAND_RECEIVE_DATA";
    public final static String COMMAND_SEND_DATA = "com.ents.smarthome.COMMAND_SEND_DATA";

    public final static String FRAGMENT_LIVINGROOM = "LIVINGROOM";
    public final static String FRAGMENT_KITCHEN = "KITCHEN";
    public final static String FRAGMENT_SECURITY = "SECURITY";
    public final static String FRAGMENT_FRIDGE = "FRIDGE";
    public final static String FRAGMENT_SETTING = "SETTING";
    public final static String FRAGMENT_FRIDGE_SETTING = "FRIDGE_SETTING";
    public final static String FRAGMENT_SECURITY_SETTING = "SECURITY_SETTING";

    private static final int REQUEST_ENABLE_BT = 1;

    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private Handler mScanHandler;

    private static final long SCAN_PERIOD = 10000; //10 seconds
    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mConnectingDevice = null;
    private BluetoothDevice mWaitToConnectDevice = null;
    private HashMap<String, Integer> devRssiValues = new HashMap<String, Integer>();;
    private int mState = UART_PROFILE_DISCONNECTED;
    private boolean isConnectingDevice = false;

    private BleConnectionManager mBleConnectionManager = null;
    private static BlockingQueue<ArrayList<Object>> mCommendQueue;
    private ArrayList<Object> mCommendArrayList;



    private Handler mHandler = new Handler();

    LivingRoomFragment mLivingroomFragment;
    KitchenFragment mKitchenFragment;
    SecurityFragment mSecurityFragment;
    FridgeFragment mFridgeFragment;
    SettingFragment mSettingFragment;
    SettingSecurityFragment mSettingSecurityFragment;
    SettingFridgeFragment mSettingFridgeFragment;

    Boolean mIsSettingMenu = false;

    private boolean initBle() {
        if (mBluetoothAdapter == null) {
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }

            BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();

            if (mBluetoothAdapter == null) {
                Toast.makeText(this, R.string.bluetooth_not_supported, Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private void startScanLeDevice(final boolean enable) {
        Log.d(TAG, "scanLeDevice() " + enable);

        this.setProgressBarIndeterminateVisibility(true);
        if (enable) {
            mScanHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    stopScanAndConnectDevice();
                }
            }, SCAN_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    private void stopScanAndConnectDevice() {
        this.setProgressBarIndeterminateVisibility(false);
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDevices(mDeviceList);
            }
        }, 2000);
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    addDevice(device, rssi);
                }
            });
        }
    };

    private void addDevice(BluetoothDevice device, int rssi) {
        boolean deviceFound = false;

        for (BluetoothDevice listDev : mDeviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }

        if (!deviceFound) {
            devRssiValues.put(device.getAddress(), rssi);
            Log.d(TAG, "Device Found! " + device.getName() + ", rssi: " + rssi);
            mDeviceList.add(device);
        }
    }


    Thread mCommendThread = new Thread() {
        public void run() {
            while(true) {
                try {
                    mCommendArrayList = mCommendQueue.take();
                    Log.d(TAG,"take commend commend: "+mCommendArrayList.get(0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    eventProcess(mCommendArrayList);
                }
            }
        }
    };

    private void eventProcess(ArrayList<Object> receivedArrayList) {
        String commend = (String) receivedArrayList.get(0);
        BluetoothDevice device = (BluetoothDevice) receivedArrayList.get(1);

        Log.d(TAG,"eventProcess: "+commend+", device: "+device.getName());
        switch (commend) {
            case COMMAND_CONNECT_DEVICE:
                Log.d(TAG, "COMMAND_CONNECT_DEVICE: device: " + device.getName());
                if(isConnectingDevice == true) {
                    Log.d(TAG, "isConnectingDevice add again " + device.getName());
                    mWaitToConnectDevice = device;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addCommand(COMMAND_CONNECT_DEVICE, mWaitToConnectDevice, null);
                            mWaitToConnectDevice = null;
                        }
                    }, 5000);
                }
                else {
                    isConnectingDevice = true;
                    mConnectingDevice = device;
                    mBleConnectionManager.connect(mConnectingDevice);
                }
                break;
            case COMMAND_DISCONNECT_DEVICE:
                Log.d(TAG, "COMMAND_DISCONNECT_DEVICE ");
                break;
            case COMMAND_RECEIVE_DATA:
                Log.d(TAG, "COMMAND_RECEIVE_DATA ");
                String data = (String) receivedArrayList.get(2);
                Log.d(TAG, "receive data: " + data);

                processMessage(device, data);
                break;
            case COMMAND_SEND_DATA:
                Log.d(TAG, "COMMAND_SEND_DATA ");
                break;

            default:
                break;
        }
    }

    public static void addCommand(String command, Object object1, Object object2){
        ArrayList<Object> commandArrayList = new ArrayList<Object>();
        commandArrayList.add(command);
        if(object1 != null) {
            commandArrayList.add(object1);
        }
        if(object2 != null) {
            commandArrayList.add(object2);
        }

        try {
            mCommendQueue.put(commandArrayList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Connected " + device.getName());
        Log.d(TAG, "isConnectingDevice " + isConnectingDevice);
        if(isConnectingDevice == true && mConnectingDevice.getAddress().equals(device.getAddress())) {
            mConnectingDevice = null;
            isConnectingDevice = false;
        }

        processMessage(device, COMMAND_CONNECTED_DEVICE);
    }

    @Override
    public void disconnected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Disonnected " + device.getName());
        processMessage(device, COMMAND_DISCONNECTED_DEVICE);
    }

    @Override
    public void failToConnect(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Fail To Connect!  " + device.getName());

    }

    @Override
    public void receivedData(BleConnectionManager manager, BluetoothDevice device, String data) {
        addCommand(COMMAND_RECEIVE_DATA, device, data);
    }

    public void connectDevices(ArrayList<BluetoothDevice> deviceList) {
        Log.d(TAG, "connectDevices: " + deviceList.size());
        for(BluetoothDevice device:deviceList) {
            Log.d(TAG,"COMMAND_CONNECT_DEVICE "+device.getName());
            addCommand(COMMAND_CONNECT_DEVICE, device, null);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.fragment_container);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLivingroomFragment = (LivingRoomFragment)getSupportFragmentManager().findFragmentById(R.id.livingroomFragment);
        mKitchenFragment = (KitchenFragment)getSupportFragmentManager().findFragmentById(R.id.kitchenFragment);
        mSecurityFragment = (SecurityFragment)getSupportFragmentManager().findFragmentById(R.id.securityFragment);
        mFridgeFragment = (FridgeFragment)getSupportFragmentManager().findFragmentById(R.id.fridgeFragment);

        // Bluetooth init
        initBle();
        mBleConnectionManager = new BleConnectionManager(this);
        BleMultiConnector.getInstance().setBleConnectionManager(mBleConnectionManager);
        mBleConnectionManager.setDelegate(this);

        mScanHandler = new Handler();

        mCommendQueue = new ArrayBlockingQueue<>(1024);
        mCommendThread.start();

    }

    @Override
    public boolean  onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.fragment_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                // Bluetooth On
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    startScanLeDevice(true);
                }
                return true;

            case R.id.menu_setting:
                mIsSettingMenu = true;
                FragmentManager fm = getSupportFragmentManager();
                mSettingFragment = new SettingFragment();
                mSettingFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                mSettingFragment.show(fm, "Setting");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();

                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        mBleConnectionManager.close();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            Log.d(TAG, "nRFUART's running in background. Disconnect to exit");
        }
        else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .setNegativeButton(R.string.popup_no, null)
                    .show();
        }
    }

    private void processMessage(BluetoothDevice device, String message) {
        switch(device.getName()) {
            case BrainyHouse.DEVICE_1_NAME:
                if(message.equals(COMMAND_CONNECTED_DEVICE) || message.equals(COMMAND_DISCONNECTED_DEVICE)) {
                    sendMessage(FRAGMENT_LIVINGROOM, message);
                    sendMessage(FRAGMENT_SECURITY, message);
                }
                else{
                    String datas[] = message.split(";");
                    if(datas[0].equals("L")) {
                        for (int i=1; i < datas.length; i++) {
                            sendMessage(FRAGMENT_LIVINGROOM,datas[i]);
                        }
                    }
                    else if(datas[0].equals("S")) {
                        for (int i=1; i < datas.length; i++) {
                            sendMessage(FRAGMENT_SECURITY,datas[i]);
                        }
                    }
                }
                break;
            case BrainyHouse.DEVICE_2_NAME:
                if(message.equals(COMMAND_CONNECTED_DEVICE) || message.equals(COMMAND_DISCONNECTED_DEVICE)) {
                    sendMessage(FRAGMENT_KITCHEN, message);
                    sendMessage(FRAGMENT_FRIDGE, message);
                }
                else{
                    String datas[] = message.split(";");
                    for(String data:datas){
                        if(datas[0].equals("K")) {
                            sendMessage(FRAGMENT_KITCHEN, data);
                        }
                        else if(datas[0].equals("F")) {
                            Log.d(TAG, "mIsSettingMenu: "+mIsSettingMenu+", data: "+data);
                            if(mIsSettingMenu == true) {
                                sendMessage(FRAGMENT_SETTING, data);
                            }
                            else {
                                sendMessage(FRAGMENT_FRIDGE, data);
                            }
                        }
                    }
                }
                break;
            default:
                break;
        }
    }

    private void sendMessage(String fragment, String data) {
        Log.d(TAG, "sendMessage() "+fragment+", "+data);
        switch(fragment) {
            case FRAGMENT_LIVINGROOM:
                mLivingroomFragment.setData(data);
                break;
            case FRAGMENT_KITCHEN:
                mKitchenFragment.setData(data);
                break;
            case FRAGMENT_SECURITY:
                mSecurityFragment.setData(data);
                break;
            case FRAGMENT_FRIDGE:
                mFridgeFragment.setData(data);
                break;
        }
    }

    @Override
    public void passDataToActivity(String fragment, String data) {
        Log.d(TAG, "passDataToActivity() "+fragment+", "+data);
        String datas[];
        switch(fragment) {
            case FRAGMENT_LIVINGROOM:
                datas = data.split("=");
                if(datas[0].equals("message")) {
                    mSecurityFragment.showMessage(datas[1]);
                    break;
                }

                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(BrainyHouse.DEVICE_1_NAME)) {
                        mBleConnectionManager.sendData(device, data);
                        break;
                    }
                }
                break;
            case FRAGMENT_KITCHEN:
                datas = data.split("=");
                if(datas[0].equals("message")) {
                    mFridgeFragment.showMessage(datas[1]);
                    break;
                }

                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(BrainyHouse.DEVICE_2_NAME)) {
                        mBleConnectionManager.sendData(device, data);
                        break;
                    }
                }
                break;
            case FRAGMENT_SECURITY:
                if(data.equals("StartCamera")) {
                    Intent intent = new Intent(this, CamConnectActivity.class);
                    startActivity(intent);
                    break;
                }

                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(BrainyHouse.DEVICE_1_NAME)) {
                        mBleConnectionManager.sendData(device, data);
                        break;
                    }
                }
                break;
            case FRAGMENT_FRIDGE:
                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(BrainyHouse.DEVICE_2_NAME)) {
                        mBleConnectionManager.sendData(device, data);
                        break;
                    }
                }
                break;

            case FRAGMENT_SETTING:
                if(data.equals("SecuritySetting")) {
                    mIsSettingMenu = true;
                    FragmentManager fm = getSupportFragmentManager();
                    mSettingSecurityFragment = new SettingSecurityFragment();
                    mSettingSecurityFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    mSettingSecurityFragment.show(fm, "Security Setting");
                }
                else if(data.equals("FridgeSetting")) {
                    mIsSettingMenu = true;
                    FragmentManager fm = getSupportFragmentManager();
                    mSettingFridgeFragment = new SettingFridgeFragment();
                    mSettingFridgeFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    mSettingFridgeFragment.show(fm, "Fridge Setting");
                }
                break;
            case FRAGMENT_FRIDGE_SETTING:
                break;
            case FRAGMENT_SECURITY_SETTING:
                break;
        }
    }
}

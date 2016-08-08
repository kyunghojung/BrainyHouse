package com.imes.iothome.ui;

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
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.imes.iothome.R;
import com.imes.iothome.ble.BleConnectionManager;
import com.imes.iothome.ble.BleConnectionManagerDelegate;
import com.imes.iothome.ble.BleMultiConnector;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.modal.IOTHome;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


public class ActivityMainFrame extends FragmentActivity
        implements BleConnectionManagerDelegate, ActivityCommunicator {

    public static final String TAG = ActivityMainFrame.class.getSimpleName();

    public final static String COMMAND_CONNECT_DEVICE = "com.ents.smarthome.COMMAND_CONNECT_DEVICE";
    public final static String COMMAND_CONNECTED_DEVICE = "com.ents.smarthome.COMMAND_CONNECTED_DEVICE";
    public final static String COMMAND_DISCONNECT_DEVICE = "com.ents.smarthome.COMMAND_DISCONNECT_DEVICE";
    public final static String COMMAND_DISCONNECTED_DEVICE = "com.ents.smarthome.COMMAND_DISCONNECTED_DEVICE";
    public final static String COMMAND_RECEIVE_DATA = "com.ents.smarthome.COMMAND_RECEIVE_DATA";
    public final static String COMMAND_SEND_DATA = "com.ents.smarthome.COMMAND_SEND_DATA";

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;

    private Handler mScanHandler;
    private static final long SCAN_PERIOD = 10000; //10 seconds

    private Handler mConnectHandler;
    private static final long CONNECT_PERIOD = 30000; //30 seconds

    private boolean mIsDevice1Connected = false;
    private boolean mIsDevice2Connected = false;

    private BluetoothAdapter mBluetoothAdapter = null;
    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();
    private BluetoothDevice mConnectingDevice = null;
    private BluetoothDevice mWaitToConnectDevice = null;
    private HashMap<String, Integer> devRssiValues = new HashMap<String, Integer>();
    ;
    private int mState = UART_PROFILE_DISCONNECTED;
    private boolean isConnectingDevice = false;

    private BleConnectionManager mBleConnectionManager = null;
    private static BlockingQueue<ArrayList<Object>> mCommandQueue;
    private ArrayList<Object> mCommandArrayList;

    private Handler mHandler = new Handler();

    private DBHelper mDBHelper;

    FragmentDashboard mFragmentDashboard;
    FragmentLEDControl mFragmentLightControl;
    FragmentAirconControl mFragmentAirconControl;
    FragmentDoorlockControl mFragmentDoorlockControl;
    FragmentCurtainControl mFragmentCurtainControl;
    FragmentGasControl mFragmentGasControl;
    FragmentCCTVControl mFragmentCctvControl;
    FragmentFridgeControl mFragmentFridgeControl;
    FragmentSecurityControl mFragmentSecurityControl;
    FragmentSecurityLocked mFragmentSecurityLocked;
    FragmentSettingUser mFragmentSettingUser;
    FragmentSettingFridge mFragmentSettingFridge;
    FragmentBTConnect mFragmentBTConnect;


    private ImageView mMenuHome;
    private ImageView mMenuLock;
    private ImageView mMenuLight;
    private ImageView mMenuAirCon;
    private ImageView mMenuDoorlock;
    private ImageView mMenuCurtain;
    private ImageView mMenuGas;
    private ImageView mMenuCctv;
    private ImageView mMenuFridge;
    private ImageView mMenuSecurity;
    private ImageView mSettingUser;

    private boolean mIsConnectedFirstTime = false;
    private String mCurrentFragment = "";

    private boolean mIsUnlocked = false;

    private String mLastMessage = "";

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

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            displayLodingFragment();
            mScanHandler.removeCallbacks(mScanHandlerCancel);
            mScanHandler.postDelayed(mScanHandlerCancel, SCAN_PERIOD);

            mConnectHandler.removeCallbacks(mConnectCancel);
            mConnectHandler.postDelayed(mConnectCancel, CONNECT_PERIOD);

            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    }

    Runnable mScanHandlerCancel = new Runnable() {
        @Override
        public void run() {
            stopScanAndConnectDevice();
        }
    };

    Runnable mConnectCancel = new Runnable() {
        @Override
        public void run() {
            stopConnectDevice();
        }
    };

    private void stopScanAndConnectDevice() {
        mBluetoothAdapter.stopLeScan(mLeScanCallback);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectDevices(mDeviceList);
            }
        }, 2000);
    }

    private void stopConnectDevice() {
        if(mIsDevice1Connected == false || mIsDevice1Connected == false){
            Log.d(TAG, "cannot connect devices!!!");
            notFoundDevice();
        }
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(device.getName() != null &&
                            (device.getName().equals(IOTHome.DEVICE_1_NAME) || device.getName().equals(IOTHome.DEVICE_2_NAME))) {
                        addDevice(device, rssi);
                    }
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


    Thread mCommandThread = new Thread() {
        public void run() {
            while (true) {
                try {
                    mCommandArrayList = mCommandQueue.take();
                    Log.d(TAG, "take command " + mCommandArrayList.get(0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    eventProcess(mCommandArrayList);
                }
            }
        }
    };

    private void eventProcess(ArrayList<Object> receivedArrayList) {
        String command = (String) receivedArrayList.get(0);
        BluetoothDevice device = (BluetoothDevice) receivedArrayList.get(1);

        Log.d(TAG, "eventProcess: " + command + ", device: " + device.getName());
        switch (command) {
            case COMMAND_CONNECT_DEVICE:
                Log.d(TAG, "COMMAND_CONNECT_DEVICE: device: " + device.getName());
                if (isConnectingDevice == true) {
                    Log.d(TAG, "isConnectingDevice add again " + device.getName());
                    mWaitToConnectDevice = device;
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            addCommand(COMMAND_CONNECT_DEVICE, mWaitToConnectDevice, null);
                            //mWaitToConnectDevice = null;
                        }
                    }, 5000);
                } else {
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

    public static void addCommand(String command, Object object1, Object object2) {
        Log.d(TAG, "addCommand command: "+command);

        ArrayList<Object> commandArrayList = new ArrayList<Object>();
        commandArrayList.add(command);
        if (object1 != null) {
            commandArrayList.add(object1);
        }
        if (object2 != null) {
            commandArrayList.add(object2);
        }

        try {
            mCommandQueue.put(commandArrayList);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "Device Connected " + device.getName());
        Log.d(TAG, "isConnectingDevice " + isConnectingDevice);
        if (isConnectingDevice == true && mConnectingDevice.getAddress().equals(device.getAddress())) {
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
        if (deviceList.size() == 0) {
            Log.d(TAG, "Device is not found!!!");
            notFoundDevice();
        }
        for (BluetoothDevice device : deviceList) {
            if(device.getName() != null) {
                Log.d(TAG, "COMMAND_CONNECT_DEVICE " + device.getName());
                addCommand(COMMAND_CONNECT_DEVICE, device, null);
            }
        }
    }

    private void notFoundDevice() {
        if (!isFinishing()) {
            if (mFragmentBTConnect != null) {
                mFragmentBTConnect.dismiss();
            }
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.retry_popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            changeFragment(IOTHome.FRAGMENT_LOADING_POPUP);
                            startScanLeDevice(true);
                        }
                    })
                    .setNegativeButton(R.string.popup_no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    })
                    .show();
        }
    }

    private void initializeMenuBar() {
        mMenuHome = (ImageView) this.findViewById(R.id.imageView_home);
        mMenuHome.setOnTouchListener(MenuHomeListener);

        mMenuLock = (ImageView) this.findViewById(R.id.imageView_lock);
        mMenuLock.setOnTouchListener(MenuLockListener);

        mMenuLight = (ImageView) this.findViewById(R.id.imageView_light);
        mMenuLight.setOnTouchListener(MenuLightListener);

        mMenuAirCon = (ImageView) this.findViewById(R.id.imageView_aircon);
        mMenuAirCon.setOnTouchListener(MenuAirConListener);

        mMenuDoorlock = (ImageView) this.findViewById(R.id.imageView_doorlock);
        mMenuDoorlock.setOnTouchListener(MenuDoorLockListener);

        mMenuCurtain = (ImageView) this.findViewById(R.id.imageView_curtain);
        mMenuCurtain.setOnTouchListener(MenuCurtainListener);

        mMenuGas = (ImageView) this.findViewById(R.id.imageView_gas);
        mMenuGas.setOnTouchListener(MenuGasListener);

        mMenuCctv = (ImageView) this.findViewById(R.id.imageView_cctv);
        mMenuCctv.setOnTouchListener(MenuCCTVListener);

        mMenuFridge = (ImageView) this.findViewById(R.id.imageView_fridge);
        mMenuFridge.setOnTouchListener(MenuFridgeListener);

        mMenuSecurity = (ImageView) this.findViewById(R.id.imageView_security);
        mMenuSecurity.setOnTouchListener(MenuSecurityListener);

        mSettingUser = (ImageView) this.findViewById(R.id.imageView_button_setting);
        mSettingUser.setOnClickListener(SettingUserListener);

    }

    private OnTouchListener MenuHomeListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "Touch Action: " + event.getAction());
            changeFragment(IOTHome.FRAGMENT_DAHSBOARD);
            return true;
        }
    };

    private OnTouchListener MenuLockListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            Log.d(TAG, "Touch Action: " + event.getAction());

            mIsUnlocked = false;
            changeFragment(IOTHome.FRAGMENT_SECURITY_LOCKED);
            return true;
        }
    };

    private OnTouchListener MenuLightListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            Log.d(TAG, "Touch Action: " + event.getAction());
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_light_on);
                changeFragment(IOTHome.FRAGMENT_LED_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_light_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuAirConListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_aircon_on);
                changeFragment(IOTHome.FRAGMENT_AIRCON_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_aircon_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuDoorLockListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_doorlock_on);
                changeFragment(IOTHome.FRAGMENT_DOORLOCK_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_doorlock_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuCurtainListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_curtain_on);
                changeFragment(IOTHome.FRAGMENT_CURTAIN_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_curtain_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuGasListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_gas_on);
                changeFragment(IOTHome.FRAGMENT_GAS_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_gas_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuCCTVListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_cctv_on);
                changeFragment(IOTHome.FRAGMENT_CCTV_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_cctv_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuFridgeListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_fridge_on);
                changeFragment(IOTHome.FRAGMENT_FRIDGE_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_fridge_off);
            }
            return true;
        }
    };

    private OnTouchListener MenuSecurityListener = new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            ImageView imageView = (ImageView) v;
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                imageView.setImageResource(R.drawable.menu_security_on);
                changeFragment(IOTHome.FRAGMENT_SECURITY_CONTROL);
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                imageView.setImageResource(R.drawable.menu_security_off);
            }
            return true;
        }
    };
    private View.OnClickListener SettingUserListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            changeFragment(IOTHome.FRAGMENT_SETTING_USER);
            return;
        }
    };

    public void dispTestFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        mFragmentFridgeControl = new FragmentFridgeControl();
        fragmentTransaction.add(R.id.contentFragment, mFragmentFridgeControl, "test");
        fragmentTransaction.commit();
    }

    private void displayLodingFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        mFragmentBTConnect = new FragmentBTConnect();
        mFragmentBTConnect.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mFragmentBTConnect.setCancelable(false);
        mFragmentBTConnect.show(fragmentManager, "BT Connect");

        mCurrentFragment = IOTHome.FRAGMENT_LOADING_POPUP;

    }

    private void changeFragment(String fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(mIsUnlocked == false && fragment != IOTHome.FRAGMENT_SECURITY_LOCKED) {
            Log.d(TAG, "is locked status, cannot change fragment!!!");
            return;
        }

        switch (fragment) {
            case IOTHome.FRAGMENT_SECURITY_LOCKED:
                mFragmentSecurityLocked = new FragmentSecurityLocked();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentSecurityLocked);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

                mCurrentFragment = IOTHome.FRAGMENT_SECURITY_LOCKED;
                break;

            case IOTHome.FRAGMENT_LOADING_POPUP:
                mFragmentBTConnect = new FragmentBTConnect();
                mFragmentBTConnect.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                mFragmentBTConnect.setCancelable(false);
                mFragmentBTConnect.show(fragmentManager, "BT Connect");

                mCurrentFragment = IOTHome.FRAGMENT_LOADING_POPUP;
                break;

            case IOTHome.FRAGMENT_DAHSBOARD:
                mFragmentDashboard = new FragmentDashboard();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentDashboard);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_DAHSBOARD;
                break;
            case IOTHome.FRAGMENT_LED_CONTROL:
                mFragmentLightControl = new FragmentLEDControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentLightControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_LED_CONTROL;
                break;
            case IOTHome.FRAGMENT_AIRCON_CONTROL:
                mFragmentAirconControl = new FragmentAirconControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentAirconControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_AIRCON_CONTROL;
                break;
            case IOTHome.FRAGMENT_DOORLOCK_CONTROL:
                mFragmentDoorlockControl = new FragmentDoorlockControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentDoorlockControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_DOORLOCK_CONTROL;
                break;
            case IOTHome.FRAGMENT_CURTAIN_CONTROL:
                mFragmentCurtainControl = new FragmentCurtainControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentCurtainControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_CURTAIN_CONTROL;
                break;
            case IOTHome.FRAGMENT_GAS_CONTROL:
                mFragmentGasControl = new FragmentGasControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentGasControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_GAS_CONTROL;
                break;
            case IOTHome.FRAGMENT_CCTV_CONTROL:
                mFragmentCctvControl = new FragmentCCTVControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentCctvControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_CCTV_CONTROL;
                break;
            case IOTHome.FRAGMENT_FRIDGE_CONTROL:
                mFragmentFridgeControl = new FragmentFridgeControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentFridgeControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_FRIDGE_CONTROL;
                break;
            case IOTHome.FRAGMENT_SECURITY_CONTROL:
                mFragmentSecurityControl = new FragmentSecurityControl();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentSecurityControl);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_SECURITY_CONTROL;
                break;
            case IOTHome.FRAGMENT_SETTING_USER:
                mFragmentSettingUser = new FragmentSettingUser();
                fragmentTransaction.replace(R.id.contentFragment, mFragmentSettingUser);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                mCurrentFragment = IOTHome.FRAGMENT_SETTING_USER;
                break;

            case IOTHome.FRAGMENT_SETTING_FRIDGE:
                mFragmentSettingFridge = new FragmentSettingFridge();
                mFragmentSettingFridge.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                mFragmentSettingFridge.show(fragmentManager, "Fridge Setting");
                mCurrentFragment = IOTHome.FRAGMENT_SETTING_FRIDGE;
            default:
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_home);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        initializeMenuBar();

        // Bluetooth init
        initBle();
        mBleConnectionManager = new BleConnectionManager(this);
        BleMultiConnector.getInstance().setBleConnectionManager(mBleConnectionManager);
        mBleConnectionManager.setDelegate(this);

        mScanHandler = new Handler();
        mConnectHandler = new Handler();

        mCommandQueue = new ArrayBlockingQueue<>(1024);
        mCommandThread.start();

        mDBHelper = new DBHelper(this);
        IOTHome.getInstance().setDBHelper(mDBHelper);

        startScanLeDevice(true);
        //dispTestFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.fragment_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
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
        Log.d(TAG, "onPause() CurrentFragment: " + mCurrentFragment);

        if (mCurrentFragment == IOTHome.FRAGMENT_LOADING_POPUP && mFragmentBTConnect != null) {
            Log.d(TAG, "onPause() mFragmentBTConnect.dismiss();");
            mFragmentBTConnect.dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBleConnectionManager.close();
    }

    @Override
    public void onBackPressed() {
        /*
        if (mState == UART_PROFILE_CONNECTED) {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            Log.d(TAG, "nRFUART's running in background. Disconnect to exit");
        }
        else */
        {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.popup_title)
                    .setMessage(R.string.exit_popup_message)
                    .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
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
        Log.d(TAG, "processMessage: " + message);
        switch (device.getName()) {
            case IOTHome.DEVICE_1_NAME:
                if (message.equals(COMMAND_CONNECTED_DEVICE)) {
                    mIsDevice1Connected = true;
                    IOTHome.getInstance().setBT1Status(mIsDevice1Connected);

                    Log.d(TAG, "Device 1 connected!!! mIsDevice2Connected: "+mIsDevice2Connected);
                    if (mIsConnectedFirstTime == false && mCurrentFragment.equals(IOTHome.FRAGMENT_LOADING_POPUP)) {
                        if(mIsDevice1Connected == true && mIsDevice2Connected == true) {
                            mFragmentBTConnect.dismiss();
                            changeFragment(IOTHome.FRAGMENT_SECURITY_LOCKED);
                        }
                    } else if (mCurrentFragment.equals(IOTHome.FRAGMENT_DAHSBOARD)) {
                        Log.d(TAG, "Device 1 disconnected!!!");
                        sendMessage(IOTHome.FRAGMENT_DAHSBOARD, COMMAND_CONNECTED_DEVICE + ":DEVICE1");
                    }
                } else if (message.equals(COMMAND_DISCONNECTED_DEVICE)) {
                    mIsDevice1Connected = false;
                    IOTHome.getInstance().setBT1Status(mIsDevice1Connected);

                    if (mCurrentFragment.equals(IOTHome.FRAGMENT_DAHSBOARD)) {
                        sendMessage(IOTHome.FRAGMENT_DAHSBOARD, COMMAND_DISCONNECTED_DEVICE + ":DEVICE1");
                    }
                } else {
                    analyzeMessage(message);
                }
                break;
            case IOTHome.DEVICE_2_NAME:
                if (message.equals(COMMAND_CONNECTED_DEVICE)) {
                    mIsDevice2Connected = true;
                    IOTHome.getInstance().setBT2Status(mIsDevice2Connected);

                    Log.d(TAG, "Device 2 connected!!! : "+mIsDevice1Connected);
                    if (mIsConnectedFirstTime == false && mCurrentFragment.equals(IOTHome.FRAGMENT_LOADING_POPUP)) {
                        if(mIsDevice1Connected == true && mIsDevice2Connected == true) {
                            mFragmentBTConnect.dismiss();
                            changeFragment(IOTHome.FRAGMENT_SECURITY_LOCKED);
                        }

                    } else if (mCurrentFragment.equals(IOTHome.FRAGMENT_DAHSBOARD)) {
                        Log.d(TAG, "Device 2 disconnected!!!");
                        sendMessage(IOTHome.FRAGMENT_DAHSBOARD, COMMAND_CONNECTED_DEVICE + ":DEVICE2");
                    }
                } else if (message.equals(COMMAND_DISCONNECTED_DEVICE)) {
                    mIsDevice2Connected = false;
                    IOTHome.getInstance().setBT2Status(mIsDevice2Connected);

                    if (mCurrentFragment.equals(IOTHome.FRAGMENT_DAHSBOARD)) {
                        sendMessage(IOTHome.FRAGMENT_DAHSBOARD, COMMAND_DISCONNECTED_DEVICE + ":DEVICE2");
                    }
                } else {
                    analyzeMessage(message);
                }
                break;
            default:
                break;
        }
    }

    private void sendMessage(String fragment, String data) {
        Log.d(TAG, "sendMessage() " + fragment + ", " + data);
        switch (fragment) {
            case IOTHome.FRAGMENT_DAHSBOARD:
                mFragmentDashboard.setData(data);
                break;
            case IOTHome.FRAGMENT_LED_CONTROL:
                mFragmentLightControl.setData(data);
                break;
            case IOTHome.FRAGMENT_AIRCON_CONTROL:
                mFragmentAirconControl.setData(data);
                break;
            case IOTHome.FRAGMENT_DOORLOCK_CONTROL:
                mFragmentDoorlockControl.setData(data);
                break;
            case IOTHome.FRAGMENT_CURTAIN_CONTROL:
                mFragmentCurtainControl.setData(data);
                break;
            case IOTHome.FRAGMENT_GAS_CONTROL:
                mFragmentGasControl.setData(data);
                break;
            case IOTHome.FRAGMENT_CCTV_CONTROL:
                mFragmentCctvControl.setData(data);
                break;
            case IOTHome.FRAGMENT_FRIDGE_CONTROL:
                mFragmentFridgeControl.setData(data);
                break;
            case IOTHome.FRAGMENT_SETTING_FRIDGE:
                mFragmentSettingFridge.setData(data);
                break;
            case IOTHome.FRAGMENT_SECURITY_CONTROL:
                mFragmentSecurityControl.setData(data);
                break;
            case IOTHome.FRAGMENT_SECURITY_LOCKED:
                mFragmentSecurityLocked.setData(data);
                break;
            case IOTHome.FRAGMENT_SETTING_USER:
                mFragmentSettingUser.setData(data);
                break;
        }
    }

    @Override
    public void passDataToActivity(String fragment, String data) {
        Log.d(TAG, "passDataToActivity() " + fragment + ", " + data);
        String datas[];
        switch (fragment) {
            case IOTHome.FRAGMENT_SECURITY_LOCKED:
                if(data.equals("unlocked")){
                    mIsUnlocked = true;
                    changeFragment(IOTHome.FRAGMENT_DAHSBOARD);
                }
                break;
            case IOTHome.FRAGMENT_LED_CONTROL:
            case IOTHome.FRAGMENT_AIRCON_CONTROL:
            case IOTHome.FRAGMENT_DOORLOCK_CONTROL:
            case IOTHome.FRAGMENT_CURTAIN_CONTROL:
            case IOTHome.FRAGMENT_CCTV_CONTROL:
            case IOTHome.FRAGMENT_SECURITY_CONTROL:
                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                        mBleConnectionManager.sendData(device, data);
                        break;
                    }
                }
                break;
            case IOTHome.FRAGMENT_GAS_CONTROL:
                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(IOTHome.DEVICE_2_NAME)) {
                        mBleConnectionManager.sendData(device, data);
                        break;
                    }
                }
                break;
            case IOTHome.FRAGMENT_CCTV_CONNECT:
                if(data.startsWith("rtsp://")) {
                    sendMessage(IOTHome.FRAGMENT_CCTV_CONTROL, data);
                }
                break;
            case IOTHome.FRAGMENT_FRIDGE_CONTROL:
                if(data.equals("DisplaySetting")) {
                    changeFragment((IOTHome.FRAGMENT_SETTING_FRIDGE));
                } else {
                    for (BluetoothDevice device : mDeviceList) {
                        if (device.getName().equals(IOTHome.DEVICE_2_NAME)) {
                            mBleConnectionManager.sendData(device, data);
                            break;
                        }
                    }
                }
                break;
            case IOTHome.FRAGMENT_SETTING_FRIDGE:
                if(data.equals("ChangeSetting")) {
                    changeFragment((IOTHome.FRAGMENT_FRIDGE_CONTROL));
                }
                break;
        }
    }

    private void analyzeMessage(String message) {
        String datas[] = message.split(";");
        Log.d(TAG, "analyzeMessage() message: "+message);

        if(message.equals(mLastMessage)) {
            Log.d(TAG, "This is a duplicated message." );
            return;
        }

        mLastMessage = message;

        if (datas.length < 2) {
            Log.d(TAG, "is wrong message: " );
            return;
        }

        autoRunMonitoring(message);

        if (datas[0].equals("L")) {
            if(mCurrentFragment == IOTHome.FRAGMENT_DAHSBOARD) {
                if (datas[1].startsWith("L") || datas[1].startsWith("T") || datas[1].startsWith("H")
                        || datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_DAHSBOARD, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_LED_CONTROL) {
                if (datas[1].startsWith("L") || datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_LED_CONTROL, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_AIRCON_CONTROL) {
                if(datas[1].startsWith("T")) {
                    sendMessage(IOTHome.FRAGMENT_AIRCON_CONTROL, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_CURTAIN_CONTROL) {
                if(datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_CURTAIN_CONTROL, datas[1]);
                }
            }
        } else if (datas[0].equals("S")) {
            if(mCurrentFragment == IOTHome.FRAGMENT_SECURITY_LOCKED) {
                if(datas[1].startsWith("RF")) {
                    sendMessage(IOTHome.FRAGMENT_SECURITY_LOCKED, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_DAHSBOARD) {
                if(datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_DAHSBOARD, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_AIRCON_CONTROL) {
                if(datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_AIRCON_CONTROL, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_DOORLOCK_CONTROL) {
                if(datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_DOORLOCK_CONTROL, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_SECURITY_CONTROL) {
                if(datas[1].startsWith("I")) {
                    sendMessage(IOTHome.FRAGMENT_SECURITY_CONTROL, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_CCTV_CONTROL) {
                if(datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_CCTV_CONTROL, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_SETTING_USER) {
                if(datas[1].startsWith("RF")) {
                    sendMessage(IOTHome.FRAGMENT_SETTING_USER, datas[1]);
                }
            }
        } else if (datas[0].equals("K")) {
            if (mCurrentFragment == IOTHome.FRAGMENT_DAHSBOARD) {
                if(datas[1].startsWith("G")) {
                    sendMessage(IOTHome.FRAGMENT_DAHSBOARD, datas[1]);
                }
            } else if(mCurrentFragment == IOTHome.FRAGMENT_GAS_CONTROL) {
                if (datas[1].startsWith("OK")) {
                    sendMessage(IOTHome.FRAGMENT_GAS_CONTROL, datas[1]);
                }
            }
        } else if (datas[0].equals("F")) {
            if (mCurrentFragment == IOTHome.FRAGMENT_FRIDGE_CONTROL) {
                if (datas[1].startsWith("RF") || datas[1].equals("BY") || datas[1].equals("BR")) {
                    sendMessage(IOTHome.FRAGMENT_FRIDGE_CONTROL, datas[1]);
                }

            }
            else if(mCurrentFragment == IOTHome.FRAGMENT_SETTING_FRIDGE) {
                if (datas[1].startsWith("RF")) {
                    sendMessage(IOTHome.FRAGMENT_SETTING_FRIDGE, datas[1]);
                }
            }
        }
    }

    private void autoRunMonitoring(String message) {
        if(message.startsWith("L;T:")) {
            String datas[] = message.split(":");
            try {
                float currentTemp = Float.parseFloat(datas[1]);
                float settingTemp = Float.parseFloat(IOTHome.getInstance().getSettingTempValue());

                Log.d(TAG, "Current temp: " + currentTemp + "Setting temp:"+ settingTemp);

                IOTHome.getInstance().setCurrentTempValue(datas[1]);

                if(settingTemp == 0.0) {
                    return;
                }

                if((IOTHome.getInstance().getFanStatus() == false)
                        && currentTemp > settingTemp) {  //Fan On
                    Log.d(TAG, "Fan ON!!!!!");
                    for (BluetoothDevice device : mDeviceList) {
                        if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                            mBleConnectionManager.sendData(device, "FN");
                            IOTHome.getInstance().setFanStatus(true);
                            break;
                        }
                    }
                } else if((IOTHome.getInstance().getFanStatus() == true)
                        && currentTemp < settingTemp) {  //Fan OFF
                    Log.d(TAG, "Fan OFF!!!!!");
                    for (BluetoothDevice device : mDeviceList) {
                        if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                            mBleConnectionManager.sendData(device, "FF");
                            IOTHome.getInstance().setFanStatus(false);
                            break;
                        }
                    }
                }
            }
            catch (NumberFormatException e) {
                IOTHome.getInstance().setCurrentTempValue("0");
                IOTHome.getInstance().setSettingTempValue("0");
                e.printStackTrace();
            }
        }
        if(message.startsWith("L;H:")) {
            String datas[] = message.split(":");
            try {
                float currentHumi = Float.parseFloat(datas[1]);
                if(currentHumi < 0) {
                    IOTHome.getInstance().setHumiValue("0");
                } else {
                    IOTHome.getInstance().setHumiValue(datas[1]);
                }

            }
            catch (NumberFormatException e) {
                IOTHome.getInstance().setHumiValue("0");
                e.printStackTrace();
            }
        }
        if(message.startsWith("L;L:")) {
            String datas[] = message.split(":");
            try {
                int currentLux = Integer.parseInt(datas[1]);
                if(currentLux < 0) {
                    IOTHome.getInstance().setLuxValue("0");
                } else if(currentLux < 50) {
                    if(IOTHome.getInstance().getLEDStatus() == false) {
                        IOTHome.getInstance().setLuxValue(datas[1]);
                        for (BluetoothDevice device : mDeviceList) {
                            if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                                mBleConnectionManager.sendData(device, "LN");
                                IOTHome.getInstance().setLEDStatus(true);
                                break;
                            }
                        }
                    }
                    if(IOTHome.getInstance().getCurtainStatus() == false) {
                        for (BluetoothDevice device : mDeviceList) {
                            if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                                mBleConnectionManager.sendData(device, "CO");
                                IOTHome.getInstance().setCurtainStatus(true);
                                break;
                            }
                        }
                    }

                } else if(currentLux >= 50) {
                    if(IOTHome.getInstance().getLEDStatus() == true) {
                        IOTHome.getInstance().setLuxValue(datas[1]);
                        for (BluetoothDevice device : mDeviceList) {
                            if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                                mBleConnectionManager.sendData(device, "LF");
                                IOTHome.getInstance().setLEDStatus(false);
                                break;
                            }
                        }
                    }

                    if(IOTHome.getInstance().getCurtainStatus() == true) {
                        for (BluetoothDevice device : mDeviceList) {
                            if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                                mBleConnectionManager.sendData(device, "CC");
                                IOTHome.getInstance().setCurtainStatus(false);
                                break;
                            }
                        }
                    }
                }

            }
            catch (NumberFormatException e) {
                IOTHome.getInstance().setLuxValue("0");
                e.printStackTrace();
            }
        }
        if(message.startsWith("K;G:")) {
            String datas[] = message.split(":");
            if (datas[1].equals("1")
                    && IOTHome.getInstance().getGasValveStatus() == true) {
                IOTHome.getInstance().setGasStatus(true);
                IOTHome.getInstance().setGasValveStatus(false);

                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(IOTHome.DEVICE_2_NAME)) {
                        mBleConnectionManager.sendData(device, "VC");
                        IOTHome.getInstance().setGasValveStatus(false);
                        break;
                    }
                }
            }

            if (datas[1].equals("0")) {
                IOTHome.getInstance().setGasStatus(false);
            }
        }
        if(message.startsWith("S;I:")) {
            String datas[] = message.split(":");
            if (datas[1].equals("1")
                    && IOTHome.getInstance().getDoorStatus() == true) {
                for (BluetoothDevice device : mDeviceList) {
                    if (device.getName().equals(IOTHome.DEVICE_1_NAME)) {
                        mBleConnectionManager.sendData(device, "DC");
                        IOTHome.getInstance().setDoorStatus(false);
                        break;
                    }
                }
            }
        }
    }
}

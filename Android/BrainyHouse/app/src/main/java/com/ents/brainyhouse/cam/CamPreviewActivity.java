package com.ents.brainyhouse.cam;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup.LayoutParams;
import android.widget.Toast;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.ble.BleConnectionManager;
import com.ents.brainyhouse.ble.BleConnectionManagerDelegate;
import com.ents.brainyhouse.ble.BleMultiConnector;
import com.ents.brainyhouse.joystick.JoystickMovedListener;
import com.ents.brainyhouse.modal.BrainyHouse;
import com.ents.brainyhouse.ui.ActivityCommunicator;
import com.ents.brainyhouse.joystick.JoystickView;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

public class CamPreviewActivity extends Activity implements SurfaceHolder.Callback,
        IVideoPlayer, BleConnectionManagerDelegate, ActivityCommunicator {
    public final static String TAG = CamPreviewActivity.class.getSimpleName();

    public final static String LOCATION = "com.ents.brainyhouse.cam.location";

    private String mFilePath;

    // display surface
    private SurfaceView mSurface;
    private SurfaceHolder holder;

    // media player
    private LibVLC libvlc;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    BrainyHouse mBrainyHouse = BrainyHouse.getInstance();
    BleConnectionManager mBleConnectionManager = null;

    private JoystickView mJoystickView;

    /*************
     * Activity
     *************/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // Receive path to play from intent
        Intent intent = getIntent();
        mFilePath = intent.getExtras().getString(LOCATION);

        Log.d(TAG, "Playing back " + mFilePath);

        mSurface = (SurfaceView) findViewById(R.id.surface);
        holder = mSurface.getHolder();
        holder.addCallback(this);

        mJoystickView = (JoystickView)findViewById(R.id.joystickView_controler);
        mJoystickView.setOnJostickMovedListener(mJoystickMovedListener);

        mBleConnectionManager = BleMultiConnector.getInstance().getBleConnectionManager();
        if(mBleConnectionManager.isConnected(BrainyHouse.DEVICE_1_NAME)) {
            mBleConnectionManager.switchContext(this);
            mBleConnectionManager.setDelegate(this);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSize(mVideoWidth, mVideoHeight);
    }

    @Override
    protected void onResume() {
        super.onResume();
        createPlayer(mFilePath);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    private JoystickMovedListener mJoystickMovedListener = new JoystickMovedListener()
    {
        @Override
        public void OnMoved(int pan, int tilt)
        {
            Move(pan, tilt);
        }

        public void Move(int pan, int tilt)
        {
            // limit to {0..10}
            int radius = (byte) ( Math.min( Math.sqrt((pan*pan) + (tilt*tilt)), 10.0 ) );
            // scale to {0..35}
            int angle = (byte) ( Math.atan2(-pan, -tilt) * 18.0 / Math.PI + 36.0 + 0.5 );

            if( angle >= 36 )
                angle = (byte)(angle-36);

            if((angle >= 0 && angle < 5) || (angle >= 32 && angle < 36)) {
                Log.d(TAG, "Move Up ");
            }
            else if(angle >= 5 && angle < 14) {
                Log.d(TAG, "Move Left ");
            }
            else if(angle >= 14 && angle < 23) {
                Log.d(TAG, "Move Down ");
            }
            else if(angle >= 23 && angle < 32) {
                Log.d(TAG, "Move Right ");
            }
            else {
                Log.d(TAG, "Move Center ");
            }
        }

        @Override
        public void OnReleased()
        {

        }

        @Override
        public void OnReturnedToCenter()
        {

        };
    };

    // Bluetooth
    @Override
    public void passDataToActivity(String fragment, String data) {

        Log.d(TAG, "passDataToActivity: data: "+data);
    }

    @Override
    public void connected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "connected: device: "+device.getName());

    }

    @Override
    public void disconnected(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "disconnected: device: "+device.getName());
    }

    @Override
    public void failToConnect(BleConnectionManager manager, BluetoothDevice device) {
        Log.d(TAG, "failToConnect: device: "+device.getName());
    }

    @Override
    public void receivedData(BleConnectionManager manager, BluetoothDevice device, String data) {
        Log.d(TAG, "receivedData: " + device.getName() + ", data: " + data);
    }

    /*************
     * Surface
     *************/

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format,
                               int width, int height) {
        Log.d(TAG, "surfaceChanged");
        if (libvlc != null)
            libvlc.attachSurface(holder.getSurface(), this);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed");
    }

    private void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        // get screen size
        int w = getWindow().getDecorView().getWidth();
        int h = getWindow().getDecorView().getHeight();

        // getWindow().getDecorView() doesn't always take orientation into
        // account, we have to correct the values
        boolean isPortrait = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        if (w > h && isPortrait || w < h && !isPortrait) {
            int i = w;
            w = h;
            h = i;
        }

        float videoAR = (float) mVideoWidth / (float) mVideoHeight;
        float screenAR = (float) w / (float) h;

        if (screenAR < videoAR)
            h = (int) (w / videoAR);
        else
            w = (int) (h * videoAR);

        // force surface buffer size
        holder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        LayoutParams lp = mSurface.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mSurface.setLayoutParams(lp);
        mSurface.invalidate();
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width,
                               int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    /*************
     * Player
     *************/

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Toast toast = Toast.makeText(this, media, Toast.LENGTH_LONG);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
                        0);
                toast.show();
            }

            // Create a new media player
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
            LibVLC.restart(this);
            EventHandler.getInstance().addHandler(mHandler);
            holder.setFormat(PixelFormat.RGBX_8888);
            holder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
            Toast.makeText(this, "Error creating player!", Toast.LENGTH_LONG).show();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        EventHandler.getInstance().removeHandler(mHandler);
        libvlc.stop();
        libvlc.detachSurface();
        holder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /*************
     * Events
     *************/

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<CamPreviewActivity> mOwner;

        public MyHandler(CamPreviewActivity owner) {
            mOwner = new WeakReference<CamPreviewActivity>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            CamPreviewActivity player = mOwner.get();

            // SamplePlayer events
            if (msg.what == VideoSizeChanged) {
                player.setSize(msg.arg1, msg.arg2);
                return;
            }

            // Libvlc events
            Bundle b = msg.getData();
            switch (b.getInt("event")) {
                case EventHandler.MediaPlayerEndReached:
                    player.releasePlayer();
                    break;
                case EventHandler.MediaPlayerPlaying:
                case EventHandler.MediaPlayerPaused:
                case EventHandler.MediaPlayerStopped:
                default:
                    break;
            }
        }
    }
}

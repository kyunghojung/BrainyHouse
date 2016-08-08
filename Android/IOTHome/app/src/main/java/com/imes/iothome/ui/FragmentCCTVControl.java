package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

import org.videolan.libvlc.EventHandler;
import org.videolan.libvlc.IVideoPlayer;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaList;

import java.lang.ref.WeakReference;

public class FragmentCCTVControl extends Fragment implements SurfaceHolder.Callback,
        IVideoPlayer {

    public static final String TAG = FragmentCCTVControl.class.getSimpleName();

    Context mContext;

    private ActivityCommunicator mActivityCommunicator;
    private View mView;

    public final static String LOCATION = "com.imes.iothome.cam.location";

    private String mFilePath;

    // display surface
    private SurfaceView mCctvPreview;
    private SurfaceHolder mSurfaceHolder;

    private ImageView mCctvEdge;

    private ImageView mCctvMoveUp;
    private ImageView mCctvMoveDown;
    private ImageView mCctvMoveLeft;
    private ImageView mCctvMoveRight;

    // media player
    private LibVLC libvlc;
    private int mVideoWidth;
    private int mVideoHeight;
    private final static int VideoSizeChanged = -1;

    FragmentCamConnect mCamConnectFragment;

    public FragmentCCTVControl() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mContext = getActivity();
        mActivityCommunicator = (ActivityCommunicator) mContext;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_cctv_control, container, false);

        mCctvEdge = (ImageView)  mView.findViewById(R.id.imageView_cctv_preview_edge);

        mCctvMoveUp = (ImageView)  mView.findViewById(R.id.imageView_cctv_arrow_up);
        mCctvMoveDown = (ImageView)  mView.findViewById(R.id.imageView_cctv_arrow_down);
        mCctvMoveLeft = (ImageView)  mView.findViewById(R.id.imageView_cctv_arrow_left);
        mCctvMoveRight = (ImageView)  mView.findViewById(R.id.imageView_cctv_arrow_right);


        mCctvMoveUp.setOnClickListener(CCTVMoveUpListener);
        mCctvMoveDown.setOnClickListener(CCTVMoveDownListener);
        mCctvMoveLeft.setOnClickListener(CCTVMoveLeftListener);
        mCctvMoveRight.setOnClickListener(CCTVMoveRightListener);

        mCctvPreview = (SurfaceView) mView.findViewById(R.id.surfaceView_cctv);

        int previewWidth = mCctvEdge.getDrawable().getIntrinsicWidth() - 100;
        int previewHeight = previewWidth * 9 / 16;

        Log.d(TAG, "previewWidth: " + previewWidth + ", previewHeight: " + previewHeight);

        mCctvPreview.getHolder().setFixedSize(previewWidth, previewHeight);

        mSurfaceHolder = mCctvPreview.getHolder();
        mSurfaceHolder.addCallback(this);

        FragmentManager fragmentManager = getFragmentManager();
        mCamConnectFragment = new FragmentCamConnect();
        mCamConnectFragment.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mCamConnectFragment.show(fragmentManager, "Cam Connect");

        return mView;
    }

    private View.OnClickListener CCTVMoveUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMessage("camUp");
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_CCTV_CONTROL, "AU");
            return;
        }
    };

    private View.OnClickListener CCTVMoveDownListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMessage("camDown");
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_CCTV_CONTROL, "AD");
            return;
        }
    };

    private View.OnClickListener CCTVMoveLeftListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMessage("camLeft");
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_CCTV_CONTROL, "AL");
            return;
        }
    };

    private View.OnClickListener CCTVMoveRightListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            showMessage("camRight");
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_CCTV_CONTROL, "AR");
            return;
        }
    };

    @Override
    public void onDestroyView () {
        super.onDestroyView();
        releasePlayer();
    }

    @Override
    public void onDetach () {
        super.onDetach();

    }

    public void setData(final String data) {

        if(data.startsWith("rtsp://")) {
            mFilePath = data;
            createPlayer(mFilePath);
            Log.d(TAG, "Playing back " + mFilePath);
        } else if (data.startsWith("OK")) {
            String tempStr[] = data.split(":");
            if(tempStr.length < 2) {
                return;
            }
            if (tempStr[1].equals("AU") || tempStr[1].equals("AD")
                    || tempStr[1].equals("AL") || tempStr[1].equals("AR")) {

                Log.d(TAG, "Move " + tempStr[1]);
            }
        }
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }

    private void createPlayer(String media) {
        releasePlayer();
        try {
            if (media.length() > 0) {
                Log.d(TAG,media);
            }

            // Create a new media player
            libvlc = LibVLC.getInstance();
            libvlc.setHardwareAcceleration(LibVLC.HW_ACCELERATION_DISABLED);
            libvlc.setSubtitlesEncoding("");
            libvlc.setAout(LibVLC.AOUT_OPENSLES);
            libvlc.setTimeStretching(true);
            libvlc.setChroma("RV32");
            libvlc.setVerboseMode(true);
            LibVLC.restart(mContext);
            EventHandler.getInstance().addHandler(mHandler);
            mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
            mSurfaceHolder.setKeepScreenOn(true);
            MediaList list = libvlc.getMediaList();
            list.clear();
            list.add(new Media(libvlc, LibVLC.PathToURI(media)), false);
            libvlc.playIndex(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void releasePlayer() {
        if (libvlc == null)
            return;
        EventHandler.getInstance().removeHandler(mHandler);
        libvlc.stop();
        libvlc.detachSurface();
        mSurfaceHolder = null;
        libvlc.closeAout();
        libvlc.destroy();
        libvlc = null;

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    /*************
     * Events
     *************/

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int format,
                               int width, int height) {
        Log.d(TAG, "surfaceChanged");
        if (libvlc != null)
            libvlc.attachSurface(mSurfaceHolder.getSurface(), this);
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        Log.d(TAG, "surfaceDestroyed");
    }

    public void setSize(int width, int height) {
        mVideoWidth = width;
        mVideoHeight = height;
        if (mVideoWidth * mVideoHeight <= 1)
            return;

        // get screen size
        int w = mCctvPreview.getWidth();
        int h = mCctvPreview.getHeight();

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
        mSurfaceHolder.setFixedSize(mVideoWidth, mVideoHeight);

        // set display size
        ViewGroup.LayoutParams lp = mCctvPreview.getLayoutParams();
        lp.width = w;
        lp.height = h;
        mCctvPreview.setLayoutParams(lp);
        mCctvPreview.invalidate();
    }

    @Override
    public void setSurfaceSize(int width, int height, int visible_width,
                               int visible_height, int sar_num, int sar_den) {
        Message msg = Message.obtain(mHandler, VideoSizeChanged, width, height);
        msg.sendToTarget();
    }

    private Handler mHandler = new MyHandler(this);

    private static class MyHandler extends Handler {
        private WeakReference<FragmentCCTVControl> mOwner;

        public MyHandler(FragmentCCTVControl owner) {
            mOwner = new WeakReference<FragmentCCTVControl>(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            FragmentCCTVControl player = mOwner.get();

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

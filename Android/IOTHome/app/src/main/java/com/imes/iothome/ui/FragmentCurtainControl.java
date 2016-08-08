package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

public class FragmentCurtainControl extends Fragment {

    public static final String TAG = FragmentCurtainControl.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;

    private AnimationDrawable mFrameAnimation;

    private ImageView mCurtainControlMain;
    private ImageView mCurtainControlButton;

    private boolean mCurtainStatus = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_curtain_control, container, false);

        mCurtainControlMain = (ImageView) mView.findViewById(R.id.imageView_curtain_control_main);

        mCurtainControlButton = (ImageView) mView.findViewById(R.id.imageView_curtain_control_button);
        mCurtainControlButton.setOnClickListener(CurtainControlListener);

        mCurtainStatus = IOTHome.getInstance().getCurtainStatus();
        if(mCurtainStatus == true) {
            mCurtainControlMain.setImageResource(R.drawable.curtain_ani_4);
            mCurtainControlButton.setImageResource(R.drawable.switch_vertical_open);
        }
        else {
            mCurtainControlMain.setImageResource(R.drawable.curtain_ani_1);
            mCurtainControlButton.setImageResource(R.drawable.switch_vertical_close);
        }

        //setTurnOnOff(mCurtainStatus);

        return mView;
    }

    private View.OnClickListener CurtainControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mCurtainStatus) {
                showMessage("Curtain Close");
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_CURTAIN_CONTROL, "CC");
            } else {
                showMessage("Curtain Open");
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_CURTAIN_CONTROL, "CO");
            }
            return;
        }
    };

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Context context = getActivity();
        mActivityCommunicator = (ActivityCommunicator) context;
    }

    public void setData(final String data) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showMessage(data);

                if (data.equals(ActivityMainFrame.COMMAND_CONNECTED_DEVICE)) {

                }
                else if (data.equals(ActivityMainFrame.COMMAND_DISCONNECTED_DEVICE)) {
                }
                else if (data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("CO")) {
                        mCurtainStatus = true;
                        IOTHome.getInstance().setCurtainStatus(mCurtainStatus);
                    } else if (tempStr[1].equals("CC")) {
                        mCurtainStatus = false;
                        IOTHome.getInstance().setCurtainStatus(mCurtainStatus);
                    }
                    setTurnOnOff(mCurtainStatus);
                }
            }
        });
    }
    private void setTurnOnOff(boolean onOff) {
        mCurtainControlMain.setImageResource(0);
        if(onOff) {
            mCurtainControlMain.setBackgroundResource(R.drawable.curtain_animation_open);
            mCurtainControlButton.setImageResource(R.drawable.switch_vertical_open);
        }
        else {
            mCurtainControlMain.setBackgroundResource(R.drawable.curtain_animation_close);
            mCurtainControlButton.setImageResource(R.drawable.switch_vertical_close);
        }
        mFrameAnimation = (AnimationDrawable) mCurtainControlMain.getBackground();

        mCurtainControlMain.setVisibility(View.VISIBLE);
        mFrameAnimation.setOneShot(true);
        mFrameAnimation.start();
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

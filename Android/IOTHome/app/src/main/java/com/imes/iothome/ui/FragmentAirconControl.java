package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

public class FragmentAirconControl extends Fragment {

    public static final String TAG = FragmentAirconControl.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;
    private ImageView mAirconControlMain;
    private ImageView mAirconControlButton;
    private ImageView mAirconSettingUp;
    private ImageView mAirconSettingDown;
    private TextView mTextViewCurrentTemp;
    private TextView mTextViewSettingTemp;
    private String mCurrentTemp = "0";
    private boolean mFanStatus = false;
    private String mSettingTemp = "0";
    private float mFloatCurrentTemp;
    private int mIntSettingTemp = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_aircon_control, container, false);

        mAirconControlMain = (ImageView) mView.findViewById(R.id.imageView_airconditioning);
        mAirconControlButton = (ImageView) mView.findViewById(R.id.imageView_aircon_control_button);
        mAirconControlButton.setOnClickListener(FanControlListener);
        mAirconSettingUp = (ImageView) mView.findViewById(R.id.imageView_aircon_setting_up);
        mAirconSettingUp.setOnClickListener(FanControlUpListener);
        mAirconSettingDown = (ImageView) mView.findViewById(R.id.imageView_aircon_setting_down);
        mAirconSettingDown.setOnClickListener(FanControlDownListener);

        mTextViewCurrentTemp = (TextView) mView.findViewById(R.id.textView_current_temp);
        mTextViewSettingTemp = (TextView) mView.findViewById(R.id.textView_setting_temp);

        mCurrentTemp = IOTHome.getInstance().getCurrentTempValue();
        mSettingTemp = IOTHome.getInstance().getSettingTempValue();
        mFanStatus = IOTHome.getInstance().getLEDStatus();

        mFloatCurrentTemp = Float.parseFloat(mCurrentTemp);
        if(mSettingTemp.equals("0")) {
            mIntSettingTemp = (int) mFloatCurrentTemp;
        } else {
            mIntSettingTemp = Integer.parseInt(mSettingTemp);
        }

        mTextViewCurrentTemp.setText(mCurrentTemp+"℃");
        mTextViewSettingTemp.setText(mIntSettingTemp+"℃");

        setTurnOnOff(mFanStatus);

        return mView;
    }

    private View.OnClickListener FanControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mFanStatus) {
                showMessage("fanOff");
                mFanStatus = false;
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_AIRCON_CONTROL, "FF");
            } else {
                showMessage("fanOn");
                mFanStatus = true;
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_AIRCON_CONTROL, "FN");
            }

            setTurnOnOff(mFanStatus);
            return;
        }
    };

    private View.OnClickListener FanControlUpListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIntSettingTemp++;
            Log.d(TAG, "mIntSettingTemp: " + mIntSettingTemp);
            IOTHome.getInstance().setSettingTempValue(String.valueOf(mIntSettingTemp));
            mTextViewSettingTemp.setText(mIntSettingTemp + "℃");
            return;
        }
    };

    private View.OnClickListener FanControlDownListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mIntSettingTemp--;
            Log.d(TAG, "TmIntSettingTemp: " + mIntSettingTemp);
            IOTHome.getInstance().setSettingTempValue(String.valueOf(mIntSettingTemp));
            mTextViewSettingTemp.setText(mIntSettingTemp+"℃");
            return;
        }
    };


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Context context = getActivity();
        mActivityCommunicator = (ActivityCommunicator) context;
    }

    public void setData(final String data) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showMessage(data);

                if (data.equals(ActivityMainFrame.COMMAND_CONNECTED_DEVICE)) {
                } else if (data.equals(ActivityMainFrame.COMMAND_DISCONNECTED_DEVICE)) {
                } else if (data.startsWith("T")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    mCurrentTemp = tempStr[1];
                    IOTHome.getInstance().setCurrentTempValue(mCurrentTemp);
                    mTextViewCurrentTemp.setText(mCurrentTemp+"℃");
                } else if (data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("FN")) {
                        mFanStatus = true;
                        IOTHome.getInstance().setFanStatus(mFanStatus);
                        setTurnOnOff(mFanStatus);
                    } else if (tempStr[1].equals("FF")) {
                        mFanStatus = false;
                        IOTHome.getInstance().setFanStatus(mFanStatus);
                        setTurnOnOff(mFanStatus);
                    }
                }
            }
        });
    }

    private void setTurnOnOff(boolean onOff) {
        if (onOff) {
            mAirconControlMain.setImageResource(R.drawable.airconditioning_on);
            mAirconControlButton.setImageResource(R.drawable.switch_horizontal_open);
        } else {
            mAirconControlMain.setImageResource(R.drawable.airconditioning_off);
            mAirconControlButton.setImageResource(R.drawable.switch_horizontal_close);
        }
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

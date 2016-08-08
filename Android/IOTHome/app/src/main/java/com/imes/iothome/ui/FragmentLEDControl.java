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

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

public class FragmentLEDControl extends Fragment {

    public static final String TAG = FragmentLEDControl.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;
    private ImageView mLightControlMain;
    private ImageView mLightControlButton;

    private boolean mLEDStatus = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_light_control, container, false);

        mLightControlMain = (ImageView) mView.findViewById(R.id.imageView_light_control_main);

        mLightControlButton = (ImageView) mView.findViewById(R.id.imageView_light_control_button);
        mLightControlButton.setOnClickListener(LightControlListener);

        mLEDStatus = IOTHome.getInstance().getLEDStatus();

        setTurnOnOff(mLEDStatus);

        return mView;
    }

    private View.OnClickListener LightControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mLEDStatus) {
                showMessage("ledOff");  //ledOff
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_LED_CONTROL, "LF");
            } else {
                showMessage("ledOn");  //ledOn
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_LED_CONTROL, "LN");
            }
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
                } else if (data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("LN")) {  //ledOn
                        mLEDStatus = true;
                        IOTHome.getInstance().setLEDStatus(mLEDStatus);
                        setTurnOnOff(mLEDStatus);
                    } else if (tempStr[1].equals("LF")) {  //ledOff
                        mLEDStatus = false;
                        IOTHome.getInstance().setLEDStatus(mLEDStatus);
                        setTurnOnOff(mLEDStatus);
                    }
                }
            }
        });
    }

    private void setTurnOnOff(boolean onOff) {
        if (onOff) {
            mLightControlMain.setImageResource(R.drawable.light_on_main);
            mLightControlButton.setImageResource(R.drawable.switch_horizontal_open);

        } else {
            mLightControlMain.setImageResource(R.drawable.light_off_main);
            mLightControlButton.setImageResource(R.drawable.switch_horizontal_close);
        }
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

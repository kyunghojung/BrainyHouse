package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

public class FragmentSecurityControl extends Fragment {

    public static final String TAG = FragmentSecurityControl.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;
    private ImageView mMonitoringControlMain;
    private TextView mIntruder;

    private boolean mPirStatus = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_security_control, container, false);

        mMonitoringControlMain = (ImageView) mView.findViewById(R.id.imageView_monitoring_control_main);
        mIntruder = (TextView) mView.findViewById(R.id.textView_intruder);

        mPirStatus = IOTHome.getInstance().getPirStatus();

        setTurnOnOff(mPirStatus);

        return mView;
    }


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
                if (data.startsWith("I")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if(tempStr[1].equals("1")) {
                        mPirStatus = true;

                    } else {
                        mPirStatus = false;
                    }
                    IOTHome.getInstance().setPirStatus(mPirStatus);
                    setTurnOnOff(mPirStatus);
                }
            }
        });
    }

    private void setTurnOnOff(boolean onOff) {
        if (onOff) {
            mMonitoringControlMain.setImageResource(R.drawable.monitoring_intrusion);
            mIntruder.setVisibility(View.VISIBLE);
        } else {
            mMonitoringControlMain.setImageResource(R.drawable.monitoring_icon);
            mIntruder.setVisibility(View.GONE);
        }
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

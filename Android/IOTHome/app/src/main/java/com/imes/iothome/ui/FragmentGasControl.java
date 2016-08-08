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

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

public class FragmentGasControl extends Fragment {

    public static final String TAG = FragmentGasControl.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;
    private ImageView mGasValveControlMain;
    private ImageView mGasValveControlButton;

    private boolean mGasValveStatus = false;  //false: close, true: open

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_gas_control, container, false);

        mGasValveControlMain = (ImageView) mView.findViewById(R.id.imageView_gas_control_main);
        mGasValveControlButton = (ImageView) mView.findViewById(R.id.imageView_gas_control_button);

        mGasValveControlButton.setOnClickListener(GasValveControlListener);

        mGasValveStatus = false;
        IOTHome.getInstance().setGasValveStatus(mGasValveStatus);
        setTurnOnOff(mGasValveStatus);

        return mView;
    }

    private View.OnClickListener GasValveControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mGasValveStatus) {
                showMessage("GasValve Close");
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_GAS_CONTROL, "VC");
            } else {
                showMessage("GasValve Open");
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_GAS_CONTROL, "VO");
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
                    if (tempStr[1].equals("VO")) {
                        mGasValveStatus = true;
                        IOTHome.getInstance().setGasValveStatus(mGasValveStatus);
                    } else if (tempStr[1].equals("VC")) {
                        mGasValveStatus = false;
                        IOTHome.getInstance().setGasValveStatus(mGasValveStatus);
                    }
                    setTurnOnOff(mGasValveStatus);
                }
            }
        });
    }

    private void setTurnOnOff(boolean onOff) {
        if (onOff) {
            mGasValveControlMain.setImageResource(R.drawable.gas_valve_open);
            mGasValveControlButton.setImageResource(R.drawable.switch_vertical_close);

        } else {
            mGasValveControlMain.setImageResource(R.drawable.gas_valve_close);
            mGasValveControlButton.setImageResource(R.drawable.switch_vertical_open);
        }
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

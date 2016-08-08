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

public class FragmentDoorlockControl extends Fragment {

    public static final String TAG = FragmentDoorlockControl.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;
    private ImageView mDoorlockControlMain;
    private ImageView mDoorlockControlButton;

    private boolean mDoorStatus = false; // close: false, open: true

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_doorlock_control, container, false);

        mDoorlockControlMain = (ImageView) mView.findViewById(R.id.imageView_doorlock_control_main);
        mDoorlockControlButton = (ImageView) mView.findViewById(R.id.imageView_doorlock_control_button);

        mDoorlockControlButton.setOnClickListener(DoorControlListener);

        mDoorStatus = IOTHome.getInstance().getDoorStatus();

        setTurnOnOff(mDoorStatus);

        return mView;
    }

    private View.OnClickListener DoorControlListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mDoorStatus) {
                showMessage("Door Close");
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_DOORLOCK_CONTROL, "DC");
            } else {
                showMessage("Door Open");
                mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_DOORLOCK_CONTROL, "DO");
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
                    if (tempStr[1].equals("DO")) {
                        mDoorStatus = true;
                        IOTHome.getInstance().setDoorStatus(mDoorStatus);
                    } else if (tempStr[1].equals("DC")) {
                        mDoorStatus = false;
                        IOTHome.getInstance().setDoorStatus(mDoorStatus);
                    }
                    setTurnOnOff(mDoorStatus);
                }
            }
        });
    }

    private void setTurnOnOff(boolean onOff) {
        if (onOff) {
            mDoorlockControlMain.setImageResource(R.drawable.door_unlocked);
            mDoorlockControlButton.setImageResource(R.drawable.switch_vertical_close);

        } else {
            mDoorlockControlMain.setImageResource(R.drawable.door_locked);
            mDoorlockControlButton.setImageResource(R.drawable.switch_vertical_open);
        }
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

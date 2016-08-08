package com.ents.brainyhouse.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ents.brainyhouse.R;

import java.text.DateFormat;
import java.util.Date;

public class SecurityFragment extends Fragment {

    public static final String TAG = SecurityFragment.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;

    private TextView mConnectionStatus;
    private TextView mTextDoorStatus;
    private TextView mTextViewPIRStatus;
    private TextView mRFValue;

    private Button mButtonDoor;
    private Button mButtonCamera;

    private ListView mMessageListView;
    private ArrayAdapter<String> mListAdapter;

    private boolean mDoorStatus = false;
    private boolean mPirStatus = false;

    private String mConnectedTime;
    private String mDisconnectedTime;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_security, container, false);

        mMessageListView = (ListView) view.findViewById(R.id.listBrainy1Message);
        mListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message_detail);
        mMessageListView.setAdapter(mListAdapter);
        mMessageListView.setDivider(null);

        mConnectionStatus = (TextView) view.findViewById(R.id.textViewDeviceStatusSecurity);
        mConnectionStatus.setTextColor(Color.RED);
        mConnectionStatus.setText("Disconnected");

        mTextDoorStatus = (TextView) view.findViewById(R.id.textViewDoorStatus);
        mTextViewPIRStatus = (TextView) view.findViewById(R.id.textViewPIRStatus);

        mRFValue = (TextView) view.findViewById(R.id.textViewSecurityRFValue);

        mButtonDoor = (Button) view.findViewById(R.id.buttonDoor);
        mButtonDoor.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mDoorStatus)
                {
                    showMessage("doorClose");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SECURITY, "doorClose");
                }
                else
                {
                    showMessage("doorOpen");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SECURITY, "doorOpen");
                }
            }
        });
        mButtonCamera = (Button) view.findViewById(R.id.buttonCamera);
        mButtonCamera.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SECURITY, "StartCamera");
            }
        });
        return view;
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

                if (data.equals(DashBoardActivity.COMMAND_CONNECTED_DEVICE)) {

                    mConnectedTime = DateFormat.getTimeInstance().format(new Date());
                    showMessage("Connected "+mConnectedTime);

                    mConnectionStatus.setTextColor(Color.BLUE);
                    mConnectionStatus.setText("Connected");
                    showMessage("Connected");
                } else if (data.equals(DashBoardActivity.COMMAND_DISCONNECTED_DEVICE)) {
                    mDisconnectedTime = DateFormat.getTimeInstance().format(new Date());
                    showMessage("Connected "+  mConnectedTime);
                    showMessage("Disonnected " + mDisconnectedTime);

                    mConnectionStatus.setTextColor(Color.RED);
                    mConnectionStatus.setText("Disonnected");
                    showMessage("Disonnected");
                } else if (data.startsWith("RF")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    mRFValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                    mRFValue.setTextColor(Color.BLUE);
                    mRFValue.setText(tempStr[1]);
                } else if (data.startsWith("I")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("1")) {
                        mPirStatus = true;
                        mTextViewPIRStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextViewPIRStatus.setTextColor(Color.RED);
                        mTextViewPIRStatus.setText("1");
                    } else {
                        mPirStatus = false;
                        mTextViewPIRStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextViewPIRStatus.setTextColor(Color.BLUE);
                        mTextViewPIRStatus.setText("0");

                    }
                }  else if (data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("doorOpen")) {
                        mDoorStatus = true;
                        mTextDoorStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextDoorStatus.setTextColor(Color.BLUE);
                        mTextDoorStatus.setText("1");
                        mButtonDoor.setText("CLOSE");
                    } else if (tempStr[1].equals("doorClose")){
                        mDoorStatus = false;
                        mTextDoorStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextDoorStatus.setTextColor(Color.RED);
                        mTextDoorStatus.setText("0");
                        mButtonDoor.setText("OPEN");
                    }
                }
            }
        });
    }

    public void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mListAdapter.add("[" + currentDateTimeString + "] " + message);
                    mMessageListView.smoothScrollToPosition(mListAdapter.getCount() - 1);

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}

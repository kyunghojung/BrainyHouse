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
import android.widget.Button;
import android.widget.TextView;

import com.ents.brainyhouse.R;

import java.text.DateFormat;
import java.util.Date;

public class LivingRoomFragment extends Fragment {

    public static final String TAG = LivingRoomFragment.class.getSimpleName();;

    private ActivityCommunicator mActivityCommunicator;

    private TextView mConnectionStatus;
    private TextView mLuxValue;
    private TextView mTempValue;
    private TextView mHumiValue;
    private TextView mTextLEDStatus;
    private TextView mTextRLEDStatus;
    private TextView mTextCurtainStatus;
    private TextView mTextFanStatus;
    private Button mButtonLED;
    private Button mButtonRedLED;
    private Button mButtonCurtain;
    private Button mButtonFan;

    private boolean mLEDStatus = false;
    private boolean mRLEDStatus = false;
    private boolean mCurtainStatus = false;
    private boolean mFanStatus = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_livingroom, container, false);

        mConnectionStatus = (TextView) view.findViewById(R.id.textViewDeviceStatusLivingRoom);
        mConnectionStatus.setTextColor(Color.RED);
        mConnectionStatus.setText("Disconnected");

        mLuxValue = (TextView) view.findViewById(R.id.textViewLuxValue);
        mTempValue = (TextView) view.findViewById(R.id.textViewTempValue);
        mHumiValue = (TextView) view.findViewById(R.id.textViewHumiValue);
        mTextLEDStatus = (TextView) view.findViewById(R.id.textViewLEDStatus);
        mTextRLEDStatus = (TextView) view.findViewById(R.id.textViewRedLEDStatus);
        mTextCurtainStatus = (TextView) view.findViewById(R.id.textViewCurtainStatus);
        mTextFanStatus = (TextView) view.findViewById(R.id.textViewFanStatus);

        mButtonLED = (Button) view.findViewById(R.id.buttonLED);
        mButtonLED.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mLEDStatus)
                {
                    showMessage("ledOff");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "ledOff");
                }
                else
                {
                    showMessage("ledOn");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "ledOn");
                }
            }
        });

        mButtonRedLED = (Button) view.findViewById(R.id.buttonRedLED);
        mButtonRedLED.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mRLEDStatus)
                {
                    showMessage("redLedOff");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "redLedOff");
                }
                else
                {
                    showMessage("redLedOn");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "redLedOn");
                }
            }
        });

        mButtonCurtain = (Button) view.findViewById(R.id.buttonCurtain);
        mButtonCurtain.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mCurtainStatus)
                {
                    showMessage("curtainClose");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "curtainClose");
                }
                else
                {
                    showMessage("curtainOpen");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "curtainOpen");
                }
            }
        });

        mButtonFan = (Button) view.findViewById(R.id.buttonFan);
        mButtonFan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mFanStatus)
                {
                    showMessage("fanOff");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "fanOff");
                }
                else
                {
                    showMessage("fanOn");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "fanOn");
                }
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
                    mConnectionStatus.setTextColor(Color.BLUE);
                    mConnectionStatus.setText("Connected");
                } else if (data.equals(DashBoardActivity.COMMAND_DISCONNECTED_DEVICE)) {
                    mConnectionStatus.setTextColor(Color.RED);
                    mConnectionStatus.setText("Disonnected");
                } else if (data.startsWith("L")) {
                      /*
                      // lux
                      0.0001 lux	Moonless, overcast night sky (starlight)[3]
                      0.002 lux	Moonless clear night sky with airglow[3]
                      0.27–1.0 lux	Full moon on a clear night[3][4]
                      3.4 lux	Dark limit of civil twilight under a clear sky[5]
                      50 lux	Family living room lights (Australia, 1998)[6]
                      80 lux	Office building hallway/toilet lighting[7][8]
                      100 lux	Very dark overcast day[3]
                      320–500 lux	Office lighting[6][9][10][11]
                      400 lux	Sunrise or sunset on a clear day.
                      1000 lux	Overcast day;[3] typical TV studio lighting
                      10000–25000 lux	Full daylight (not direct sun)[3]
                      32000–100000 lux	Direct sunlight
                      */
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    mLuxValue.setText(tempStr[1]);
                } else if (data.startsWith("T")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    mTempValue.setText(tempStr[1]);
                } else if (data.startsWith("H")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    mHumiValue.setText(tempStr[1]);
                } else if(data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("ledOn")) {
                        mLEDStatus = true;
                        mTextLEDStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextLEDStatus.setTextColor(Color.BLUE);
                        mTextLEDStatus.setText("1");
                        mButtonLED.setText("OFF");

                    } else if (tempStr[1].equals("ledOff")) {
                        mLEDStatus = false;
                        mTextLEDStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextLEDStatus.setTextColor(Color.RED);
                        mTextLEDStatus.setText("O");
                        mButtonLED.setText("ON");

                    } else if (tempStr[1].equals("redLedOn")) {
                        mRLEDStatus = true;
                        mTextRLEDStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextRLEDStatus.setTextColor(Color.BLUE);
                        mTextRLEDStatus.setText("1");
                        mButtonRedLED.setText("OFF");

                    } else if (tempStr[1].equals("redLedOff")) {
                        mRLEDStatus = false;
                        mTextRLEDStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextRLEDStatus.setTextColor(Color.RED);
                        mTextRLEDStatus.setText("0");
                        mButtonRedLED.setText("ON");

                    } else if (tempStr[1].equals("curtainOpen")) {
                        mCurtainStatus = true;
                        mTextCurtainStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextCurtainStatus.setTextColor(Color.BLUE);
                        mTextCurtainStatus.setText("1");
                        mButtonCurtain.setText("Close");
                    } else if (tempStr[1].equals("curtainClose")) {
                        mCurtainStatus = false;
                        mTextCurtainStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextCurtainStatus.setTextColor(Color.RED);
                        mTextCurtainStatus.setText("0");
                        mButtonCurtain.setText("Open");
                    } else if (tempStr[1].equals("fanOn")) {
                        mFanStatus = true;
                        mTextFanStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextFanStatus.setTextColor(Color.BLUE);
                        mTextFanStatus.setText("1");
                        mButtonFan.setText("OFF");
                    } else if (tempStr[1].equals("fanOff")) {
                        mFanStatus = false;
                        mTextFanStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextFanStatus.setTextColor(Color.RED);
                        mTextFanStatus.setText("0");
                        mButtonFan.setText("ON");
                    }
                }
            }
        });
    }

    private void showMessage(final String message) {
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        Log.d(TAG, "[" + currentDateTimeString + "] " + message);
        mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_LIVINGROOM, "message=" + message);
    }
}

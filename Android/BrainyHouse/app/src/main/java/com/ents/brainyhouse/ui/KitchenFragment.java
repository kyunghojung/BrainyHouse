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

public class KitchenFragment extends Fragment {

    public static final String TAG = KitchenFragment.class.getSimpleName();;

    private ActivityCommunicator mActivityCommunicator;

    private TextView mConnectionStatus;
    private TextView mGasValue;
    private TextView mTextRedLedStatus;
    private TextView mTextGasValveStatus;
    private TextView mTextBuzzerStatus;

    private Button mButtonRLED;
    private Button mButtonGASValve;
    private Button mButtonBuzzer;

    private boolean mRLEDStatus = false;
    private boolean mGASValveStatus = false;
    private boolean mBuzzerStatus = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_kitchen, container, false);

        mConnectionStatus = (TextView) view.findViewById(R.id.textViewDeviceStatusKitchen);
        mConnectionStatus.setTextColor(Color.RED);
        mConnectionStatus.setText("Disconnected");

        mGasValue = (TextView) view.findViewById(R.id.textViewTempValue);
        mTextRedLedStatus = (TextView) view.findViewById(R.id.textViewRedLEDStatus);
        mTextGasValveStatus = (TextView) view.findViewById(R.id.textViewGasValveStatus);
        mTextBuzzerStatus = (TextView) view.findViewById(R.id.textViewBuzzerStatus);

        mButtonRLED = (Button) view.findViewById(R.id.buttonRedLED);
        mButtonRLED.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mRLEDStatus)
                {
                    mRLEDStatus = false;
                    showMessage("redLedOff");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "redLedOff");
                }
                else
                {
                    mRLEDStatus = true;
                    showMessage("redLedOn");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "redLedOn");
                }
            }
        });

        mButtonGASValve = (Button) view.findViewById(R.id.buttonGasValve);
        mButtonGASValve.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mGASValveStatus)
                {
                    mGASValveStatus = false;
                    showMessage("valveClose");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "valveClose");
                }
                else
                {
                    mGASValveStatus = true;
                    showMessage("valveOpen");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "valveOpen");
                }
            }
        });

        mButtonBuzzer = (Button) view.findViewById(R.id.buttonBuzzer);
        mButtonBuzzer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(mBuzzerStatus)
                {
                    mBuzzerStatus = false;
                    showMessage("buzzerOff");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "buzzerOff");
                }
                else
                {
                    mBuzzerStatus = true;
                    showMessage("buzzerOn");
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "buzzerOn");
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
                } else if (data.startsWith("G")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    if(tempStr[1].equals("1")) {
                        mGasValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mGasValue.setTextColor(Color.RED);
                        mGasValue.setText("1");
                    } else {
                        mGasValue.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mGasValue.setTextColor(Color.BLUE);
                        mGasValue.setText("0");
                    }
                } else if(data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("redLedOn")) {
                        mTextRedLedStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextRedLedStatus.setTextColor(Color.RED);
                        mTextRedLedStatus.setText("1");
                        mButtonRLED.setText("OFF");
                    } else if (tempStr[1].equals("redLedOff")) {
                        mTextRedLedStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextRedLedStatus.setTextColor(Color.BLUE);
                        mTextRedLedStatus.setText("0");
                        mButtonRLED.setText("ON");
                    } else if (tempStr[1].equals("buzzerOn")) {
                        mTextBuzzerStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextBuzzerStatus.setTextColor(Color.RED);
                        mTextBuzzerStatus.setText("1");
                        mButtonBuzzer.setText("OFF");
                    } else if (tempStr[1].equals("buzzerOff")) {
                        mTextBuzzerStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextBuzzerStatus.setTextColor(Color.BLUE);
                        mTextBuzzerStatus.setText("0");
                        mButtonBuzzer.setText("ON");
                    }else if (tempStr[1].equals("valveOpen")) {
                        mTextGasValveStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextGasValveStatus.setTextColor(Color.RED);
                        mTextGasValveStatus.setText("1");
                        mButtonGASValve.setText("CLOSE");
                    } else if (tempStr[1].equals("valveClose")) {
                        mTextGasValveStatus.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                        mTextGasValveStatus.setTextColor(Color.BLUE);
                        mTextGasValveStatus.setText("0");
                        mButtonGASValve.setText("OPEN");
                    }
                }
            }
        });
    }

    private void showMessage(final String message) {
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        Log.d(TAG, "[" + currentDateTimeString + "] " + message);
        mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_KITCHEN, "message=" + message);
    }
}

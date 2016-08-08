package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

import java.text.DateFormat;
import java.util.Date;

public class FragmentDashboard extends Fragment {
    public static final String TAG = FragmentDashboard.class.getSimpleName();

    private RelativeLayout mDashBoardContainer;
    Context mContext;
    View mView;

    private ActivityCommunicator mActivityCommunicator;

    private TextView mTextViewTemp;
    private TextView mTextViewHumi;
    private TextView mTextViewUser;
    private TextView mTextViewLux;
    private TextView mTextViewFan;
    private TextView mTextViewGas;
    private TextView mTextViewDoor;
    private TextView mTextViewLED;
    private TextView mTextViewCurtain;
    private TextView mTextViewBT1;
    private TextView mTextViewBT2;

    private boolean mLEDStatus = false;
    private boolean mCurtainStatus = false;
    private boolean mFanStatus = false;
    private boolean mGasStatus = false;
    private boolean mDoorStatus = false;
    private boolean mBT1Status = false;
    private boolean mBT2Status = false;

    private String mLuxValue = "0";
    private String mTempValue = "0";
    private String mHumiValue = "0";
    private String mUserName = "Unknown";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        mLEDStatus = IOTHome.getInstance().getLEDStatus();
        mCurtainStatus = IOTHome.getInstance().getCurtainStatus();
        mFanStatus = IOTHome.getInstance().getFanStatus();
        mGasStatus = IOTHome.getInstance().getGasStatus();
        mDoorStatus = IOTHome.getInstance().getDoorStatus();
        mBT1Status = IOTHome.getInstance().getBT1Status();
        mBT2Status = IOTHome.getInstance().getBT2Status();

        mLuxValue  = IOTHome.getInstance().getLuxValue();
        mTempValue = IOTHome.getInstance().getCurrentTempValue();
        mHumiValue = IOTHome.getInstance().getHumiValue();

        mUserName = IOTHome.getInstance().getCurrentUser();

        mTextViewTemp =  (TextView) mView.findViewById(R.id.textView_temp);
        mTextViewTemp.setText(mTempValue + "℃");
        mTextViewTemp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Temp was clicked!!!");
            }
        });

        mTextViewHumi =  (TextView) mView.findViewById(R.id.textView_humi);
        mTextViewHumi.setText(mHumiValue+"%");
        mTextViewHumi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Humi was clicked!!!");
            }
        });

        mTextViewUser =  (TextView) mView.findViewById(R.id.textView_user);
        mTextViewUser.setText(mUserName);
        mTextViewUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "USER was clicked!!!");
            }
        });

        mTextViewLux =  (TextView) mView.findViewById(R.id.textView_lux);
        mTextViewLux.setText(mLuxValue+"lx");
        mTextViewLux.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "LUX was clicked!!!");
            }
        });

        mTextViewFan =  (TextView) mView.findViewById(R.id.textView_fan);
        mTextViewFan.setBackgroundColor(mFanStatus?getResources().getColor(R.color.light_green):getResources().getColor(R.color.light_grey));
        mTextViewFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Fan was clicked!!!");
            }
        });

        mTextViewGas =  (TextView) mView.findViewById(R.id.textView_gas);
        mTextViewGas.setBackgroundColor(mGasStatus ? Color.RED : getResources().getColor(R.color.light_green));
        mTextViewGas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Gas was clicked!!!");
            }
        });

        mTextViewDoor =  (TextView) mView.findViewById(R.id.textView_door);
        mTextViewDoor.setBackgroundColor(mDoorStatus?getResources().getColor(R.color.light_green):getResources().getColor(R.color.light_grey));
        mTextViewDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Door was clicked!!!");
            }
        });

        mTextViewLED =  (TextView) mView.findViewById(R.id.textView_lamp);
        mTextViewLED.setBackgroundColor(mLEDStatus?getResources().getColor(R.color.light_green):getResources().getColor(R.color.light_grey));
        mTextViewLED.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "LAMP was clicked!!!");
            }
        });

        mTextViewCurtain =  (TextView) mView.findViewById(R.id.textView_curtain);
        mTextViewCurtain.setBackgroundColor(mCurtainStatus?getResources().getColor(R.color.light_green):getResources().getColor(R.color.light_grey));
        mTextViewCurtain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "Curtain was clicked!!!");
            }
        });

        mTextViewBT1 =  (TextView) mView.findViewById(R.id.textView_bt1);
        mTextViewBT1.setBackgroundColor(mBT1Status?getResources().getColor(R.color.light_green):getResources().getColor(R.color.light_grey));
        mTextViewBT1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "BT1 was clicked!!!");
            }
        });

        mTextViewBT2 =  (TextView) mView.findViewById(R.id.textView_bt2);
        mTextViewBT2.setBackgroundColor(mBT2Status?getResources().getColor(R.color.light_green):getResources().getColor(R.color.light_grey));
        mTextViewBT2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"BT2 was clicked!!!");
            }
        });

        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
        mActivityCommunicator = (ActivityCommunicator) mContext;
    }

    public void setData(final String data) {
        if(getActivity() == null) {
            Log.d(TAG, "getActivity() is null!!");
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showMessage(data);
                if (data.startsWith(ActivityMainFrame.COMMAND_CONNECTED_DEVICE)) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if(tempStr[1].equals("DEVICE1")) {
                        mBT1Status = true;
                        mTextViewBT1.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    }
                    else if(tempStr[1].equals("DEVICE2")) {
                        mBT2Status = true;
                        mTextViewBT2.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    }
                } else if (data.startsWith(ActivityMainFrame.COMMAND_DISCONNECTED_DEVICE)) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if(tempStr[1].equals("DEVICE1")) {
                        mBT1Status = true;
                        mTextViewBT1.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    }
                    else if(tempStr[1].equals("DEVICE2")) {
                        mBT2Status = true;
                        mTextViewBT2.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    }
                } else if (data.startsWith("L")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    mLuxValue = tempStr[1];
                    mTextViewLux.setText(mLuxValue + "lx");

                    IOTHome.getInstance().setLuxValue(mLuxValue);

                } else if (data.startsWith("T")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    mTempValue = tempStr[1];
                    mTextViewTemp.setText(mTempValue + "℃");
                    IOTHome.getInstance().setCurrentTempValue(mTempValue);
                } else if (data.startsWith("H")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    mHumiValue = tempStr[1];
                    mTextViewHumi.setText(mHumiValue + "%");
                    IOTHome.getInstance().setHumiValue(mHumiValue);
                } else if (data.startsWith("G")) {
                    String tempStr[] = data.split(":");
                    if(tempStr.length < 2) {
                        return;
                    }
                    if(tempStr[1].equals("1")) {
                        mGasStatus = true;
                    } else {
                        mGasStatus = false;
                    }
                    IOTHome.getInstance().setGasStatus(mGasStatus);

                    mTextViewGas.setBackgroundColor(
                            mGasStatus ? Color.RED :
                                    getResources().getColor(R.color.light_green));

                }
                else if (data.startsWith("OK")) {
                    String tempStr[] = data.split(":");
                    if (tempStr.length < 2) {
                        return;
                    }
                    if (tempStr[1].equals("LN")) {  //ledOn
                        mLEDStatus = true;
                        IOTHome.getInstance().setLEDStatus(mLEDStatus);
                        mTextViewLED.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    } else if (tempStr[1].equals("LF")) {  //ledOff
                        mLEDStatus = false;
                        IOTHome.getInstance().setLEDStatus(mLEDStatus);
                        mTextViewLED.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    } else if (tempStr[1].equals("CO")) {  //curtainOpen
                        mCurtainStatus = true;
                        IOTHome.getInstance().setCurtainStatus(mCurtainStatus);
                        mTextViewCurtain.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    } else if (tempStr[1].equals("CC")) {  //curtainClose
                        mCurtainStatus = false;
                        IOTHome.getInstance().setCurtainStatus(mCurtainStatus);
                        mTextViewLED.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    } else if (tempStr[1].equals("FN")) {  //fanOn
                        mFanStatus = true;
                        IOTHome.getInstance().setFanStatus(mFanStatus);
                        mTextViewFan.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    } else if (tempStr[1].equals("FF")) {  //fanOff
                        mFanStatus = false;
                        IOTHome.getInstance().setFanStatus(mFanStatus);
                        mTextViewFan.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    } else if (tempStr[1].equals("FN")) {  //fanOn
                        mFanStatus = true;
                        IOTHome.getInstance().setFanStatus(mFanStatus);
                        mTextViewFan.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    } else if (tempStr[1].equals("FF")) {  //fanOff
                        mFanStatus = false;
                        IOTHome.getInstance().setFanStatus(mFanStatus);
                        mTextViewFan.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    } else if (tempStr[1].equals("DO")) {  //DoorOpen
                        mDoorStatus = true;
                        IOTHome.getInstance().setDoorStatus(mDoorStatus);
                        mTextViewDoor.setBackgroundColor(getResources().getColor(R.color.light_green));
                        mView.invalidate();
                    } else if (tempStr[1].equals("DC")) {  //DoorClose
                        mDoorStatus = false;
                        IOTHome.getInstance().setDoorStatus(mDoorStatus);
                        mTextViewDoor.setBackgroundColor(getResources().getColor(R.color.light_grey));
                        mView.invalidate();
                    }
                }
            }
        });
    }

    private void showMessage(final String message) {
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        Log.d(TAG, "[" + currentDateTimeString + "] " + message);
        mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_DAHSBOARD, "message=" + message);
    }
}

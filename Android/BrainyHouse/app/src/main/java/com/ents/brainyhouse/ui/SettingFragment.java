package com.ents.brainyhouse.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.database.DBHelper;

import java.text.DateFormat;
import java.util.Date;

public class SettingFragment extends DialogFragment {
    public static final String TAG = SettingFragment.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;

    Button mButtonSecuritySetting;
    Button mButtonFridgeSetting;

    DBHelper mItemDB = new DBHelper(getActivity());

    public SettingFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container);

        mButtonSecuritySetting =  (Button) view.findViewById(R.id.buttonSecuritySetting);
        mButtonSecuritySetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SETTING, "SecuritySetting");
                getDialog().dismiss();
            }
        });
        mButtonFridgeSetting =  (Button) view.findViewById(R.id.buttonFridgeSetting);
        mButtonFridgeSetting.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SETTING, "FridgeSetting");
                getDialog().dismiss();
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

    public void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SETTING, "message=" + message);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}

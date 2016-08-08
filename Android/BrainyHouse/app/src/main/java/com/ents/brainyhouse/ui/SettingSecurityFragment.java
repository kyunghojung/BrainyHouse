package com.ents.brainyhouse.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.database.DBHelper;
import com.ents.brainyhouse.database.SecurityItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SettingSecurityFragment extends DialogFragment {
    public static final String TAG = SettingSecurityFragment.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;

    private TextView mTextViewSecuritySettingRFIDValue;
    private ListView mItemsListView;
    private ArrayAdapter<String> mItemsListAdapter;

    private Button mButtonOK;
    private Button mButtonCancel;


    private DBHelper mSecurityDBHelper;
    private ArrayList<SecurityItem> mSecurityItems = new ArrayList<SecurityItem>();

    public SettingSecurityFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_security, container);

        mItemsListView = (ListView) view.findViewById(R.id.listViewSecuritySettingItems);
        mItemsListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message_detail);
        mItemsListView.setAdapter(mItemsListAdapter);
        mItemsListView.setDivider(null);

        mTextViewSecuritySettingRFIDValue =  (TextView) view.findViewById(R.id.textViewSecuritySettingRFIDValue);

        mButtonOK =  (Button) view.findViewById(R.id.buttonOK);
        mButtonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSecurityDBHelper.addSecurityItem(mTextViewSecuritySettingRFIDValue.getText().toString());
                mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SECURITY_SETTING, "ChangeSetting");
                getDialog().dismiss();
            }
        });
        mButtonCancel =  (Button) view.findViewById(R.id.buttonCancel);
        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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

        mSecurityDBHelper = new DBHelper(context);
        mSecurityItems = mSecurityDBHelper.getAllSecurityItems();

        for(SecurityItem securityItem:mSecurityItems) {
            updataItemsView(securityItem.getRFID());
        }
    }

    public void setData(final String data) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showMessage(data);
                Log.d(TAG, "data: " + data);
                if (data.startsWith("RF")) {
                    String tempStr[] = data.split(":");
                    Log.d(TAG, "tempStr.length: " + tempStr.length);
                    if (tempStr.length < 2) {
                        return;
                    }
                    mTextViewSecuritySettingRFIDValue.setText(tempStr[1]);
                }
            }
        });
    }

    public void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_SECURITY_SETTING, "message=" + message);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void updataItemsView(String rfid) {
        Log.d(TAG, "updataItemsView rfid: " + rfid);
        mItemsListAdapter.add(rfid);
        mItemsListView.smoothScrollToPosition(mItemsListView.getCount() - 1);
    }
}

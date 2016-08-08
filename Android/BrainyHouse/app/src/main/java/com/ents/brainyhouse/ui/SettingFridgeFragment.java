package com.ents.brainyhouse.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.database.DBHelper;
import com.ents.brainyhouse.database.FridgeItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SettingFridgeFragment extends DialogFragment {
    public static final String TAG = SettingFridgeFragment.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;

    private EditText mEditTextFridgeSettingItemName;
    private TextView mTextViewFrdigeSettingRFIDValue;
    private ListView mItemsListView;
    private FridgeItemAdapter mItemsListAdapter;

    private Button mButtonOK;
    private Button mButtonCancel;


    private DBHelper mFridgeDBHelper;
    private ArrayList<FridgeItem> mFridgeItems = new ArrayList<FridgeItem>();

    public SettingFridgeFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_fridge, container);

        mItemsListView = (ListView) view.findViewById(R.id.listViewFridgeSettingItems);
        mItemsListAdapter = new FridgeItemAdapter(getActivity(), R.layout.fridge_setting_detail, mFridgeItems);
        mItemsListView.setAdapter(mItemsListAdapter);
        mItemsListView.setDivider(null);

        mEditTextFridgeSettingItemName =  (EditText) view.findViewById(R.id.editTextFridgeSettingItemName);
        mTextViewFrdigeSettingRFIDValue =  (TextView) view.findViewById(R.id.textViewFrdigeSettingRFIDValue);

        mButtonOK =  (Button) view.findViewById(R.id.buttonOK);
        mButtonOK.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mFridgeDBHelper.addFridgeItem(mEditTextFridgeSettingItemName.getText().toString(),
                        mTextViewFrdigeSettingRFIDValue.getText().toString());
                mActivityCommunicator.passDataToActivity(DashBoardActivity.FRAGMENT_FRIDGE_SETTING, "ChangeSetting");
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

        mFridgeDBHelper = new DBHelper(context);
        mFridgeItems = mFridgeDBHelper.getAllFridgeItems();
        updataItemsView();
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
                    mTextViewFrdigeSettingRFIDValue.setText(tempStr[1]);
                }
            }
        });
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

    private void updataItemsView() {
        if(mFridgeItems.size() <= 0) {
            return;
        }
        mItemsListAdapter.updateItems(mFridgeItems);
        mItemsListView.smoothScrollToPosition(mItemsListView.getCount() - 1);
    }
}

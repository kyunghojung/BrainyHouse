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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.database.DBHelper;
import com.ents.brainyhouse.database.FridgeItem;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FridgeFragment extends Fragment {

    public static final String TAG = FridgeFragment.class.getSimpleName();;

    private ActivityCommunicator mActivityCommunicator;

    private TextView mConnectionStatus;
    private TextView mRFValue;

    private ListView mItemsListView;
    private FridgeItemAdapter mItemsListAdapter;
    private ListView mMessageListView;
    private ArrayAdapter<String> mMessageListAdapter;

    private String mConnectedTime;
    private String mDisconnectedTime;

    private DBHelper mFridgeDBHelper;
    private ArrayList<FridgeItem> mFridgeItems = new ArrayList<FridgeItem>();

    private boolean mIsStoring = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fridge, container, false);

        mItemsListView = (ListView) view.findViewById(R.id.listFridgeItems);
        mItemsListAdapter = new FridgeItemAdapter(getActivity(), R.layout.fridge_item_detail, mFridgeItems);
        mItemsListView.setAdapter(mItemsListAdapter);
        mItemsListView.setDivider(null);

        mMessageListView = (ListView) view.findViewById(R.id.listFridgeMessage);
        mMessageListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message_detail);
        mMessageListView.setAdapter(mMessageListAdapter);
        mMessageListView.setDivider(null);

        mConnectionStatus = (TextView) view.findViewById(R.id.textViewDeviceStatusFridge);
        mConnectionStatus.setTextColor(Color.RED);
        mConnectionStatus.setText("Disconnected");

        mRFValue = (TextView) view.findViewById(R.id.textViewFridgeRFValue);

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

                    for (int i = 0; i < mFridgeItems.size(); i++) {
                        FridgeItem fridgeItem = mFridgeItems.get(i);
                        if (fridgeItem.getRFID().equals(tempStr[1])) {
                            if(mIsStoring) {
                                fridgeItem.setCount(fridgeItem.getCount() + 1);
                            }
                            else {
                                fridgeItem.setCount(fridgeItem.getCount() -1);
                            }
                            mFridgeItems.set(i, fridgeItem);
                            mFridgeDBHelper.updateFridgeItem(fridgeItem);
                            showMessage("Item[" + i + "] added count: " + fridgeItem.getCount());
                            updataItemsView();
                            break;
                        }
                    }
                } else if (data.equals("BTN1")) {
                    mIsStoring = true;                  // Storing mode
                    showMessage("This is storing mode!");

                } else if (data.equals("BTN2")) {
                    mIsStoring = false;                 // unstoring mode
                    showMessage("This is unstoring mode!");

                }  else if (data.startsWith("ChangeSetting")) {
                    showMessage("Change Fridge setting");
                    mFridgeItems = mFridgeDBHelper.getAllFridgeItems();
                    updataItemsView();
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

    public void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mMessageListAdapter.add("[" + currentDateTimeString + "] " + message);
                    mMessageListView.smoothScrollToPosition(mMessageListAdapter.getCount() - 1);

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}
package com.imes.iothome.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.database.FridgeItem;
import com.imes.iothome.modal.IOTHome;

import java.util.ArrayList;

public class FragmentFridgeControl extends Fragment {

    public static final String TAG = FragmentFridgeControl.class.getSimpleName();
    Context mContext;
    private ActivityCommunicator mActivityCommunicator;
    private View mView;

    private TextView mTextViewCurrentMode;
    private TextView mTextViewItemAdd;


    private ListView mItemsListView;
    private AdapterFridgeItem mAdapterFridgeItem;
    private ArrayList<FridgeItem> mFridgeItems = new ArrayList<FridgeItem>();

    private DBHelper mFridgeItemDB;

    private final int FRIDGE_MODE_NONE = 0;
    private final int FRIDGE_MODE_INCOME = 1;
    private final int FRIDGE_MODE_RELEASE = 2;

    private int mCurrentMode = FRIDGE_MODE_NONE;

    private FragmentSettingFridge mFragmentSettingFridge;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_fridge_control, container, false);

        mTextViewCurrentMode = (TextView)mView.findViewById(R.id.textView_fridge_mode);
        mTextViewItemAdd = (TextView)mView.findViewById(R.id.textView_fridge_item_add);
        mTextViewItemAdd.setOnClickListener(ItemAddListener);

        mAdapterFridgeItem = new AdapterFridgeItem(getActivity(), R.layout.adapter_fridge_item, mFridgeItems);
        mAdapterFridgeItem.setDataChangeListener(FridgeItemChangeListner);

        mItemsListView = (ListView) mView.findViewById(R.id.listView_item_list);
        mItemsListView.setAdapter(mAdapterFridgeItem);
        mItemsListView.setDivider(null);

        mFridgeItemDB = IOTHome.getInstance().getDBHelper();

        mFridgeItems = mFridgeItemDB.getAllFridgeItems();

        updataItemsView(mFridgeItems);

        return mView;
    }

    private View.OnClickListener ItemAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
/*
            FragmentManager fm = getActivity().getSupportFragmentManager();
            mFragmentSettingFridge = new FragmentSettingFridge();
            mFragmentSettingFridge.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            mFragmentSettingFridge.show(fm, "Fridge Setting");
*/
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_FRIDGE_CONTROL, "DisplaySetting");
        }
    };

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mContext = getActivity();
        mActivityCommunicator = (ActivityCommunicator) mContext;
    }

    public void setData(final String data) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                showMessage(data);

                if (data.equals(ActivityMainFrame.COMMAND_CONNECTED_DEVICE)) {
                }
                else if (data.equals(ActivityMainFrame.COMMAND_DISCONNECTED_DEVICE)) {
                }
                else if (data.equals("BY")) {
                    mCurrentMode = FRIDGE_MODE_INCOME;
                    mTextViewCurrentMode.setTextColor(Color.GREEN);
                    mTextViewCurrentMode.setText(getText(R.string.fridge_item_income));
                }
                else if (data.equals("BR")) {
                    mCurrentMode = FRIDGE_MODE_RELEASE;
                    mTextViewCurrentMode.setTextColor(Color.RED);
                    mTextViewCurrentMode.setText(getText(R.string.fridge_item_release));
                }
                else if (data.startsWith("RF")) {
                    String tempStr[] = data.split(":");
                    Log.d(TAG, "tempStr.length: " + tempStr.length);
                    if (tempStr.length < 2) {
                        return;
                    }

                    FridgeItem item = mFridgeItemDB.getFridgeItemByRFID(tempStr[1]);

                    if(item != null && mCurrentMode != FRIDGE_MODE_NONE) {
                        if(mCurrentMode == FRIDGE_MODE_INCOME) {
                            int stock = item.getAmount() + 1;
                            Log.d(TAG, "income item " + item.getName()+", " + stock);
                            mFridgeItemDB.updateFrdgeItem(item.getRFID(), stock);
                        }
                        else if(mCurrentMode == FRIDGE_MODE_RELEASE) {
                            int stock = item.getAmount() - 1;
                            Log.d(TAG, "release item " + item.getName() + ", " + stock);
                            if(stock == 0){
                                mFridgeItemDB.deleteFridgeItem(item.getID());

                                String message = item.getName() + getString(R.string.fridge_item_order);
                                new AlertDialog.Builder(getActivity())
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setTitle(R.string.popup_title)
                                        .setMessage(message)
                                        .setPositiveButton(R.string.popup_confirm, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                            }
                            else {
                                mFridgeItemDB.updateFrdgeItem(item.getRFID(), stock);
                            }
                        }
                        refreshList();
                    }
                }
            }
        });
    }

    private DataChangedListener FridgeItemChangeListner = new DataChangedListener()
    {
        @Override
        public void deletedData(int id)
        {
            Log.d(TAG, "deleteData() id: " + id);
            refreshList();
        }

        @Override
        public void updatedData(int id)
        {
            Log.d(TAG, "updateData() id: " + id);

        }

    };

    private void updataItemsView(ArrayList<FridgeItem> fridgeItems) {
        Log.d(TAG, "updataItemsView: " + fridgeItems.size());

        mItemsListView.setAdapter(mAdapterFridgeItem);
        mAdapterFridgeItem.setChangedData(fridgeItems);
    }

    void refreshList()
    {
        mFridgeItems.clear();
        mFridgeItems = mFridgeItemDB.getAllFridgeItems();
        updataItemsView(mFridgeItems);
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

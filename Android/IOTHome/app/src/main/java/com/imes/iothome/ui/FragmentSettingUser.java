package com.imes.iothome.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.database.UserItem;
import com.imes.iothome.modal.IOTHome;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentSettingUser extends Fragment {
    public static final String TAG = FragmentSettingUser.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    Context mContext;

    private EditText mEditTextUserAddName;
    private TextView mTextViewUserAddRFID;
    private ListView mItemsListView;
    private TextView mUserAdd;
    private Button mMakeRfid;

    private AdapterUserData mAdapterUserData;
    private ArrayList<UserItem> mUserItems = new ArrayList<UserItem>();

    private DBHelper mUserDBHelper;

    public FragmentSettingUser() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_user, container, false);

        Log.d(TAG, "onCreateView()");

        mAdapterUserData = new AdapterUserData(getActivity(), R.layout.adapter_user_item, mUserItems);
        mAdapterUserData.setDataChangeListener(userDataChangeListner);

        mItemsListView = (ListView) view.findViewById(R.id.listView_user_list);
        mItemsListView.setAdapter(mAdapterUserData);
        mItemsListView.setDivider(null);

        mEditTextUserAddName = (EditText) view.findViewById(R.id.editText_setting_user_add_name);
        mTextViewUserAddRFID =  (TextView) view.findViewById(R.id.textView_setting_user_add_rfid_value);

        mUserAdd = (TextView)  view.findViewById(R.id.textView_setting_user_add);
        mUserAdd.setOnClickListener(UserAddListener);

        /*
        mMakeRfid = (Button) view.findViewById(R.id.button_make_rfid);
        mMakeRfid.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String randomStr = UUID.randomUUID().toString();
                while (randomStr.length() < 8) {
                    randomStr += UUID.randomUUID().toString();
                }
                String rfid = randomStr.substring(0, 8);

                mTextViewUserAddRFID.setText(rfid);
            }
        });
        */

        mUserDBHelper = IOTHome.getInstance().getDBHelper();
        mUserItems = mUserDBHelper.getAllUser();
        updataItemsView(mUserItems);
        return view;
    }

    private DataChangedListener userDataChangeListner = new DataChangedListener()
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

    private View.OnClickListener UserAddListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            String name = mEditTextUserAddName.getText().toString();
            String rfid = mTextViewUserAddRFID.getText().toString();
            if(name.length() == 0) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.popup_title)
                        .setMessage(R.string.please_input_name)
                        .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            } else if (rfid.length() == 0) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.popup_title)
                        .setMessage(R.string.please_input_rfid)
                        .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }

            if(mUserDBHelper.isExistName(name) == true){
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.popup_title)
                        .setMessage(R.string.already_exist_user_name)
                        .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }

            if(mUserDBHelper.isExistRFID(rfid) == true
                    || IOTHome.getInstance().getAdminCardNumber(mContext).equals(rfid)) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.popup_title)
                        .setMessage(R.string.already_exist_rfid)
                        .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }

            if(mUserDBHelper.isExistRFID(rfid) == true) {
                new AlertDialog.Builder(getActivity())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(R.string.popup_title)
                        .setMessage(R.string.already_exist_rfid)
                        .setPositiveButton(R.string.popup_yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
                return;
            }

            mUserDBHelper.addUser(name, rfid);
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_SETTING_USER, "ChangeSetting");

            mEditTextUserAddName.setText("");
            mTextViewUserAddRFID.setText("");
            refreshList();
            return;
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
                Log.d(TAG, "data: " + data);
                if (data.startsWith("RF")) {
                    String tempStr[] = data.split(":");
                    Log.d(TAG, "tempStr.length: " + tempStr.length);
                    if (tempStr.length < 2) {
                        return;
                    }
                    mTextViewUserAddRFID.setText(tempStr[1]);
                }
            }
        });
    }

    public void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_SETTING_USER, "message=" + message);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

    private void updataItemsView(ArrayList<UserItem> userItems) {
        Log.d(TAG, "updataItemsView: " + userItems.size());

        mItemsListView.setAdapter(mAdapterUserData);
        mAdapterUserData.setChangedData(userItems);
    }

    void refreshList()
    {
        mUserItems.clear();
        mUserItems = mUserDBHelper.getAllUser();
        updataItemsView(mUserItems);
    }
}

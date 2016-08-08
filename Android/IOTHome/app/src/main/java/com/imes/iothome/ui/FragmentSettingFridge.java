package com.imes.iothome.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.modal.IOTHome;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentSettingFridge extends DialogFragment {
    public static final String TAG = FragmentSettingFridge.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;

    private EditText mEditTextFridgeSettingItemAddName;
    private TextView mTextViewFrdigeSettingItemAddRFID;
    private EditText mTextViewFrdigeSettingItemAddShelfLife;
    private TextView mTextViewConfirm;
    private TextView mTextViewCancel;

    private DBHelper mFridgeDBHelper;

    public FragmentSettingFridge() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting_fridge, container);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(false);

        mEditTextFridgeSettingItemAddName =  (EditText) view.findViewById(R.id.editText_fridge_setting_item_add_name);
        mTextViewFrdigeSettingItemAddRFID =  (TextView) view.findViewById(R.id.textview_fridge_setting_item_add_rfid_value);
        mTextViewFrdigeSettingItemAddShelfLife =  (EditText) view.findViewById(R.id.editText_fridge_setting_item_add_shelf_life_value);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date());
        mTextViewFrdigeSettingItemAddShelfLife.setHint(date);
        mTextViewConfirm =  (TextView) view.findViewById(R.id.textview_fridge_setting_item_add_confirm);
        mTextViewConfirm.setOnClickListener(ItemAddConfirmListener);
        mTextViewCancel =  (TextView) view.findViewById(R.id.textview_fridge_setting_item_add_cancel);
        mTextViewCancel.setOnClickListener(ItemAddCancelListener);

        mFridgeDBHelper = IOTHome.getInstance().getDBHelper();
        return view;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        Context context = getActivity();
        mActivityCommunicator = (ActivityCommunicator) context;
    }

    private View.OnClickListener ItemAddConfirmListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String name = mEditTextFridgeSettingItemAddName.getText().toString();
            String rfid = mTextViewFrdigeSettingItemAddRFID.getText().toString();
            String shelfLife = mTextViewFrdigeSettingItemAddShelfLife.getText().toString();

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

            if(mFridgeDBHelper.isExistFridgeItemName(name) == true){
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

            if(mFridgeDBHelper.isExistFridgeItemRFID(rfid) == true) {
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

            mFridgeDBHelper.addFridgeItem(name, rfid, shelfLife);
            mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_SETTING_FRIDGE, "ChangeSetting");

            dismissAllowingStateLoss();
        }
    };

    private View.OnClickListener ItemAddCancelListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismissAllowingStateLoss();
        }
    };

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
                    mTextViewFrdigeSettingItemAddRFID.setText(tempStr[1]);
                }
            }
        });
    }

    public void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_SETTING_FRIDGE, "message=" + message);
                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }
}

package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.modal.IOTHome;

public class FragmentSecurityLocked extends Fragment {

    public static final String TAG = FragmentSecurityLocked.class.getSimpleName();

    private ActivityCommunicator mActivityCommunicator;
    private View mView;
    private ImageView mMainImage;
    private TextView mTextLine1;
    private TextView mTextLine2;

    private String mAdminCardNumber = "";

    private DBHelper mUserDBHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_security_locked, container, false);

        mMainImage = (ImageView) mView.findViewById(R.id.imageView_security_main);
        mTextLine1 = (TextView) mView.findViewById((R.id.textView_security_locked));
        mTextLine2 = (TextView) mView.findViewById((R.id.textView_security_locked2));

        mMainImage.setImageResource(R.drawable.security_main_locked);
        mTextLine1.setText(R.string.security_locked);
        mTextLine2.setText(R.string.security_locked2);

        mUserDBHelper = IOTHome.getInstance().getDBHelper();

        mAdminCardNumber = IOTHome.getInstance().getAdminCardNumber(getActivity());
        Log.d(TAG, "Admin Card Number: "+mAdminCardNumber);
        return mView;
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

                if (data.equals(ActivityMainFrame.COMMAND_CONNECTED_DEVICE)) {

                }
                else if (data.equals(ActivityMainFrame.COMMAND_DISCONNECTED_DEVICE)) {

                }
                else if (data.startsWith("RF")) {
                    String tempStr[] = data.split(":");

                    if (tempStr.length < 2) {
                        return;
                    }
                    Log.d(TAG, "RF: " + tempStr[1]);
                    if(mAdminCardNumber == null || mAdminCardNumber.length() == 0 || mAdminCardNumber.equals("null")) {
                        IOTHome.getInstance().setAdminCardNumber(getActivity(), tempStr[1]);
                        mAdminCardNumber = tempStr[1];
                        Log.d(TAG, "set mAdminCardNumber: "+mAdminCardNumber);
                        IOTHome.getInstance().setCurrentUser(getString(R.string.admin_name));
                    }
                    else if(tempStr[1].equals(mAdminCardNumber) || mUserDBHelper.isExistRFID(tempStr[1]) == true) {
                        if(tempStr[1].equals(mAdminCardNumber)) {
                            IOTHome.getInstance().setCurrentUser(getString(R.string.admin_name));
                        } else if(mUserDBHelper.isExistRFID(tempStr[1]) == true) {
                            String userName = mUserDBHelper.getUserNameByRfid(tempStr[1]);
                            Log.d(TAG, "user name: "+userName);
                            IOTHome.getInstance().setCurrentUser(userName);
                        }

                        mActivityCommunicator.passDataToActivity(IOTHome.FRAGMENT_SECURITY_LOCKED, "unlocked");
                    }
                }
            }
        });
    }

    public void showMessage(String message) {
        Log.d(TAG, message);
    }
}

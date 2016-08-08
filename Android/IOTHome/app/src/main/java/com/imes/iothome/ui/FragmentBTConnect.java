package com.imes.iothome.ui;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.modal.IOTHome;

import java.text.DateFormat;
import java.util.Date;

public class FragmentBTConnect extends DialogFragment {
    public static final String TAG = FragmentBTConnect.class.getSimpleName();

    Context mContext;

    private ActivityCommunicator mActivityCommunicator;

    private AnimationDrawable mFrameAnimation;
    private ImageView mImageView;
    private TextView mTextView_popup_ui_title;
    private TextView mTextView_popup_ui_body;

    private boolean mConnectRuning;

    public FragmentBTConnect() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_popup_loading, container);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        getDialog().setCanceledOnTouchOutside(false);

        mImageView = (ImageView) view.findViewById(R.id.imageView_popup_ui_animation);
        mImageView.setBackgroundResource(R.drawable.loading_animation);
        mFrameAnimation = (AnimationDrawable) mImageView.getBackground();

        mImageView.setVisibility(View.VISIBLE);
        mFrameAnimation.setOneShot(false);
        mFrameAnimation.start();

        mTextView_popup_ui_title = (TextView) view.findViewById(R.id.TextView_popup_ui_title);
        mTextView_popup_ui_body = (TextView) view.findViewById(R.id.TextView_popup_ui_body);

        mTextView_popup_ui_title.setText("");
        mTextView_popup_ui_body.setText(R.string.bluetooth_connecting);

        return view;
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        mContext = getActivity();
        mActivityCommunicator = (ActivityCommunicator) mContext;
    }

    @Override
    public void onDestroyView () {
        super.onDestroyView();

        mFrameAnimation.stop();
        mFrameAnimation.setVisible(false, true);
        mFrameAnimation.selectDrawable(0);
        mImageView.setVisibility(View.INVISIBLE);
        mImageView.setBackgroundResource(0);
    }

    @Override
    public void onDetach () {
        super.onDetach();

    }

    public void showMessage(final String message) {

    }

    public void sendMessage(String message) {
        String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
        Log.d(TAG, "[" + currentDateTimeString + "]" + message);
    }
}

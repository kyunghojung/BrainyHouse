package com.ents.brainyhouse.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.Date;

import com.ents.brainyhouse.R;

public class RoomFragment extends Fragment {

    public static final String TAG = RoomFragment.class.getSimpleName();;

    private ListView mMessageListView;
    private ArrayAdapter<String> mListAdapter;
    private Button mBtnSend;
    private EditText mEditMessage;
    private TextView mTitle;

    private String mFragmentName;
    private ActivityCommunicator mActivityCommunicator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_room, container, false);

        mMessageListView = (ListView) view.findViewById(R.id.listMessage);
        mListAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message_detail);
        mMessageListView.setAdapter(mListAdapter);
        mMessageListView.setDivider(null);
        mBtnSend =(Button) view.findViewById(R.id.sendButton);
        mEditMessage = (EditText) view.findViewById(R.id.sendText);
        mTitle = (TextView) view.findViewById(R.id.titleText);

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mEditMessage.getText().toString();
                mActivityCommunicator.passDataToActivity(mFragmentName, message);
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

    public void setFragmentName(String name) {
        mFragmentName = name;
        Log.d(TAG, "setFragmentName: "+mFragmentName);
        mTitle.setText(mFragmentName);
    }

    public void setData(String data) {
        showMessage(data);
    }

    private void showMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                try {
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                    mListAdapter.add("[" + currentDateTimeString + "] " + message);
                    mMessageListView.smoothScrollToPosition(mListAdapter.getCount() - 1);

                } catch (Exception e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
    }

}

package com.imes.iothome.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.database.UserItem;
import com.imes.iothome.modal.IOTHome;

import java.util.ArrayList;

public class AdapterUserData extends BaseAdapter {
    public static final String TAG = AdapterUserData.class.getSimpleName();

    private Context mContext;
    private int mLayout;
    private LayoutInflater mInflater;

    private ArrayList<UserItem> mData = new ArrayList<UserItem>();

    DBHelper mUserDB;

    private DataChangedListener mDataChangedListener;

    public void setDataChangeListener(DataChangedListener listener) {
        mDataChangedListener = listener;
    }

    public AdapterUserData(Context context, int layout, ArrayList<UserItem> data) {
        this.mContext = context;
        this.mLayout = layout;
        this.mData = data;

        mInflater = (LayoutInflater) mContext.getSystemService(context.LAYOUT_INFLATER_SERVICE);

        mUserDB = IOTHome.getInstance().getDBHelper();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mData.get(position).getID();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mInflater.inflate(mLayout, parent, false);
        }

        Log.d(TAG, "position: " + position);

        TextView userName = (TextView) convertView.findViewById(R.id.textView_user_name);
        userName.setText(mData.get(position).getName());

        TextView userRfid = (TextView) convertView.findViewById(R.id.textView_user_rfid);
        userRfid.setText(mData.get(position).getRFID());

        ImageView userDelete = (ImageView) convertView.findViewById(R.id.imageView_user_delete);
        userDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteUser(mData.get(position).getName());
                mDataChangedListener.deletedData(mData.get(position).getID());
            }
        });

        return convertView;
    }

    public void setChangedData(ArrayList<UserItem> data) {
        this.mData = data;
    }

    void deleteUser(String name) {
        Log.d(TAG, "deleteUser name: " + name);
        mUserDB.deleteUser(name);
    }

}
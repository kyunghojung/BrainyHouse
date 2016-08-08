package com.imes.iothome.ui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.imes.iothome.R;
import com.imes.iothome.database.DBHelper;
import com.imes.iothome.database.FridgeItem;
import com.imes.iothome.modal.IOTHome;

import java.util.ArrayList;

public class AdapterFridgeItem extends BaseAdapter{
    public static final String TAG = AdapterFridgeItem.class.getSimpleName();

    Context mContext;
    LayoutInflater mLayoutInflater;
    int mLayout;

    private ArrayList<FridgeItem> mFridgeItems;

    DBHelper mFridgeItemDB;

    private DataChangedListener mDataChangedListener;

    public void setDataChangeListener(DataChangedListener listener) {
        mDataChangedListener = listener;
    }

    public AdapterFridgeItem(Context context, int layout, ArrayList<FridgeItem> items) {
        mContext = context;
        mLayout = layout;
        mFridgeItems = items;
        mFridgeItemDB = IOTHome.getInstance().getDBHelper();

        mLayoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    public void updateItems(ArrayList<FridgeItem> items) {
        mFridgeItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mFridgeItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mFridgeItems.get(position).getName();
    }

    @Override
    public long getItemId(int position) {
        return mFridgeItems.get(position).getID();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = mLayoutInflater.inflate(mLayout, parent, false);
        }

        Log.d(TAG, "position: " + position);

        TextView textViewName = (TextView) convertView.findViewById(R.id.textView_adapter_fridge_item_name);
        TextView textViewShelfLife = (TextView) convertView.findViewById(R.id.textView_adapter_fridge_shelf_life);
        TextView textViewAmount = (TextView) convertView.findViewById(R.id.textView_adapter_fridge_item_amount);

        textViewName.setText(mFridgeItems.get(position).getName());
        textViewShelfLife.setText(mFridgeItems.get(position).getShelfLife());
        textViewAmount.setText(String.valueOf(mFridgeItems.get(position).getAmount()));

        return convertView;
    }

    public void setChangedData(ArrayList<FridgeItem> items) {
        mFridgeItems = items;
    }
}

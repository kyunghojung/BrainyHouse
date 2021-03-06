package com.ents.brainyhouse.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.ents.brainyhouse.R;
import com.ents.brainyhouse.database.FridgeItem;

import java.util.ArrayList;

public class SettingFridgeItemAdapter extends BaseAdapter{
    Context mContext;
    LayoutInflater mLayoutInflater;
    int mLayout;

    ArrayList<FridgeItem> mFridgeItems;

    public SettingFridgeItemAdapter(Context context, int layout, ArrayList<FridgeItem> items) {
        mContext = context;
        mLayoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        mLayout = layout;
        mFridgeItems = items;
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
        TextView textViewName = (TextView) convertView.findViewById(R.id.textViewFridgeSettingDetailItemName);
        TextView textViewRFID = (TextView) convertView.findViewById(R.id.textViewFridgeSettingDetailItemRFID);

        textViewName.setText(mFridgeItems.get(position).getName());
        textViewRFID.setText(mFridgeItems.get(position).getRFID());

        return convertView;
    }
}

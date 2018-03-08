package com.pg.software.controller.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pg.software.controller.R;

import java.util.ArrayList;

/**
 * Created by Freedom on 2017/10/25.
 */
public class BTAdapter extends BaseAdapter {

    private ArrayList<String> mData;
    private LayoutInflater inflater;

    public BTAdapter(Context mContext,ArrayList<String> mData){
        this.mData=mData;
        inflater=LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int i) {
        return mData.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder mHolder = null;
        if (view==null){
            mHolder=new ViewHolder();
            view=inflater.inflate(R.layout.itemadapter,null);
            mHolder.mTextView= (TextView) view.findViewById(R.id.itemText);
            view.setTag(mHolder);
        }else{
            mHolder= (ViewHolder) view.getTag();
        }
        mHolder.mTextView.setText(mData.get(i));
        return view;
    }

    class ViewHolder{
         private TextView mTextView;

    }
}

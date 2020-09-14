package com.sivaram.contacttracing.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.sivaram.contacttracing.R;
import com.sivaram.contacttracing.helpers.TracedContact;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<TracedContact> traces;

    public ListAdapter(Context context, ArrayList<TracedContact> traces) {
        this.context = context;
        this.traces = traces;
    }

    @Override
    public int getCount() {
        return traces.size();
    }

    @Override
    public Object getItem(int position) {
        return traces.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = (View) inflater.inflate(R.layout.row_layout, null);
        }

        TextView textViewRssi = (TextView)convertView.findViewById(R.id.rssi_textView);
        TextView textViewTime=(TextView)convertView.findViewById(R.id.time_textview);
        String curRssi =  traces.get(position).getRssi();
        String curTime = traces.get(position).getTime();

        textViewRssi.setText(String.format("Rssi : %s", curRssi));
        textViewTime.setText(String.format("Time : %s", curTime));

        return convertView;
    }
}
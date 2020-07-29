package com.sivaram.contacttracing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

public class ListAdapter extends BaseAdapter {

    private Context context;
    private List<BluetoothModel> traces;

    public ListAdapter(Context context, List<BluetoothModel> traces) {
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

        TextView textViewContact = (TextView)convertView.findViewById(R.id.trace_contact);
        TextView textViewRssi=(TextView)convertView.findViewById(R.id.trace_rssi);
        TextView textViewTime =(TextView)convertView.findViewById(R.id.trace_time);

        textViewContact.setText(traces.get(position).getContact());
        textViewRssi.setText(traces.get(position).getRssi());
        textViewTime.setText(traces.get(position).getTime());

        return convertView;
    }
}
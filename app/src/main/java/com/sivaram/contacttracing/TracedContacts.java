package com.sivaram.contacttracing;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import static com.sivaram.contacttracing.Utility.deviceDB;


public class TracedContacts extends ListActivity {

    ListView contactsListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_traced_contacts);
        contactsListView = (ListView)findViewById(android.R.id.list);
    }
    @Override
    protected void onResume() {
        super.onResume();
        contactsListView.setAdapter(new ListAdapter(this,deviceDB));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
package com.sivaram.contacttracing;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

public class ContactTracingApplication extends Application {

    public static ContactTracingApplication instance;
    public static Toast toast;
    public static ContactTracingApplication getInstance() {
        return instance;
    }
    public ContactTracingApplication() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
    }

    public Context getContext() {
        return getApplicationContext();
    }
    public static void toastMessage(String message) {
        if(toast!=null){
            toast.cancel();
        }
        toast= Toast.makeText(instance,message,Toast.LENGTH_SHORT);
        toast.show();
    }

}

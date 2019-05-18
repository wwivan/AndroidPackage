package me.factory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MyReceiver extends BroadcastReceiver {
    private Message message;
    private String RECE_DATA_ACTION = "com.se4500.onDecodeComplete";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equals(RECE_DATA_ACTION)) {
            String data = intent.getStringExtra("se4500");
            //Toast.makeText(context, data, Toast.LENGTH_SHORT);
        }
    }

    interface Message {
        public void getMsg(String str);
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
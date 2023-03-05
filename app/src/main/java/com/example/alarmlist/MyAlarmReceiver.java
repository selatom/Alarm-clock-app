package com.example.alarmlist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class MyAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

        Toast.makeText(context, "The alarm is on", Toast.LENGTH_SHORT).show();
        Intent intent1 = new Intent(context, AlarmActivity.class);
        context.startActivity(intent1);
    }
}
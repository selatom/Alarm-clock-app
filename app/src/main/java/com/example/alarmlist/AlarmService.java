package com.example.alarmlist;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AlarmService extends Service {
    private Timer timer;

    /**
     *This method use Timer class and recalculate the hour and dat every minute.
     * if the current date is equal to one of the alarms date.
     * if it does, it starts the alarm.
     *
     * @param alarms list of all active alarms
     */
    private void calculateTime (ArrayList<String>alarms) {
        timer = new Timer();

        // Check every minute if the current hour is in the alarm list
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {

                // Get the current date in a form of: 2022-05-13 06:37
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);
                Date date = Calendar.getInstance().getTime();

                String currentDate = format.format(date);

                // If the current time is in the alarm list, start alarm and update list
                if (alarms.contains(currentDate)) {
                    startRingtone(currentDate);

                    alarms.remove(currentDate);
                    updateDatabase(currentDate);

                    if (alarms.size() == 0) {
                        // No alarms left in the list, stop the service
                        stopService();
                    }
                }
            }
        }, 0, 6000);
    }

    //The alarm is on, notify the user
    private void startRingtone(String time) {

        // Start vibrator
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

        // create an notification about the meaning of the alarm
        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(time)
                .setContentText("The alarm for " + time + "is on!")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .build();

        // Display the notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        manager.notify(0, notification);

        // Send a broadcast to notify the main thread about the alarm
        Intent intent = new Intent();
        intent.setAction("com.example.alarmlist");
        intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);

        // Start the ringtone
        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ringtone.setLooping(false);
        }
        ringtone.play();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (action.equals(Constants.START_ALARM_SERVICE)) {
                //Start service
                startAlarmService();
            } else {
                stopService();
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void stopService() {
        //Stop service
        stopForeground(true);
        this.stopSelf();

        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * This method notify the user about the running service and starts the service task
     */
    public void startAlarmService () {
        String channel_id = "Alarm_notification_channel";
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                resultIntent,
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(),
                channel_id);

        builder.setContentTitle("Alarm Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("ALARMS");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ((manager != null)
                    && (manager.getNotificationChannel(channel_id) == null)) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channel_id,
                        "Alarm_service",
                        NotificationManager.IMPORTANCE_HIGH
                );

                notificationChannel.setDescription("This channel is used by alarm services");
                manager.createNotificationChannel(notificationChannel);
            }
        }

        downloadActiveAlarms();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * open sql and download the active alarms into a list
     */
    private void downloadActiveAlarms() {
        ArrayList<String>alarms = new ArrayList<>();

        ContentResolver contentResolver = getContentResolver();
        String selection = ContractAlarm.TABLE_NAME + "." + ContractAlarm.Columns.STATUS + " = '" + "ON'";
        String[]projections = {ContractAlarm.Columns.DATE};

        Cursor cursor = contentResolver.query(ContractAlarm.CONTENT_URI, projections, selection, null, null);

        while (cursor.moveToNext()) {
            alarms.add(cursor.getString(cursor.getColumnIndexOrThrow(ContractAlarm.Columns.DATE)));
        }

        cursor.close();

        if (alarms.size() > 0) {
            calculateTime(alarms);
        }
        else {
            stopService();
        }
    }

    /**
     * Finds the wanted column un sql and updates it status
     * @param date the string selection
     */
    private void updateDatabase(String date) {
        ContentResolver contentResolver = getContentResolver();
        String selection = ContractAlarm.TABLE_NAME + "." + ContractAlarm.Columns.DATE + " = '" + date + "'";
        ContentValues values = new ContentValues();

        values.put(ContractAlarm.Columns.STATUS, "OFF");

        contentResolver.update(ContractAlarm.CONTENT_URI, values, selection, null);
    }
}

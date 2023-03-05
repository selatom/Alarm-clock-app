package com.example.alarmlist;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.ActivityManager;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewAlarmFragment extends Fragment {
    private TimePicker timePicker;
    private EditText name;
    private static String currentDate;
    private static Calendar calendar;

    private AlarmInfo alarmInfo;

    public NewAlarmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            alarmInfo = (AlarmInfo) getArguments().getSerializable("INFO");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_new_alarm, container, false);

        timePicker = view.findViewById(R.id.timePicker);
        TextView date = view.findViewById(R.id.add_date);
        name = view.findViewById(R.id.add_name);
        TextView save = view.findViewById(R.id.add_save);
        TextView cancel = view.findViewById(R.id.add_cancel);
        ImageView display_cal = view.findViewById(R.id.add_change_date);

        calendar = Calendar.getInstance();

        SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM", Locale.US);
        currentDate = format.format(calendar.getTime());

        if (alarmInfo == null) {
            date.setText(currentDate);
            timePicker.setHour(calendar.get(Calendar.HOUR_OF_DAY));
        } else {
            date.setText(alarmInfo.getDay());
            name.setText(alarmInfo.getName());

            String[] time = alarmInfo.getHour().split(":");
            timePicker.setHour(Integer.parseInt(time[0]));
            timePicker.setMinute(Integer.parseInt(time[1]));
        }

        save.setOnClickListener(v -> {
            String nameOfAlarm = name.getText().toString();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            calendar.set(year, month, day, hour, minute);

            String fullHour = hour + ":" + minute;
            if (minute < 10) {
                fullHour = hour + ":0" + minute;
            }
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd hh:mm", Locale.US);
            String fullDate = format2.format(calendar.getTime());
            Log.d(TAG, "onCreateView: " + fullDate);

            insertToDatabase(fullHour, currentDate, nameOfAlarm, fullDate);
            cancelFragment();
        });

        cancel.setOnClickListener(v -> cancelFragment());

        display_cal.setOnClickListener(view1 -> {
            MyDatePickerDialog pickerDialog = new MyDatePickerDialog(requireActivity(),
                    null,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            pickerDialog.show();
        });


        return view;
    }

    private void insertToDatabase(String fullHour, String currentDate, String nameOfAlarm, String fullDate) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        ContentValues values = new ContentValues();

        values.put(ContractAlarm.Columns.DATE, fullDate);
        values.put(ContractAlarm.Columns.NAME, nameOfAlarm);
        values.put(ContractAlarm.Columns.DAY, currentDate);
        values.put(ContractAlarm.Columns.HOUR, fullHour);
        values.put(ContractAlarm.Columns.STATUS, "ON");
        values.put(ContractAlarm.Columns.FIXED, "NO");

        contentResolver.insert(ContractAlarm.CONTENT_URI, values);

        //Start the service with the new list of alarms
        if (isMyServiceOn()) {
            stopService();
        }

        startService();
    }

    //This method discard/close the add-new-fragment
    private void cancelFragment() {
        FragmentManager manager = requireActivity().getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();

        Fragment fragment = manager.findFragmentByTag("main");

        transaction.remove(this);
        if (fragment != null)
            transaction.show(fragment);
        transaction.commit();
    }

    //Check if the alarms service is on
    public boolean isMyServiceOn(){
        ActivityManager activityManager =
                (ActivityManager) requireActivity().getSystemService(Context.ACTIVITY_SERVICE);
        if(activityManager != null){
            for(ActivityManager.RunningServiceInfo serviceInfo :
                    activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if(AlarmService.class.getName().equals(serviceInfo.service.getClassName())){
                    if(serviceInfo.foreground){
                        return false;
                    }
                }
            }
            return true;
        }
        return true;
    }

    private void startService() {
        Intent intent = new Intent(requireActivity().getApplicationContext(), AlarmService.class);
        intent.setAction(Constants.START_ALARM_SERVICE);
        requireActivity().startService(intent);
    }

    private void stopService() {
        Intent intent = new Intent(requireActivity().getApplicationContext(), AlarmService.class);
        intent.setAction(Constants.STOP_ALARM_SERVICE);
        requireActivity().startService(intent);
    }

    /**
     *Display date picker dialog. the user can choose any date he wants.
     */
    public static class MyDatePickerDialog extends DatePickerDialog {

        private String title;

        public MyDatePickerDialog(Context context, DatePickerDialog.OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
            super(context, callBack, year, monthOfYear, dayOfMonth);
        }

        private void createTitle() {
            SimpleDateFormat format = new SimpleDateFormat("EEEE, d MMM", Locale.US);
            this.title = format.format(calendar.getTime());
            currentDate = this.title;
            setTitle(this.title);
        }

        @Override
        public void onDateChanged(DatePicker view, int year, int month, int day) {
            super.onDateChanged(view, year, month, day);
            calendar.set(year, month, day);
            createTitle();
            setTitle(title);
        }
    }

}
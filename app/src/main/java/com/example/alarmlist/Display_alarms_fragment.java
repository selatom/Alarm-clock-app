package com.example.alarmlist;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Objects;

public class Display_alarms_fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, CursorRecyclerViewAdapter.OnAlarmClick {
    private static final int LOADER_ID = 1;
    private static final int VERTICAL_ITEM_SPACE = 48;

    private Context context;

    private CursorRecyclerViewAdapter mAlarmAdapter;

    public Display_alarms_fragment() {
        // Required empty public constructor
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_display_alarms_fragment, container, false);
        RecyclerView list = view.findViewById(R.id.alarm_list);
        ImageView addAlarm = view.findViewById(R.id.add_alarm);

        list.setLayoutManager(new LinearLayoutManager(getActivity()));
        list.addItemDecoration(new VerticalSpaceItemDecoration(VERTICAL_ITEM_SPACE));

        if (mAlarmAdapter == null) {
            mAlarmAdapter = new CursorRecyclerViewAdapter(null, this);
        }

        list.setAdapter(mAlarmAdapter);
        LoaderManager.getInstance(requireActivity()).initLoader(LOADER_ID, null, this);

        addAlarm.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

            transaction.add(R.id.fragmentContainerView, new NewAlarmFragment(), "add");
            transaction.hide(Display_alarms_fragment.this);
            transaction.addToBackStack("main");
            transaction.commit();
        });

        return view;
    }

    //Check if the alarms service is on
    public boolean isMyServiceOn(){
        ActivityManager activityManager =
                (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
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

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        if (id == LOADER_ID) {
            return new CursorLoader(context, ContractAlarm.CONTENT_URI, null, null, null, null);
        } else {
            throw new IllegalArgumentException("Invalid loader id " +id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mAlarmAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mAlarmAdapter.swapCursor(null);
    }

    @Override
    public void onAlarmOn(AlarmInfo alarm) {
            ContentResolver contentResolver = requireActivity().getContentResolver();
            ContentValues values = new ContentValues();
            String selection = ContractAlarm.TABLE_NAME + "." + ContractAlarm.Columns._ID + " = " + alarm.getId();

            values.put(ContractAlarm.Columns.STATUS, "ON");

            contentResolver.update(ContractAlarm.CONTENT_URI, values, selection, null);

            //Start the service again with the new alarms list
            if (isMyServiceOn()) {
                stopService();
            }

            startService();
    }

    @Override
    public void onAlarmOff(AlarmInfo alarm) {
        ContentResolver contentResolver = requireActivity().getContentResolver();
        ContentValues values = new ContentValues();
        String selection = ContractAlarm.TABLE_NAME + "." + ContractAlarm.Columns._ID + " = " + alarm.getId();

        values.put(ContractAlarm.Columns.STATUS, "OFF");

        contentResolver.update(ContractAlarm.CONTENT_URI, values, selection, null);


        //Start the service again with the new alarm list
        if (isMyServiceOn()) {
            stopService();
        }

        startService();
    }

    @Override
    public void onClick(AlarmInfo alarm) {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();

        transaction.add(R.id.fragmentContainerView, new NewAlarmFragment(), "add");
        transaction.hide(Display_alarms_fragment.this);
        transaction.addToBackStack("main");
        transaction.commit();
    }
}
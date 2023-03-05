package com.example.alarmlist;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

public class CursorRecyclerViewAdapter extends RecyclerView.Adapter<CursorRecyclerViewAdapter.ViewHolder>{
    private Cursor mCursor;
    private OnAlarmClick mAlarmClick;

    public interface OnAlarmClick {
        void onAlarmOn(AlarmInfo alarm);
        void onAlarmOff(AlarmInfo alarm);
        void onClick(AlarmInfo alarm);
    }

    public CursorRecyclerViewAdapter(Cursor mCursor, OnAlarmClick mAlarmClick) {
        this.mCursor = mCursor;
        this.mAlarmClick = mAlarmClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.alarm_detail, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if ((mCursor) == null || (mCursor.getCount() == 0)) {
            //There are no active nor passive alarms.
            holder.date.setVisibility(View.GONE);
            holder.time.setVisibility(View.GONE);
            holder.switchBTN.setVisibility(View.GONE);
        } else {
            if (!mCursor.moveToPosition(position)) {
                throw new IllegalArgumentException("Position " + position + "do not exist");
            }

            //Display alarms detail
            AlarmInfo alarmInfo = new AlarmInfo(
                  mCursor.getInt(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns._ID)),
                  mCursor.getString(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns.NAME)),
                  mCursor.getString(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns.DAY)),
                  mCursor.getString(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns.DATE)),
                  mCursor.getString(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns.HOUR)),
                  mCursor.getString(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns.STATUS)),
                  mCursor.getString(mCursor.getColumnIndexOrThrow(ContractAlarm.Columns.FIXED)));

            holder.date.setVisibility(View.VISIBLE);
            holder.time.setVisibility(View.VISIBLE);
            holder.switchBTN.setVisibility(View.VISIBLE);

            holder.time.setText(alarmInfo.getHour());
            holder.date.setText(alarmInfo.getDay());

            holder.switchBTN.setChecked(alarmInfo.getStatus().equals("ON"));
//            switch (alarmInfo.getStatus()) {
//                case "ON":
//                    holder.switchBTN.setChecked(true);
//                    break;
//                case "OFF":
//                    holder.switchBTN.setChecked(false);
//                    break;
//            }

            holder.switchBTN.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    mAlarmClick.onAlarmOn(alarmInfo);
                } else {
                    mAlarmClick.onAlarmOff(alarmInfo);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        if ((mCursor == null) || (mCursor.getCount() == 0)) {
            return 1;
        }

        return mCursor.getCount();
    }


    //The user scroll the list and therefore we need to display other alarm
    public Cursor swapCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
            return null;
        }

        int count = getItemCount();
        Cursor oldCursor = mCursor;
        mCursor = newCursor;

        if (newCursor != null) {
            //Need to display new alarm
            notifyDataSetChanged();
        } else {
            //Remove from display
            notifyItemRangeRemoved(0, count);
        }

        return oldCursor;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView time;
        private final TextView date;
        private final SwitchCompat switchBTN;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.time = itemView.findViewById(R.id.alarmdetail_time);
            this.date = itemView.findViewById(R.id.alarmdetail_date);
            this.switchBTN = itemView.findViewById(R.id.alarmdetail_switch);
        }
    }
}

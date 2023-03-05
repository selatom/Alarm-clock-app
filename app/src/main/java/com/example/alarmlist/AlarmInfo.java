package com.example.alarmlist;

import java.io.Serializable;

public class AlarmInfo implements Serializable {
    private final int id;
    private final String name;
    private final String day;
    private final String date;
    private final String hour;
    private final String status;
    private final String fixed;

    public AlarmInfo(int id, String name, String day, String date, String hour, String status, String fixed) {
        this.id = id;
        this.name = name;
        this.day = day;
        this.date = date;
        this.hour = hour;
        this.status = status;
        this.fixed = fixed;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDay() {
        return day;
    }

    public String getDate() {
        return date;
    }

    public String getHour() {
        return hour;
    }

    public String getStatus() {
        return status;
    }

    public String getFixed() {
        return fixed;
    }
}

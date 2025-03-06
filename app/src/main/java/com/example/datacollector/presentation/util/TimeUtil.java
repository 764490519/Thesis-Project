package com.example.datacollector.presentation.util;

import java.text.SimpleDateFormat;

public class TimeUtil {

    public static String formatDateTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String date = format.format(time);
        return date;
    }
}

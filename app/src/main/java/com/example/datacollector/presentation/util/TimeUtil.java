package com.example.datacollector.presentation.util;

import java.text.SimpleDateFormat;

public class TimeUtil {

    //将毫秒转换为时间周期，如xx m xx s xx ms
    public static String formatDuration(long mileSeconds) {
        long ms = mileSeconds % 1000;
        long s = mileSeconds / 1000 % 60;
        long m = mileSeconds / 1000 / 60;
        if (m != 0){
            return m + " m " + s + " s " + ms + " ms";
        }else if( s != 0){
            return s + " s " + ms + " ms";
        }else {
            return ms + " ms";
        }
    }

    public static String formatDate(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String date = format.format(time);
        return date;
    }
}

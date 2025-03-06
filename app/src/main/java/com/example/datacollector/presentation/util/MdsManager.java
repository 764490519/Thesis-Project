package com.example.datacollector.presentation.util;

import android.content.Context;
import android.util.Log;

import com.example.datacollector.presentation.MoveSense.bluetooth.MdsRx;
import com.example.datacollector.presentation.MoveSense.model.TimeDetailedModel;
import com.example.datacollector.presentation.MoveSense.model.TimeModel;
import com.example.datacollector.presentation.MoveSense.utils.FormatHelper;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsHeader;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

public class MdsManager {

    private final String TAG = "MdsManagerUserDebug";
    private final String PREFIX = "suunto://";
    private Context context;
    private MovesenseDevice device;
    private String serial;
    private long startTime;
    private long delay;

    public MdsManager(Context context,MovesenseDevice device){
        this.context = context;
        this.device = device;
        this.serial = device.getSerial();
        resetTime(null);
        getTimeDetailed(new MdsResponseListener() {
            @Override
            public void onError(MdsException e) {
                Log.e(TAG,e.getMessage());
            }
            @Override
            public void onSuccess(String data, MdsHeader header) {
             TimeDetailedModel timeDetailedModel = new Gson().fromJson(data,TimeDetailedModel.class);
             long timestamp = timeDetailedModel.content.utcTime / 1000;
             long relativeTime = timeDetailedModel.content.relativeTime;
             long currentTime = System.currentTimeMillis();
             startTime = timestamp - relativeTime;
             delay = (currentTime - timestamp)/2;

            }
        });
    }

    public long getStartTime(){
        return startTime;
    }



    public void resetTime(MdsResponseListener listener){
        put("/Time","{\"value\":" + System.currentTimeMillis() * 1000 + "}",listener != null ? listener : new MdsResponseListener(){
            @Override
            public void onError(MdsException e) {
                Log.e(TAG,e.getMessage());
            }
            @Override
            public void onSuccess(String data, MdsHeader header) {
                Log.d(TAG,data);
            }
        });
    }

    public void getTimeDetailed(MdsResponseListener listener){
        get("/Time/Detailed",listener != null ? listener : new MdsResponseListener(){
            @Override
            public void onError(MdsException e) {
                Log.e(TAG,e.getMessage());
            }
            @Override
            public void onSuccess(String data, MdsHeader header) {
                Log.d(TAG,data);
                TimeDetailedModel timeDetailedModel = new Gson().fromJson(data,TimeDetailedModel.class);
                long timestamp = timeDetailedModel.content.utcTime / 1000;
                Log.d(TAG,TimeUtil.formatDateTime(timestamp));

            }
        });
    }

    public MdsSubscription subscribeTimeDetailed(MdsNotificationListener listener){
        return subscribe("Time",listener != null ? listener : new MdsNotificationListener(){
            @Override
            public void onError(MdsException e) {
                Log.e(TAG,e.getMessage());
            }

            @Override
            public void onNotification(String data) {
                Log.d(TAG,data);
            }
        });
    }

    public MdsSubscription subscribeImu(int rate, MdsNotificationListener listener){
        return subscribe("Meas/IMU9/" + rate,listener != null ? listener : new MdsNotificationListener(){
            @Override
            public void onError(MdsException e) {
                Log.e(TAG,e.getMessage());
            }

            @Override
            public void onNotification(String data) {
                Log.d(TAG,data);
            }
        });
    }


    public void get(String uri, MdsResponseListener listener) {
        Mds.builder().build(context).get(PREFIX + serial + uri, null,listener);
    }

    public void put(String uri, String contract, MdsResponseListener listener) {
        Mds.builder().build(context).put(PREFIX + serial + uri, contract, listener);
    }

    public MdsSubscription subscribe(String uri, MdsNotificationListener listener) {
        return Mds.builder().build(context).subscribe(PREFIX + "MDS/EventListener", FormatHelper.formatContractToJson(serial, uri), listener);
    }





}

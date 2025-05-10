package com.example.datacollector.presentation.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.datacollector.presentation.model.ImuModel;
import com.example.datacollector.presentation.model.Interval;
import com.example.datacollector.presentation.model.TimeDetailedModel;
import com.example.datacollector.presentation.model.TimeModel;
import com.example.datacollector.presentation.movesense.utils.FormatHelper;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsHeader;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MdsManager {

    private final String TAG = "MdsManagerUserDebug";
    private final String PREFIX = "suunto://";

    private MdsSubscription mdsSubscription;
    private MdsResponseListener mdsResponseListener;
    private long sendTime;
    private Context context;
    private MovesenseDevice device;
    private String serial;
    private long startTime;
    private long delay;
    private int repeatTimes = 10;



    public MdsManager(Context context,MovesenseDevice device){
        this.context = context;
        this.device = device;
        this.serial = device.getSerial();
        resetTime(null);
        getTimeDetailed(new MdsResponseListener() {
            @Override
            public void onError(MdsException e) {
                Log.d(TAG,e.getMessage());
            }
            @Override
            public void onSuccess(String data, MdsHeader header) {
                TimeDetailedModel timeDetailedModel = new Gson().fromJson(data,TimeDetailedModel.class);
                long timestamp = timeDetailedModel.content.utcTime / 1000;
                long relativeTime = timeDetailedModel.content.relativeTime;
                startTime = timestamp - relativeTime;
                long receiveTime = System.currentTimeMillis();
                delay = receiveTime - timestamp;
            }
        });



        List<Interval> intervals = new ArrayList<>();
        AtomicInteger count = new AtomicInteger(0);
        mdsResponseListener = new MdsResponseListener() {
            @Override
            public void onError(MdsException e) {
                Log.d(TAG,e.getMessage());
            }
            @Override
            public void onSuccess(String data, MdsHeader header) {
                TimeDetailedModel timeDetailedModel = new Gson().fromJson(data,TimeDetailedModel.class);
                long timestamp = timeDetailedModel.content.utcTime / 1000;
                long receiveTime = System.currentTimeMillis();
                long RTT = receiveTime - sendTime;
                long calculatedDelay = (sendTime + receiveTime) / 2 - timestamp;
                Log.d(TAG,"send time" + sendTime + ", server time: " + timestamp + ", receive time: " + receiveTime);
                Log.d(TAG,"RTT: " + RTT + ", Delay: " + calculatedDelay);
                Log.d(TAG, "Range: (" + (calculatedDelay - RTT/2) + "," + (calculatedDelay + RTT/2) + ")");
                intervals.add(new Interval((calculatedDelay - RTT/2), (calculatedDelay + RTT/2)));
                int completed = count.incrementAndGet();
                if(completed < repeatTimes){
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(()->{
                        sendTime = System.currentTimeMillis();
                        getTimeDetailed(mdsResponseListener);
                    }, 50);
                }else {
                    Interval bestInterval = Interval.getMostConfidentInterval(intervals);
                    long estimatedDelay = (bestInterval.start + bestInterval.end) / 2;
//                    long estimatedDelay = bestInterval.start;
                    delay = estimatedDelay;
                    Log.d(TAG, "Most confident delay interval: (" + bestInterval.start + ", " + bestInterval.end + ")");
                    Log.d(TAG, "Estimated network delay: " + estimatedDelay);
                }
            }
        };

        sendTime = System.currentTimeMillis();
        getTimeDetailed(mdsResponseListener);


    }

    public long getStartTime(){
        return startTime;
    }

    public long getDelay(){
        return delay;
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
                Log.d(TAG,TimeUtil.formatDate(timestamp));

            }
        });
    }

    public MdsSubscription subscribeTimeDetailed(MdsNotificationListener listener){
        return subscribe("Time/Detailed",listener != null ? listener : new MdsNotificationListener(){
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

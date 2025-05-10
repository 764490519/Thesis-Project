package com.example.datacollector.presentation.activities.test;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.datacollector.R;
import com.example.datacollector.presentation.model.AccelerometerModel;
import com.example.datacollector.presentation.model.GyroscopeModel;
import com.example.datacollector.presentation.model.ImuModel;
import com.example.datacollector.presentation.model.SensorData;
import com.example.datacollector.presentation.model.TimeDetailedModel;
import com.example.datacollector.presentation.services.AccelerometerForegroundService;
import com.example.datacollector.presentation.services.GyroscopeForegroundService;
import com.example.datacollector.presentation.services.HeartRateForegroundService;
import com.example.datacollector.presentation.services.LocationForeGroundService;
import com.example.datacollector.presentation.util.CsvLogger;
import com.example.datacollector.presentation.util.MdsManager;
import com.example.datacollector.presentation.util.PermissionUtil;
import com.example.datacollector.presentation.util.TimeUtil;
import com.google.gson.Gson;
import com.movesense.mds.Mds;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsHeader;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsResponseListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

import java.util.Locale;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Test5Activity extends ComponentActivity {

    private final String TAG = "TestActivityUserDebug";
    private MdsSubscription mdsSubscription;

    MovesenseDevice device;
    MdsManager mdsManager;
    private boolean isRecording = false;
    private long diffSum;
    private long dataCount;
    private int samplingRate = 52;
    private final long RECORDING_DURATION = 1 * 60 * 60 * 1000;//

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        device = MovesenseConnectedDevices.getConnectedDevice(0);
        mdsManager = new MdsManager(this, device);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording(RECORDING_DURATION);
            }
        });
        findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecording();
            }
        });
    }

    private void startRecording(long duration) {
        startRecording();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopRecording();
            }
        }, duration);
    }


    private void startRecording() {
        if (isRecording) {
            Log.d(TAG, "Already Recording");
            Toast.makeText(Test5Activity.this, "Already Recording", Toast.LENGTH_SHORT).show();
            return;
        }
        diffSum = 0;
        dataCount = 0;
        isRecording = true;
        subscribeImuData();
        Toast.makeText(Test5Activity.this, "Started Recording", Toast.LENGTH_SHORT).show();
    }


    private void stopRecording() {
        if (!isRecording) {
            Log.d(TAG, "Not Recording");
            Toast.makeText(Test5Activity.this, "Not Recording", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Stop Recording");
        isRecording = false;
        unSubscribeImuData();

    }


    public void subscribeImuData() {
        Log.d(TAG, "Name: " + device.getName() + " MacAddress: " + device.getMacAddress());

        CsvLogger csvLogger = new CsvLogger(this,"test5_" + samplingRate + ".csv");
        csvLogger.logData("timestamp,diff");

        mdsSubscription = mdsManager.subscribeImu(samplingRate, new MdsNotificationListener() {
            @Override
            public void onNotification(String data) {

                ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);
                long timestamp = imuModel.getBody().getTimestamp() + mdsManager.getStartTime() + mdsManager.getDelay();;
                long time = System.currentTimeMillis();
                long diff = time - timestamp;
                Log.d(TAG, "timestamp: " + timestamp + ", time: " + time + ", diff: " + diff);
                csvLogger.logData(timestamp + "," + diff);
                diffSum += diff;
                dataCount++;
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, e.getMessage());
            }
        });

    }

    public void unSubscribeImuData() {
        mdsSubscription.unsubscribe();
        Log.d(TAG, "Average diff: " + (diffSum / dataCount));
        Log.d(TAG, "Data Count: " + dataCount);
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
//        if (requestCode == REQUEST_CODE_HEARTRATE) {
//            if (PermissionUtil.hasPermission(this, Manifest.permission.BODY_SENSORS)) {
//                subscribeHeartRateData();
//            } else {
//                Log.w(TAG, "Heart Rate Permission Denied");
//            }
//        }
//
//        if (requestCode == REQUEST_CODE_LOCATION) {
//            if (PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
//                subscribeLocationData();
//            } else {
//                Log.w(TAG, "Location Permission Denied");
//            }
//        }
    }

}
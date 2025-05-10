package com.example.datacollector.presentation.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
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
import com.example.datacollector.presentation.services.AccelerometerForegroundService;
import com.example.datacollector.presentation.services.GyroscopeForegroundService;
import com.example.datacollector.presentation.services.HeartRateForegroundService;
import com.example.datacollector.presentation.services.LocationForeGroundService;
import com.example.datacollector.presentation.util.CsvLogger;
import com.example.datacollector.presentation.util.MdsManager;
import com.example.datacollector.presentation.util.PermissionUtil;
import com.example.datacollector.presentation.util.TimeUtil;
import com.google.gson.Gson;
import com.movesense.mds.MdsException;
import com.movesense.mds.MdsNotificationListener;
import com.movesense.mds.MdsSubscription;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

import java.util.Locale;
import java.util.PriorityQueue;

public class DriftActivity extends ComponentActivity {

    private final String TAG = "DriftActivityUserDebug";
//    private static final int[] SUPPORTED_IMU_RATES = {13, 26, 52, 104, 208, 416, 833, 1666};
    private static final int[] SUPPORTED_IMU_RATES = {208};
    private static final long DURATION_PER_RATE_MS = 5 * 60 * 60 * 1000;
    private int currentRateIndex = 0;
    private int imuRate;// 13, 26, 52, 104, 208, 416, 833, 1666


    private MovesenseDevice device;
    private MdsManager mdsManager;
    private MdsSubscription mdsSubscription;
    private long recordingStartTime = 0;
    private boolean isRecording = false;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        device = MovesenseConnectedDevices.getConnectedDevice(0);
        mdsManager = new MdsManager(this, device);

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isRecording){
                    Log.d(TAG, "Already Recording");
                    return;
                }
                isRecording = true;
                Log.d(TAG, "Start Recording");
                recordingStartTime = System.currentTimeMillis();
                recordCurrentRateData();
                Toast.makeText(DriftActivity.this, "Started Recording", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "Stop Recording");
//                unSubscribeImuData();
            }
        });
    }

    private void recordCurrentRateData() {
        if (currentRateIndex >= SUPPORTED_IMU_RATES.length) {
            Log.d("TAG", "Recording Completed");
            return;
        }
        Log.d(TAG, "Recording data for rate: " + SUPPORTED_IMU_RATES[currentRateIndex]);
        imuRate = SUPPORTED_IMU_RATES[currentRateIndex];
        subscribeImuData();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                unSubscribeImuData();
                currentRateIndex++;
                recordCurrentRateData();
            }
        }, DURATION_PER_RATE_MS);

    }


    public void subscribeImuData() {
//        Log.d(TAG, "Name: " + device.getName() + " MacAddress: " + device.getMacAddress());

        CsvLogger logger = new CsvLogger(this, "imu_data_" + imuRate + ".csv");
        logger.logData("data,timestamp,time error,linearAccX,linearAccY,linearAccZ");

        mdsSubscription = mdsManager.subscribeImu(imuRate, new MdsNotificationListener() {
            @Override
            public void onNotification(String data) {
                ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);
                long timestamp = imuModel.getBody().getTimestamp() + mdsManager.getStartTime() + mdsManager.getDelay();
                double linearAccX = imuModel.getBody().getArrayAcc()[0].getX();
                double linearAccY = imuModel.getBody().getArrayAcc()[0].getY();
                double linearAccZ = imuModel.getBody().getArrayAcc()[0].getZ();
                long timeError = System.currentTimeMillis() - timestamp;
                logger.logData(TimeUtil.formatDate(timestamp) + "," + timestamp + "," + timeError + "," + linearAccX + "," + linearAccY + "," + linearAccZ);
//                Log.d(TAG, "Rate: "+ imuRate +" timestamp" + timestamp + " LinearAcc: " + String.format(Locale.getDefault(), "%.6f - %.6f - %.6f", linearAccX, linearAccY, linearAccZ));
            }

            @Override
            public void onError(MdsException e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    public void unSubscribeImuData() {
        mdsSubscription.unsubscribe();
    }

}




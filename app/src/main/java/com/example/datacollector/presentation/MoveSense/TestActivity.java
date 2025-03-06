package com.example.datacollector.presentation.MoveSense;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.MeasureCallback;
import androidx.health.services.client.MeasureClient;
import androidx.health.services.client.data.Availability;
import androidx.health.services.client.data.DataPointContainer;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.DeltaDataType;
import androidx.health.services.client.data.SampleDataPoint;

import com.example.datacollector.R;
import com.example.datacollector.presentation.MoveSense.model.ImuModel;
import com.example.datacollector.presentation.MoveSense.model.SensorData;
import com.example.datacollector.presentation.MoveSense.model.TimeDetailedModel;
import com.example.datacollector.presentation.MoveSense.model.TimeModel;
import com.example.datacollector.presentation.MoveSense.utils.FormatHelper;
import com.example.datacollector.presentation.location.LocationForeGroundService;
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

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

public class TestActivity extends ComponentActivity {

    private final String TAG = "TestActivityUserDebug";
    private int imuRate = 104;// 13, 26, 52, 104, 208, 416, 833, 1666
    private String fileName = "sensor_data_" + imuRate +  ".csv";
    private final int REQUEST_CODE_HEARTRATE = 1;
    private final int REQUEST_CODE_LOCATION = 2;
    private MovesenseDevice device;
    private CsvLogger csvLogger;
    private MdsManager mdsManager;
    private MdsSubscription mdsSubscription;
    private MeasureClient hrMeasureClient;
    private MeasureCallback hrMeasureCallback;
    private List<SensorData> sensorDataList = new ArrayList<>();
    private long recordingStartTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        device = MovesenseConnectedDevices.getConnectedDevice(0);
        mdsManager = new MdsManager(this, device);
        Log.d(TAG, "Name: " + device.getName() + " MacAddress: " + device.getMacAddress());

        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordingStartTime = System.currentTimeMillis();
                startHeartRateUpdate();
                startImuSubscription();
                startLocationNotification();
            }
        });
        findViewById(R.id.stop_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hrMeasureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM,hrMeasureCallback);
                mdsSubscription.unsubscribe();
                Collections.sort(sensorDataList);
                csvLogger = new CsvLogger(TestActivity.this, fileName);
                csvLogger.logData("date,timestamp, type");
                for(SensorData sensorData: sensorDataList){
                    String date = TimeUtil.formatDateTime(sensorData.timestamp);
                    csvLogger.logData(date + "," + sensorData.timestamp + "," + sensorData.dataType);
                }
                Log.d(TAG, "Record start time: " + TimeUtil.formatDateTime(recordingStartTime));
            }
        });
    }


    public void startImuSubscription() {
        mdsSubscription = mdsManager.subscribeImu(imuRate, new MdsNotificationListener() {
            @Override
            public void onNotification(String data) {
                Log.d(TAG,"Imu Data: " + data);
                ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);
                long timestamp = imuModel.getBody().getTimestamp() + mdsManager.getStartTime();
                sensorDataList.add(new SensorData(timestamp, "IMU", imuModel));
            }
            @Override
            public void onError(MdsException e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    public void startHeartRateUpdate(){
        if(!PermissionUtil.hasPermission(this, Manifest.permission.BODY_SENSORS)){
            PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_CODE_HEARTRATE);
            return;
        }
        hrMeasureClient = HealthServices.getClient(this).getMeasureClient();
        hrMeasureCallback = new MeasureCallback() {
            @Override
            public void onDataReceived(@NonNull DataPointContainer dataPointContainer) {
                List<SampleDataPoint<Double>> sampleDataPointList = dataPointContainer.getData(DataType.HEART_RATE_BPM);
                if (!sampleDataPointList.isEmpty() && sampleDataPointList.get(0).getValue() > 0) {
                    Log.i(TAG, "HeartRate Date: " + dataPointContainer);
                    int heartRate = sampleDataPointList.get(0).getValue().intValue();
                    Duration duration = sampleDataPointList.get(0).getTimeDurationFromBoot();
                    long timestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + duration.toMillis();
                    sensorDataList.add(new SensorData(timestamp, "HeartRate", heartRate));
                }
            }
            @Override
            public void onAvailabilityChanged(@NonNull DeltaDataType<?, ?> deltaDataType, @NonNull Availability availability) {
                Log.d(TAG, "heart rate sensor availability Changed: " + availability);
            }
        };
        hrMeasureClient.registerMeasureCallback(DataType.HEART_RATE_BPM,hrMeasureCallback);
    }

    private void startLocationNotification(){
        if (!PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) && !PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
            return;
        }

        BroadcastReceiver locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.w(TAG, "Location Update Received");
                long timestamp = intent.getLongExtra("timestamp", 0);
                sensorDataList.add(new SensorData(timestamp, "Location", intent));
            }
        };
        registerReceiver(locationReceiver,new IntentFilter("LOCATION_UPDATE"), Context.RECEIVER_EXPORTED);
        Intent intent = new Intent(this, LocationForeGroundService.class);
        startService(intent);

    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        if(requestCode == REQUEST_CODE_HEARTRATE){
            if(PermissionUtil.hasPermission(this, Manifest.permission.BODY_SENSORS)){
                startHeartRateUpdate();
            }else {
                Log.w(TAG, "Heart Rate Permission Denied");
            }
        }

        if(requestCode == REQUEST_CODE_LOCATION){
            if(PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)){
                startLocationNotification();
            }else {
                Log.w(TAG, "Location Permission Denied");
            }
        }
    }

}
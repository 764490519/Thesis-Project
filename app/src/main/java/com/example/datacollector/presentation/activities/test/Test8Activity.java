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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Test8Activity extends ComponentActivity {

    private final String TAG = "TestActivityUserDebug";
    private final int REQUEST_CODE_HEARTRATE = 1;
    private final int REQUEST_CODE_LOCATION = 2;
    private MdsSubscription mdsSubscription;
    private BroadcastReceiver heartRateReceiver;
    private BroadcastReceiver locationReceiver;
    private BroadcastReceiver accelerometerReceiver;
    private BroadcastReceiver gyroscopeReceiver;
    private PriorityQueue<SensorData> sensorDataPriorityQueue;
    private long recordingStartTime;
    private int[] dataCounts;
    private CsvLogger csvLogger;
    private CsvLogger syncLogger;
    private MdsManager mdsManager;
    private MovesenseDevice device;
    private boolean isRecording = false;
    private final boolean[] logDebug = {false, false, false, false, false};
    private final boolean syncDebug = true;
//    private final long RECORDING_DURATION = 1 * 60 * 60 * 1000;//
    private final long RECORDING_DURATION = 120 * 60 *  1000;//
    private final long SYNC_INTERVAL = 45 * 1000;
    private int samplingRate = 52;// 13, 26, 52, 104, 208, 416, 833, 1666

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        syncLogger = new CsvLogger(this, "test8_time_sync.csv");
        syncLogger.logData("SamplingRate,RecordingDuration,SyncTime,IMUCount,HeartRateCount,LocationCount,AccelerometerCount,GyroscopeCount");
        device = MovesenseConnectedDevices.getConnectedDevice(0);
        mdsManager = new MdsManager(this, device);


        findViewById(R.id.start_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording(RECORDING_DURATION);
//                startRecording();
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
            Toast.makeText(Test8Activity.this, "Already Recording", Toast.LENGTH_SHORT).show();
            return;
        }

        isRecording = true;
        recordingStartTime = System.currentTimeMillis();
        sensorDataPriorityQueue = new PriorityQueue<>();
        dataCounts = new int[5];

        Log.d(TAG, "Start Recording, IMU Sampling Rate: " + samplingRate);
        String fileName = "test8_" + samplingRate + ".csv";
        csvLogger = new CsvLogger(Test8Activity.this, fileName);
        csvLogger.logData("date,timestamp,type,data");

        subscribeImuData();
        subscribeHeartRateData();
        subscribeLocationData();
        subscribeAccelerometerData();
        subscribeGyroscopeData();
        syncDuringRecording();
        Toast.makeText(Test8Activity.this, "Started Recording", Toast.LENGTH_SHORT).show();
    }


    private void stopRecording() {
        if (!isRecording) {
            Log.d(TAG, "Not Recording");
            Toast.makeText(Test8Activity.this, "Not Recording", Toast.LENGTH_SHORT).show();
            return;
        }
        Log.d(TAG, "Stop Recording");
        isRecording = false;
        unSubscribeImuData();
        unSubscribeHeartRateData();
        unSubscribeLocationData();
        unSubscribeAccelerometerData();
        unSubscribeGyroscopeData();
        syncAfterRecording();
    }


    public void subscribeImuData() {
        Log.d(TAG, "Name: " + device.getName() + " MacAddress: " + device.getMacAddress());

        mdsSubscription = mdsManager.subscribeImu(samplingRate, new MdsNotificationListener() {
            @Override
            public void onNotification(String data) {
                ImuModel imuModel = new Gson().fromJson(data, ImuModel.class);
                long baseTimestamp = imuModel.getBody().getTimestamp() + mdsManager.getStartTime() + mdsManager.getDelay();
                long diff = 1000 / samplingRate;
                for (int i = 0; i < imuModel.getBody().getArrayAcc().length; i++) {
                    long timestamp = baseTimestamp + i * diff;
                    double linearAccX = imuModel.getBody().getArrayAcc()[i].getX();
                    double linearAccY = imuModel.getBody().getArrayAcc()[i].getY();
                    double linearAccZ = imuModel.getBody().getArrayAcc()[i].getZ();
                    sensorDataPriorityQueue.add(new SensorData(timestamp, "IMU", "LinearAcc: " + String.format(Locale.getDefault(), "%.6f - %.6f - %.6f", linearAccX, linearAccY, linearAccZ)));
                    if (logDebug[0]) {
                        Log.d(TAG, "IMU timestamp" + timestamp + " LinearAcc: " + String.format(Locale.getDefault(), "%.6f - %.6f - %.6f", linearAccX, linearAccY, linearAccZ));
                    }
                }
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

    public void subscribeHeartRateData() {
        if (!PermissionUtil.hasPermission(this, Manifest.permission.BODY_SENSORS)) {
            PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_CODE_HEARTRATE);
            return;
        }

        Intent serviceIntent = new Intent(this, HeartRateForegroundService.class);
        startService(serviceIntent);

        heartRateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long timestamp = intent.getLongExtra("timestamp", 0);
                int heartRate = intent.getIntExtra("heartRate", 0);
                String date = TimeUtil.formatDate(timestamp);
                sensorDataPriorityQueue.add(new SensorData(timestamp, "HeartRate", "HeartRate: " + heartRate));
                if (logDebug[1]) {
                    Log.d(TAG, "HeartRate timestamp: " + timestamp + " HeartRate: " + heartRate);
                }
            }
        };
        registerReceiver(heartRateReceiver, new IntentFilter("HEART_RATE_UPDATE"), Context.RECEIVER_EXPORTED);
    }

    public void unSubscribeHeartRateData() {
        unregisterReceiver(heartRateReceiver);
        Intent serviceIntent = new Intent(this, HeartRateForegroundService.class);
        stopService(serviceIntent);
    }

    private void subscribeLocationData() {
        if (!PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) && !PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
            return;
        }

        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long timestamp = intent.getLongExtra("timestamp", 0);
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longitude = intent.getDoubleExtra("longitude", 0);
                sensorDataPriorityQueue.add(new SensorData(timestamp, "Location", "Latitude: " + latitude + " Longitude: " + longitude));
                if (logDebug[2]) {
                    Log.d(TAG, "Location timestamp: " + timestamp + " Latitude: " + latitude + " Longitude: " + longitude);
                }
            }
        };
        registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"), Context.RECEIVER_EXPORTED);
        Intent intent = new Intent(this, LocationForeGroundService.class);
        startService(intent);
    }

    private void unSubscribeLocationData() {
        unregisterReceiver(locationReceiver);
        Intent intent = new Intent(this, LocationForeGroundService.class);
        stopService(intent);
    }

    private void subscribeAccelerometerData() {
        accelerometerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                AccelerometerModel accelerometerModel = intent.getParcelableExtra("accelerometer");
                if (accelerometerModel == null) {
                    return;
                }
                sensorDataPriorityQueue.add(new SensorData(accelerometerModel.timestamp, "Accelerometer", "X: " + accelerometerModel.x + " Y: " + accelerometerModel.y + " Z: " + accelerometerModel.z));
                if (logDebug[3]) {
                    Log.d(TAG, "Accelerometer timestamp: " + accelerometerModel.timestamp + " X: " + accelerometerModel.x + " Y: " + accelerometerModel.y + " Z: " + accelerometerModel.z);
                }
            }
        };
        registerReceiver(accelerometerReceiver, new IntentFilter("ACCELEROMETER_UPDATE"), Context.RECEIVER_EXPORTED);
        Intent intent = new Intent(this, AccelerometerForegroundService.class);
        startService(intent);
    }

    private void unSubscribeAccelerometerData() {
        unregisterReceiver(accelerometerReceiver);
        Intent intent = new Intent(this, AccelerometerForegroundService.class);
        stopService(intent);
    }

    private void subscribeGyroscopeData() {
        gyroscopeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                GyroscopeModel gyroscopeModel = intent.getParcelableExtra("gyroscope");
                if (gyroscopeModel == null) {
                    return;
                }
                sensorDataPriorityQueue.add(new SensorData(gyroscopeModel.timestamp, "Gyroscope", "X: " + gyroscopeModel.x + " Y: " + gyroscopeModel.y + " Z: " + gyroscopeModel.z));
                if (logDebug[4]) {
                    Log.d(TAG, "Gyroscope timestamp: " + gyroscopeModel.timestamp + " X: " + gyroscopeModel.x + " Y: " + gyroscopeModel.y + " Z: " + gyroscopeModel.z);
                }
            }
        };
        registerReceiver(gyroscopeReceiver, new IntentFilter("GYROSCOPE_UPDATE"), Context.RECEIVER_EXPORTED);

        Intent intent = new Intent(this, GyroscopeForegroundService.class);
        startService(intent);
    }

    private void unSubscribeGyroscopeData() {
        unregisterReceiver(gyroscopeReceiver);
        Intent intent = new Intent(this, GyroscopeForegroundService.class);
        stopService(intent);
    }


    private void syncAfterRecording() {
        long syncStartTime = System.currentTimeMillis();
        sync(csvLogger, sensorDataPriorityQueue);
        long syncEndTime = System.currentTimeMillis();
        if (syncDebug) {
            Log.d(TAG, "Recording Time: " + TimeUtil.formatDuration(syncStartTime - recordingStartTime));
            Log.d(TAG, "Sync Time: " + TimeUtil.formatDuration(syncEndTime - syncStartTime));
            Log.d(TAG, "IMU Count: " + dataCounts[0] + ", HeartRate Count: " + dataCounts[1] + ", Location Count: " + dataCounts[2] + ", Accelerometer Count: " + dataCounts[3] + ", Gyroscope Count: " + dataCounts[4]);
        }

        syncLogger.logData(samplingRate + "," + RECORDING_DURATION + "," + (syncEndTime - syncStartTime) + "," + dataCounts[0] + "," + dataCounts[1] + "," + dataCounts[2] + "," + dataCounts[3] + "," + dataCounts[4]);
    }

    private void syncDuringRecording() {
        Handler syncHandler = new Handler(Looper.getMainLooper());
        AtomicInteger index = new AtomicInteger(0);
        AtomicLong lastSyncTime = new AtomicLong(System.currentTimeMillis());
        syncHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isRecording){
                    return;
                }
//                csvLogger.logData("Sync: " + index.get());
                PriorityQueue queue = sensorDataPriorityQueue;
                sensorDataPriorityQueue = new PriorityQueue<>();
                int[] counts = dataCounts.clone();
                sync(csvLogger, queue);
                if (syncDebug) {
                    Log.d(TAG, "Sync Index:" + index.get() + " Duration: " + TimeUtil.formatDuration(System.currentTimeMillis() - lastSyncTime.get()));
                    Log.d(TAG, "IMU Count: " + (dataCounts[0] - counts[0]) + ", HeartRate Count: " + (dataCounts[1] - counts[1]) + ", Location Count: " + (dataCounts[2] - counts[2]) + ", Accelerometer Count: " + (dataCounts[3] - counts[3]) + ", Gyroscope Count: " + (dataCounts[4] - counts[4]));
                    index.incrementAndGet();
                }
                lastSyncTime.set(System.currentTimeMillis());
                syncHandler.postDelayed(this, SYNC_INTERVAL);
            }
        }, SYNC_INTERVAL);
    }


    private void sync(CsvLogger logger, PriorityQueue<SensorData> queue) {
        while (!queue.isEmpty()) {
            SensorData sensorData = queue.poll();
            String data = TimeUtil.formatDate(sensorData.timestamp);
            logger.logData(data + "," + sensorData.timestamp + "," + sensorData.dataType + "," + sensorData.data);
            switch (sensorData.dataType) {
                case "IMU":
                    dataCounts[0]++;
                    break;
                case "HeartRate":
                    dataCounts[1]++;
                    break;
                case "Location":
                    dataCounts[2]++;
                    break;
                case "Accelerometer":
                    dataCounts[3]++;
                    break;
                case "Gyroscope":
                    dataCounts[4]++;
                    break;

            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, int deviceId) {
        if (requestCode == REQUEST_CODE_HEARTRATE) {
            if (PermissionUtil.hasPermission(this, Manifest.permission.BODY_SENSORS)) {
                subscribeHeartRateData();
            } else {
                Log.w(TAG, "Heart Rate Permission Denied");
            }
        }

        if (requestCode == REQUEST_CODE_LOCATION) {
            if (PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                subscribeLocationData();
            } else {
                Log.w(TAG, "Location Permission Denied");
            }
        }
    }
}
package com.example.datacollector.presentation.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.health.services.client.ExerciseClient;
import androidx.health.services.client.ExerciseUpdateCallback;
import androidx.health.services.client.HealthServices;
import androidx.health.services.client.data.Availability;
import androidx.health.services.client.data.DataType;
import androidx.health.services.client.data.ExerciseConfig;
import androidx.health.services.client.data.ExerciseLapSummary;
import androidx.health.services.client.data.ExerciseType;
import androidx.health.services.client.data.ExerciseUpdate;
import androidx.health.services.client.data.SampleDataPoint;

import java.util.Collections;
import java.util.List;

public class HeartRateForegroundService extends Service {

    private static final String CHANNEL_ID = "HeartRateServiceChannel";
    private static final String TAG = "HeartRateServiceUserDebug";
    private ExerciseClient exerciseClient;
    private ExerciseUpdateCallback exerciseUpdateCallback;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, getNotification());
        startHeartRateMonitoring();
    }

    private void startHeartRateMonitoring(){
        exerciseClient = HealthServices.getClient(this).getExerciseClient();
        exerciseUpdateCallback = new ExerciseUpdateCallback() {
            @Override
            public void onLapSummaryReceived(@NonNull ExerciseLapSummary exerciseLapSummary) {
            }

            @Override
            public void onRegistrationFailed(@NonNull Throwable throwable) {
            }

            @Override
            public void onRegistered() {
            }

            @Override
            public void onAvailabilityChanged(@NonNull DataType<?, ?> dataType, @NonNull Availability availability) {
                Log.d(TAG, "onAvailabilityChanged: " + availability);
            }

            @Override
            public void onExerciseUpdateReceived(@NonNull ExerciseUpdate exerciseUpdate) {
                if (!exerciseUpdate.getLatestMetrics().getData(DataType.HEART_RATE_BPM).isEmpty()) {

                    List<SampleDataPoint<Double>> hrDataPoints = exerciseUpdate.getLatestMetrics().getData(DataType.HEART_RATE_BPM);
                    for(SampleDataPoint<Double> hrDataPoint: hrDataPoints){
                        int heartRate = hrDataPoint.getValue().intValue();
                        long timestamp = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime() + hrDataPoint.getTimeDurationFromBoot().toMillis();
                        if (heartRate != 0){
                            Intent intent = new Intent("HEART_RATE_UPDATE");
                            intent.putExtra("timestamp", timestamp);
                            intent.putExtra("heartRate", heartRate);
                            sendBroadcast(intent);
                        }
                    }

                }
            }
        };

        ExerciseConfig exerciseConfig = new ExerciseConfig.Builder(ExerciseType.RUNNING)
                .setDataTypes(Collections.singleton(DataType.HEART_RATE_BPM)).build();
        exerciseClient.setUpdateCallback(exerciseUpdateCallback);
        exerciseClient.startExerciseAsync(exerciseConfig);
        Log.d(TAG, "startHeartRateMonitoring: ");

    }

//    private void startHeartRateMonitoring0() {
//
//        hrMeasureCallback = new MeasureCallback() {
//            @Override
//            public void onAvailabilityChanged(@NonNull DeltaDataType<?, ?> deltaDataType, @NonNull Availability availability) {
//                Log.d(TAG, "onAvailabilityChanged: " + availability);
//            }
//            @Override
//            public void onDataReceived(@NonNull DataPointContainer dataPointContainer) {
//                List<SampleDataPoint<Double>> sampleDataPointList = dataPointContainer.getData(DataType.HEART_RATE_BPM);
//                if (!sampleDataPointList.isEmpty() && sampleDataPointList.get(0).getValue() > 0) {
//                    int heartRate = sampleDataPointList.get(0).getValue().intValue();
//                    Duration duration = sampleDataPointList.get(0).getTimeDurationFromBoot();
//                    long timestamp = System.currentTimeMillis() - android.os.SystemClock.elapsedRealtime() + duration.toMillis();
//
//                    Intent intent = new Intent("HEART_RATE_UPDATE");
//                    intent.putExtra("timestamp", timestamp);
//                    intent.putExtra("heartRate", heartRate);
//                    sendBroadcast(intent);
//                }
//            }
//        };
//        hrMeasureClient.registerMeasureCallback(DataType.HEART_RATE_BPM, hrMeasureCallback);
//        acquireWakeLock();
//    }

    private void stopHeartRateMonitoring() {
        exerciseClient.endExerciseAsync();
//        if (hrMeasureClient != null && hrMeasureCallback != null) {
//            hrMeasureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM, hrMeasureCallback);
//        }
//        releaseWakeLock();
    }


    private Notification getNotification(){
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Heart Rate Monitoring")
                .setContentText("Monitoring heart rate in background")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)
                .setOngoing(true)
                .build();
    }


    private void createNotificationChannel(){
        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Heart Rate Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopHeartRateMonitoring();
    }

//    private void acquireWakeLock() {
//        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
//        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "HeartRateService::WakeLock");
//        wakeLock.acquire(10 * 60 * 1000L /*10 minutes*/);
//    }
//
//    private void releaseWakeLock() {
//        if (wakeLock != null && wakeLock.isHeld()) {
//            wakeLock.release();
//        }
//    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

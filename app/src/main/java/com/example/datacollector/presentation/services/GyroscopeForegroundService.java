package com.example.datacollector.presentation.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.datacollector.presentation.model.AccelerometerModel;
import com.example.datacollector.presentation.model.GyroscopeModel;
import com.example.datacollector.presentation.util.TimeUtil;

public class GyroscopeForegroundService extends Service implements SensorEventListener {

    private String TAG = "GyroForegroundServiceUserDebug";
    private static final String CHANNEL_ID = "GyroForegroundServiceChannel";
    private final int delay = SensorManager.SENSOR_DELAY_NORMAL;
//    private final int delay = SensorManager.SENSOR_DELAY_GAME;
    private SensorManager sensorManager;
    private Sensor gyroscope;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1,getNotification());
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null){
            gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if(gyroscope != null){
            sensorManager.registerListener(this, gyroscope, delay);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null){
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            long timestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() +  event.timestamp/1000000;
            Intent intent = new Intent("GYROSCOPE_UPDATE");
            intent.putExtra("gyroscope", new GyroscopeModel(x, y, z,timestamp));
            sendBroadcast(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "onAccuracyChanged: " + accuracy);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification getNotification() {
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Gyroscope Service")
                .setContentText("Collecting Gyroscope Data");
        return builder.build();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}

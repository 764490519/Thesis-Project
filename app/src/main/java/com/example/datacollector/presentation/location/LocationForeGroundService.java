package com.example.datacollector.presentation.location;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.datacollector.R;
import com.example.datacollector.presentation.util.PermissionUtil;
import com.example.datacollector.presentation.util.TimeUtil;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import android.Manifest;
import android.os.Looper;
import android.util.Log;


public class LocationForeGroundService extends Service {
    private static final String TAG = "LocationForeGroundServiceUserDebug";
    private static final String CHANNEL_ID = "location_service_channel";
    private static final int UPDATE_INTERVAL = 5000;
    private static final int FASTEST_INTERVAL = 1000;
    private FusedLocationProviderClient mFusedLocationClient;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"Location Foreground Service started");
        createNotificationChannel();
        getLocation();
        startForeground(1,getNotification());
        return START_STICKY;
    }


    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Service Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @SuppressLint("MissingPermission")
    private void getLocation(){
        LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
        mLocationRequestHighAccuracy.setPriority(Priority.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(UPDATE_INTERVAL);
        mLocationRequestHighAccuracy.setFastestInterval(FASTEST_INTERVAL);

        if(!PermissionUtil.hasPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.w(TAG,"ACCESS_FINE_LOCATION permission not granted");
            return;
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequestHighAccuracy,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(@NonNull LocationResult locationResult) {
                        Location location = locationResult.getLastLocation();
                        if(location != null){
                            //Log.d(TAG,"Time: " + TimeUtil.formatTime(location.getTime()) + ", Location: " + location.getLatitude() + "," + location.getLongitude());
                            //Send broadcast to activity
                            Intent intent = new Intent("LOCATION_UPDATE");
                            intent.putExtra("timestamp", location.getTime());
                            intent.putExtra("date", TimeUtil.formatDateTime(location.getTime()));
                            intent.putExtra("latitude", location.getLatitude());
                            intent.putExtra("longitude", location.getLongitude());
                            intent.putExtra("altitude", location.getAltitude());
                            intent.putExtra("accuracy", location.getAccuracy());
                            sendBroadcast(intent);
                        }
                    }
                },
                Looper.getMainLooper());

    }

    private Notification getNotification(){
        Intent notificationIntent = new Intent(this,LocationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,notificationIntent,PendingIntent.FLAG_IMMUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Location Service")
                .setContentText("Getting location updates")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setOngoing(true);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE);
        }
        return builder.build();
    }
}

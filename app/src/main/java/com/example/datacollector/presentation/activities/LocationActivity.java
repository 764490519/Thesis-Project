package com.example.datacollector.presentation.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.datacollector.presentation.services.LocationForeGroundService;
import com.example.datacollector.presentation.util.CsvLogger;
import com.example.datacollector.presentation.util.PermissionUtil;

import com.example.datacollector.R;
import android.Manifest;

public class LocationActivity extends ComponentActivity {

    private static final String TAG = "LocationActivityUserDebug";
    private static final int REQUEST_CODE_LOCATION = 1;
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private TextView mAltitudeTextView;
    private CsvLogger csvLogger;
    private BroadcastReceiver locationReceiver;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);
        //Initialize views
        mLatitudeTextView = findViewById(R.id.latitude_textview);
        mLongitudeTextView = findViewById(R.id.longitude_textview);
        mAltitudeTextView = findViewById(R.id.altitude_textview);

        csvLogger = new CsvLogger(this, "location_data.csv");
        csvLogger.logData("Timestamp,Date,Latitude,Longitude,Altitude");

        if (PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtil.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            startLocationForeGroundService();
        } else {
            PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_LOCATION);
        }
    }


    private void startLocationForeGroundService(){
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String date = intent.getStringExtra("date");
                long timestamp = intent.getLongExtra("timestamp", 0);
                double latitude = intent.getDoubleExtra("latitude", 0.0);
                double longitude = intent.getDoubleExtra("longitude", 0.0);
                double altitude = intent.getDoubleExtra("altitude", 0.0);
                String latitudeDMS = convertToDMS(latitude, true);
                String longitudeDMS = convertToDMS(longitude, false);
                String altitudeFormat = String.format("%.1f", altitude);
                Log.d(TAG, "Location updated, date:  " + date + ", latitude: " + latitudeDMS + ", longitude: " + longitudeDMS);
                csvLogger.logData(timestamp + "," + date + "," + latitudeDMS + "," + longitudeDMS + "," + altitudeFormat);
                mLatitudeTextView.setText("Latitude: " + latitudeDMS);
                mLongitudeTextView.setText("Longitude: " + longitudeDMS);
                mAltitudeTextView.setText("Altitude: " + altitudeFormat + " m");
            }
        };

        registerReceiver(locationReceiver, new IntentFilter("LOCATION_UPDATE"), Context.RECEIVER_EXPORTED);

        Intent intent = new Intent(this, LocationForeGroundService.class);
        startService(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQUEST_CODE_LOCATION){
            if(PermissionUtil.hasPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) || PermissionUtil.hasPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)){
                startLocationForeGroundService();
            }
            else {
                Log.w(TAG,"Location permission not granted");
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(locationReceiver);
    }

    public static String convertToDMS(double degree, boolean isLatitude) {
        int degrees = (int) degree;
        double decimalMinutes = (Math.abs(degree) - Math.abs(degrees)) * 60;
        int minutes = (int) decimalMinutes;
        double seconds = (decimalMinutes - minutes) * 60;

        String direction;
        if (isLatitude) {
            direction = (degree >= 0) ? "N" : "S";
        } else {
            direction = (degree >= 0) ? "E" : "W";
        }

        return String.format("%d° %d' %.2f\" %s", Math.abs(degrees), minutes, seconds, direction);
    }


}
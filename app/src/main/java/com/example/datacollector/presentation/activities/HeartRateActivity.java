package com.example.datacollector.presentation.activities;

import android.Manifest;
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
import androidx.health.services.client.MeasureCallback;
import androidx.health.services.client.MeasureClient;
import androidx.health.services.client.data.DataType;

import com.example.datacollector.R;
import com.example.datacollector.presentation.services.HeartRateForegroundService;
import com.example.datacollector.presentation.util.CsvLogger;
import com.example.datacollector.presentation.util.PermissionUtil;
import com.example.datacollector.presentation.util.TimeUtil;


public class HeartRateActivity extends ComponentActivity {
    private final String TAG = "HeartRateActivityUserDebug";
    private final int REQUEST_CODE_BODY_SENSORS = 1001;
    private boolean supportHeartRate = true;
    private TextView mHeartRateTextView;
    private MeasureClient measureClient;
    private MeasureCallback measureCallback;
    private BroadcastReceiver heartRateReceiver;
    private CsvLogger csvLogger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activirt_heart_rate);
        mHeartRateTextView = findViewById(R.id.heart_rate_textview);

        csvLogger = new CsvLogger(this, "heart_rate_data.csv");
        csvLogger.logData("Timestamp,Date,HeartRate");

        //Check for permissions
        if(PermissionUtil.hasPermission(this, Manifest.permission.BODY_SENSORS)){
            startHeartRateMonitoring();
        }
        else {
            PermissionUtil.requestPermissions(this, new String[]{Manifest.permission.BODY_SENSORS}, REQUEST_CODE_BODY_SENSORS);
        }

    }


    private void startHeartRateMonitoring(){
        Intent serviceIntent = new Intent(this, HeartRateForegroundService.class);
        startService(serviceIntent);

        heartRateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long timestamp = intent.getLongExtra("timestamp",0);
                int heartRate = intent.getIntExtra("heartRate",0);
                String date = TimeUtil.formatDate(timestamp);
                Log.d(TAG, "Heart Rate Updated, Date: " + date + " Heart Rate: " + heartRate);
                csvLogger.logData(timestamp+ ","+ date + "," + heartRate);
                mHeartRateTextView.setText("Heart Rate: " + heartRate);
            }
        };
        registerReceiver(heartRateReceiver, new IntentFilter("HEART_RATE_UPDATE"),Context.RECEIVER_EXPORTED);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_BODY_SENSORS){
            if(PermissionUtil.hasPermission(this,Manifest.permission.BODY_SENSORS)){
                startHeartRateMonitoring();
            }
            else {
                Log.w(TAG,"Heart Rate permission not granted");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        measureClient.unregisterMeasureCallbackAsync(DataType.HEART_RATE_BPM,measureCallback);
    }
}

package com.example.datacollector.presentation.heartrate;

import android.Manifest;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
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
import com.example.datacollector.presentation.util.CsvLogger;
import com.example.datacollector.presentation.util.PermissionUtil;
import com.example.datacollector.presentation.util.TimeUtil;

import java.time.Duration;
import java.util.List;


public class HeartRateActivity extends ComponentActivity {
    private final String TAG = "HeartRateActivityUserDebug";
    private final int REQUEST_CODE_BODY_SENSORS = 1001;
    private boolean supportHeartRate = true;
    private TextView mHeartRateTextView;
    private MeasureClient measureClient;
    private MeasureCallback measureCallback;
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
        measureClient = HealthServices.getClient(this).getMeasureClient();
        measureCallback = new MeasureCallback() {
            @Override
            public void onAvailabilityChanged(@NonNull DeltaDataType<?, ?> deltaDataType, @NonNull Availability availability) {
                Log.d(TAG, "heart rate sensor availability Changed: " + availability);
            }
            @Override
            public void onDataReceived(@NonNull DataPointContainer dataPointContainer) {
                List<SampleDataPoint<Double>> sampleDataPointList = dataPointContainer.getData(DataType.HEART_RATE_BPM);
                if (!sampleDataPointList.isEmpty() && sampleDataPointList.get(0).getValue() > 0) {
                    int heartRate = sampleDataPointList.get(0).getValue().intValue();
                    Duration duration = sampleDataPointList.get(0).getTimeDurationFromBoot();
                    long timestamp = System.currentTimeMillis() - SystemClock.elapsedRealtime() + duration.toMillis();
                    String date = TimeUtil.formatDateTime(timestamp);
                    Log.d(TAG, "Heart Rate Updated, Date: " + date + " Heart Rate: " + heartRate);
                    csvLogger.logData(timestamp+ ","+ date + "," + heartRate);
                    mHeartRateTextView.setText("Heart Rate: " + heartRate);
                }
            }
        };
        measureClient.registerMeasureCallback(DataType.HEART_RATE_BPM,measureCallback);
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

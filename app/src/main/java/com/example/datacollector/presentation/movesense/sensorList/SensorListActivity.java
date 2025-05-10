package com.example.datacollector.presentation.movesense.sensorList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.datacollector.R;
import com.example.datacollector.presentation.activities.ImuActivity;

public class SensorListActivity extends ComponentActivity {

    private final String TAG = "SensorListActivityUserDebug";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"SensorListActivity onCreate");

        setContentView(R.layout.activity_sensor_list);
        Button imuButton = findViewById(R.id.imu_button);

        imuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SensorListActivity.this, ImuActivity.class));
            }
        });

    }
}

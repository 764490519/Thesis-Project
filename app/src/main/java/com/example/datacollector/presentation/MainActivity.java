package com.example.datacollector.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.datacollector.R;
import com.example.datacollector.presentation.MoveSense.MovesenseActivity;
import com.example.datacollector.presentation.MoveSense.bluetooth.BluetoothStatusMonitor;
import com.example.datacollector.presentation.MoveSense.bluetooth.MdsRx;
import com.example.datacollector.presentation.MoveSense.bluetooth.RxBle;
import com.example.datacollector.presentation.location.LocationActivity;
import com.example.datacollector.presentation.heartrate.HeartRateActivity;


public class MainActivity extends ComponentActivity implements View.OnClickListener {

    private final String TAG = this.getClass().getName() + "UserDebug";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(android.R.style.Theme_DeviceDefault);
        setContentView(R.layout.activity_main);
        Button movesenseButton = findViewById(R.id.movesense_start_button);
        Button heartRateButton = findViewById(R.id.heart_rate_start_button);
        Button locationButton = findViewById(R.id.location_start_button);
        movesenseButton.setOnClickListener(this);
        heartRateButton.setOnClickListener(this);
        locationButton.setOnClickListener(this);

        initialServices();

        startActivity(new Intent(this, MovesenseActivity.class));

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.movesense_start_button) {
            startActivity(new Intent(this, MovesenseActivity.class));
        } else if (id == R.id.heart_rate_start_button) {
            startActivity(new Intent(this, HeartRateActivity.class));
        } else if (id == R.id.location_start_button) {
            startActivity(new Intent(this, LocationActivity.class));
        }
    }


    public void initialServices(){
        // Initialize RxBleWrapper
        RxBle.Instance.initialize(this);
        // Initialize MDS
        MdsRx.Instance.initialize(this);
        BluetoothStatusMonitor.INSTANCE.initBluetoothStatus();
    }

}

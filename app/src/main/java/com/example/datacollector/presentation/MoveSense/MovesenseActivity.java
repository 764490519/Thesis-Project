package com.example.datacollector.presentation.MoveSense;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datacollector.R;
import com.example.datacollector.presentation.MoveSense.bluetooth.MdsRx;
import com.example.datacollector.presentation.MoveSense.model.MdsConnectedDevice;
import com.example.datacollector.presentation.MoveSense.model.MdsDeviceInfoNewSw;
import com.example.datacollector.presentation.MoveSense.model.MdsDeviceInfoOldSw;
import com.example.datacollector.presentation.util.MdsManager;
import com.example.datacollector.presentation.MoveSense.utils.ThrowableToastingAction;
import com.example.datacollector.presentation.util.PermissionUtil;
import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MovesenseActivity extends ComponentActivity {

    private final String TAG = this.getClass().getName() + "UserDebug";

    private final int REQUEST_CODE_BLE = 1001;

    private List<ScanResult> scannedDevices = new ArrayList<>();
    private Set<String> scannedDeviceAddresses = new HashSet<>();
    private TextView movesenseTextView;
    private RecyclerView recyclerView;
    private MovesenseAdapter deviceAdapter;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    private CompositeDisposable connectedDevicesSubscriptions = new CompositeDisposable();


    private String[] blePermissions = new String[]{
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movesense);
        movesenseTextView = findViewById(R.id.movesense_textView);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new MovesenseAdapter(scannedDevices,itemClickListener);
        recyclerView.setAdapter(deviceAdapter);
        // Check permissions
        for (String permission : blePermissions) {
            if(!PermissionUtil.hasPermission(this,permission)){
                PermissionUtil.requestPermissions(this, blePermissions, REQUEST_CODE_BLE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        initialBleService();
    }

    private void initialBleService(){
        for (String permission : blePermissions) {
            if(!PermissionUtil.hasPermission(this,permission)){
                Log.w(TAG, "Permission not granted: " + permission);
                return;
            }
        }

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            Log.w(TAG, "Device Don't Support Bluetooth.");
            movesenseTextView.setText("Device Don't Support Bluetooth.");
            return;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (!bluetoothAdapter.isEnabled()) {
            Log.w(TAG, "Attempting to Enable Bluetooth...\"");
            enableBluetoothLauncher.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
        }else {
            startScan();
        }
    }


    @SuppressLint("MissingPermission")
    private void startScan() {
        Log.i(TAG, "Starting Scan");
        scannedDevices.clear();
        scannedDeviceAddresses.clear();
        deviceAdapter.updateDevices(scannedDevices);
        bluetoothLeScanner.startScan(mScanCallback);
    }

    @SuppressLint("MissingPermission")
    private void stopScan(){
        Log.i(TAG, "Stopping Scan");
        bluetoothLeScanner.stopScan((ScanCallback) null);
    }


    private ActivityResultLauncher<Intent> enableBluetoothLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.i("BLEScanActivityUserDebug", "Bluetooth started successfully");
                        startScan();
                    } else {
                        Log.w("BLEScanActivityUserDebug", "Failed to start Bluetooth");
                        movesenseTextView.setText("Failed to start Bluetooth");
                    }
                }
            }
    );

    private ScanCallback mScanCallback = new ScanCallback() {
        @SuppressLint({"MissingPermission", "SuspiciousIndentation"})
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            if(!scannedDeviceAddresses.contains(result.getDevice().getAddress()) && result.getDevice().getName() != null && result.getDevice().getName().contains("Movesense")){
                Log.i(TAG,"New Device Scanned: " + result.getDevice().getName());
                scannedDevices.add(result);
                scannedDeviceAddresses.add(result.getDevice().getAddress());
                deviceAdapter.updateDevices(scannedDevices);

                //for testing
                if(result.getDevice().getName().equals("Movesense 240530000119")){
                    connect2Device(result.getDevice());
                }
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e(TAG, "Scan failed with error code: " + errorCode);
        }
    };

    private final View.OnClickListener itemClickListener = new View.OnClickListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onClick(View view) {
            int position = recyclerView.getChildAdapterPosition(view);
            BluetoothDevice device = scannedDevices.get(position).getDevice();
            connect2Device(device);
        }
    };


    @SuppressLint("MissingPermission")
    private void connect2Device(BluetoothDevice device) {
        Log.i(TAG,"Connecting to " + device.getName());
        Toast.makeText(MovesenseActivity.this,"Connecting to " + device.getName(),Toast.LENGTH_SHORT).show();
        Mds.builder().build(this).connect(device.getAddress(),null);

        connectedDevicesSubscriptions.add(MdsRx.Instance.connectedDeviceObservable()
                .subscribe(new Consumer<MdsConnectedDevice>() {
                    @Override
                    public void accept(MdsConnectedDevice mdsConnectedDevice) {
                        // Stop refreshing
                        if (mdsConnectedDevice.getConnection() != null) {
                            Log.i(TAG, "Connected " + mdsConnectedDevice.toString());

                            // Add connected device
                            if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoNewSw) {
                                MdsDeviceInfoNewSw mdsDeviceInfoNewSw = (MdsDeviceInfoNewSw) mdsConnectedDevice.getDeviceInfo();
                                Log.d(TAG, "instanceof MdsDeviceInfoNewSw: " + mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress()
                                        + " : " + mdsDeviceInfoNewSw.getDescription() + " : " + mdsDeviceInfoNewSw.getSerial()
                                        + " : " + mdsDeviceInfoNewSw.getSw());
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoNewSw.getAddressInfoNew().get(0).getAddress(),
                                        mdsDeviceInfoNewSw.getDescription(),
                                        mdsDeviceInfoNewSw.getSerial(),
                                        mdsDeviceInfoNewSw.getSw()));
                            } else if (mdsConnectedDevice.getDeviceInfo() instanceof MdsDeviceInfoOldSw) {
                                MdsDeviceInfoOldSw mdsDeviceInfoOldSw = (MdsDeviceInfoOldSw) mdsConnectedDevice.getDeviceInfo();
                                Log.d(TAG, "instanceof MdsDeviceInfoOldSw: " + mdsDeviceInfoOldSw.getAddressInfoOld()
                                        + " : " + mdsDeviceInfoOldSw.getDescription() + " : " + mdsDeviceInfoOldSw.getSerial()
                                        + " : " + mdsDeviceInfoOldSw.getSw());
                                MovesenseConnectedDevices.addConnectedDevice(new MovesenseDevice(
                                        mdsDeviceInfoOldSw.getAddressInfoOld(),
                                        mdsDeviceInfoOldSw.getDescription(),
                                        mdsDeviceInfoOldSw.getSerial(),
                                        mdsDeviceInfoOldSw.getSw()));
                            }

                            Log.i(TAG, "List size(): " + MovesenseConnectedDevices.getConnectedDevices().size());
                            connectedDevicesSubscriptions.dispose();

                            stopScan();

//                            startActivity(new Intent(MovesenseActivity.this, ImuActivity.class));
//                            startActivity(new Intent(MovesenseActivity.this, ImuActivity2.class));
                            startActivity(new Intent(MovesenseActivity.this, TestActivity.class));

                        } else {
                            Log.e(TAG, "DISCONNECT");
                        }
                    }
                }, new ThrowableToastingAction(this)));


    }


}


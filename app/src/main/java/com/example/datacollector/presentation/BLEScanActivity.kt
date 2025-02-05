package com.example.datacollector.presentation

import android.Manifest
import android.app.ComponentCaller
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class BLEScanActivity : ComponentActivity() {

    private val REQUEST_CODE_BLE = 1001
    private val REQUEST_ENABLE_BT = 1

    private val blePermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private var isScanning = false
    private var scannedDevices = mutableListOf<ScanResult>()
    private var scannedDeviceAddresses = HashSet<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("BLEScanActivityUserDebug", "BLEScanActivity onCreate")

        //check BlueTooth support
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager == null) {
            Log.i("BLEScanActivityUserDebug", "device does not support bluetooth.")
            finish()
        }
        bluetoothAdapter = bluetoothManager.adapter
        bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

        //request BLE related permissions
        if (blePermissions.any {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }) {
            Log.i("BLEScanActivityUserDebug", "requesting BLE related Permisson: " + this)
            ActivityCompat.requestPermissions(this, blePermissions, REQUEST_CODE_BLE)
        }


        //enable BLE
        if (!bluetoothAdapter.isEnabled) {
            Log.i("BLEScanActivityUserDebug", "Bluetooth is not enabled, attempting to enable...")
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            enableBluetoothLauncher.launch(enableBtIntent)
        }

        Log.i("BLEScanActivityUserDebug", "test...")


        //layout
        val rootLayout = LinearLayout(this)
        rootLayout.orientation = LinearLayout.VERTICAL
        rootLayout.gravity = Gravity.CENTER

        val myButton = Button(this)
        myButton.text = "start scan"
        myButton.setTextColor(Color.BLUE)


        rootLayout.addView(myButton)
        setContentView(rootLayout)

        myButton.setOnClickListener {
            if (isScanning){
                //stop scan BLE devices
                Log.i("BLEScanActivityUserDebug", "stop scan bluetooth devices...")
                bluetoothLeScanner.stopScan(scanCallback)
                isScanning = false
                myButton.text = "start scan"
                myButton.setTextColor(Color.BLUE)

                Log.d("BLEScanActivityUserDebug", "Stopped scanning. Found ${scannedDevices.size} devices.")

                for (device in scannedDevices) {
                    Log.d("BLEScanActivityUserDebug", "Device: ${device.device.name ?: "Unknown"} - ${device.device.address}")
                }

            }else{
                Log.i("BLEScanActivityUserDebug", "start scan bluetooth devices...")
                scannedDevices = mutableListOf<ScanResult>()
                scannedDeviceAddresses = HashSet<String>()
                bluetoothLeScanner.startScan(scanCallback)
                isScanning = true
                myButton.text = "stop scan"
                myButton.setTextColor(Color.RED)
            }

        }

        Log.i("BLEScanActivityUserDebug", "test3...")




    }


    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i("BLEScanActivityUserDebug", "Bluetooth started successfully")

        } else {
            Log.i("BLEScanActivityUserDebug", "Failed to start Bluetooth")

        }
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        caller: ComponentCaller
    ) {
        super.onActivityResult(requestCode, resultCode, data, caller)
    }

    val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            Log.i("BLEScanActivityUserDebug", "onScanResult")
            if (result != null && result.device != null && scannedDeviceAddresses.add(result.device.address)) {
                scannedDevices.add(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            super.onBatchScanResults(results)
            Log.i("BLEScanActivityUserDebug", "onBatchScanResults")
            if (results == null)
                return
            for (result in results){
                if (result.device == null || !scannedDeviceAddresses.add(result.device.address))
                    continue
                scannedDevices.add(result)
            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
        }

    }



    override fun onPause() {
        super.onPause()
        Log.i("BLEScanActivityUserDebug", "BLEScanActivity onPause")
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.i("BLEScanActivityUserDebug", "BLEScanActivity onDestroy")

    }


}
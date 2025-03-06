package com.example.datacollector.presentation.Bluetooth

import android.Manifest
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
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.datacollector.R
import com.example.datacollector.presentation.Bluetooth.callback.BLEScanCallback

class BLEScanActivity : ComponentActivity() {

    private val REQUEST_CODE_BLE = 1001

    private val blePermissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothLeScanner: BluetoothLeScanner
    private lateinit var recyclerView: RecyclerView
    private lateinit var deviceAdapter: DeviceAdapter
    private var isScanning = false
    private var scannedDevices = mutableListOf<ScanResult>()
    private var scannedDeviceAddresses = HashSet<String>()

    private lateinit var scanCallback: ScanCallback
    private var deviceManager = DeviceManager(this)

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


        setContentView(R.layout.activity_ble_scan)
        val myButton = findViewById<Button>(R.id.bleToggleButton)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(this, scannedDevices) { device ->
            deviceManager.connect(device)
        }
        recyclerView.adapter = deviceAdapter


        var currentIndex = 0
        val testButton = findViewById<Button>(R.id.testButton)
        testButton.setOnClickListener {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()
            val characteristicsQueue = listOf(
                DeviceServices.GENERIC_ACCESS to DeviceCharacteristics.DEVICE_NAME,
                DeviceServices.GENERIC_ACCESS to DeviceCharacteristics.APPEARANCE,
                DeviceServices.GENERIC_ACCESS to DeviceCharacteristics.CONNECTION_PARAMETERS,
                DeviceServices.GENERIC_ACCESS to DeviceCharacteristics.CENTRAL_ADDRESS_RESOLUTION,

                DeviceServices.GENERIC_ATTRIBUTE to DeviceCharacteristics.SERVICE_CHANGED,

                DeviceServices.DEVICE_INFORMATION to DeviceCharacteristics.MANUFACTURER_NAME,
                DeviceServices.DEVICE_INFORMATION to DeviceCharacteristics.SERIAL_NUMBER,

                DeviceServices.BATTERY_SERVICE to DeviceCharacteristics.BATTERY_LEVEL,

                DeviceServices.MOVESENSE_SERVICE to DeviceCharacteristics.MOVESENSE_DATA_1,
                DeviceServices.MOVESENSE_SERVICE to DeviceCharacteristics.MOVESENSE_DATA_2
            )

            val (serviceUUID, characteristicUUID) = characteristicsQueue[currentIndex]
            deviceManager. readCharacteristic(serviceUUID, characteristicUUID)


            currentIndex = (currentIndex + 1) % characteristicsQueue.size

        }



        myButton.setOnClickListener {
            if (isScanning) {
                //Stop Scanning
                Log.i("BLEScanActivityUserDebug", "stop scan bluetooth devices...")
                bluetoothLeScanner.stopScan(scanCallback)
                isScanning = false
                myButton.text = "start scan"
                myButton.setTextColor(Color.BLUE)

                Log.d(
                    "BLEScanActivityUserDebug",
                    "Stopped scanning. Found ${scannedDevices.size} devices."
                )

                for (device in scannedDevices) {
                    Log.d(
                        "BLEScanActivityUserDebug",
                        "Device: ${device.device.name ?: "Unknown"} - ${device.device.address}"
                    )
                }


            }
            else {
                //Start Scanning
                Log.i("BLEScanActivityUserDebug", "start scan bluetooth devices...")
                scannedDevices = mutableListOf<ScanResult>()
                scannedDeviceAddresses = HashSet<String>()
                scanCallback = BLEScanCallback(scannedDevices, scannedDeviceAddresses, deviceAdapter,this)
                bluetoothLeScanner.startScan(scanCallback)
                isScanning = true
                myButton.text = "stop scan"
                myButton.setTextColor(Color.RED)
            }

        }

    }



    //Launcher for enabling bluetooth
    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.i("BLEScanActivityUserDebug", "Bluetooth started successfully")

        } else {
            Log.i("BLEScanActivityUserDebug", "Failed to start Bluetooth")

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
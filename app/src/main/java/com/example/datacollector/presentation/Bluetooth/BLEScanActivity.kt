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
import com.example.datacollector.presentation.MoveSense.MoveSenseCharacteristics
import com.example.datacollector.presentation.MoveSense.MoveSenseManager
import com.example.datacollector.presentation.MoveSense.MoveSenseServices
import com.example.datacollector.presentation.callback.BLEScanCallback

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
    private var moveSenseManager = MoveSenseManager(this)

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


        setContentView(R.layout.ble_scan_activity)
        val myButton = findViewById<Button>(R.id.bleToggleButton)
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(this, scannedDevices) { device ->
            moveSenseManager.connect(device)
        }
        recyclerView.adapter = deviceAdapter


        var currentIndex = 0
        val testButton = findViewById<Button>(R.id.testButton)
        testButton.setOnClickListener {
            Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show()
            val characteristicsQueue = listOf(
                MoveSenseServices.GENERIC_ACCESS to MoveSenseCharacteristics.DEVICE_NAME,
                MoveSenseServices.GENERIC_ACCESS to MoveSenseCharacteristics.APPEARANCE,
                MoveSenseServices.GENERIC_ACCESS to MoveSenseCharacteristics.CONNECTION_PARAMETERS,
                MoveSenseServices.GENERIC_ACCESS to MoveSenseCharacteristics.CENTRAL_ADDRESS_RESOLUTION,

                MoveSenseServices.GENERIC_ATTRIBUTE to MoveSenseCharacteristics.SERVICE_CHANGED,

                MoveSenseServices.DEVICE_INFORMATION to MoveSenseCharacteristics.MANUFACTURER_NAME,
                MoveSenseServices.DEVICE_INFORMATION to MoveSenseCharacteristics.SERIAL_NUMBER,

                MoveSenseServices.BATTERY_SERVICE to MoveSenseCharacteristics.BATTERY_LEVEL,

                MoveSenseServices.MOVESENSE_SERVICE to MoveSenseCharacteristics.MOVESENSE_DATA_1,
                MoveSenseServices.MOVESENSE_SERVICE to MoveSenseCharacteristics.MOVESENSE_DATA_2
            )

            val (serviceUUID, characteristicUUID) = characteristicsQueue[currentIndex]
            moveSenseManager. readCharacteristic(serviceUUID, characteristicUUID)


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
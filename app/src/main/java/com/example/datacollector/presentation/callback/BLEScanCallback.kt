package com.example.datacollector.presentation.callback

import android.Manifest
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.datacollector.presentation.Bluetooth.DeviceAdapter

class BLEScanCallback(
    private val scannedDevices: MutableList<ScanResult>,
    private val scannedDeviceAddresses: MutableSet<String>,
    private val deviceAdapter: DeviceAdapter,
    private val context: Context
) : ScanCallback() {

    override fun onScanResult(callbackType: Int, result: ScanResult?) {
        super.onScanResult(callbackType, result)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return
        if (result != null && result.device != null &&result.device.name != null && scannedDeviceAddresses.add(result.device.address)) {
            Log.i("BLEScanCallbackUserDebug", "Device Scanned: ${result.device.name}")
            scannedDevices.add(result)
            deviceAdapter.updateDevices(scannedDevices)
        }
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
        super.onBatchScanResults(results)
        Log.i("BLEScanCallbackUserDebug", "onBatchScanResults")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) return

        if (results == null) return
        for (result in results) {
            if (result.device == null || result.device.name == null || !scannedDeviceAddresses.add(result.device.address)) continue
            Log.i("BLEScanCallbackUserDebug", "Device Scanned: ${result.device.name}")
            scannedDevices.add(result)
        }
        deviceAdapter.updateDevices(scannedDevices)
    }

    override fun onScanFailed(errorCode: Int) {
        super.onScanFailed(errorCode)
        Log.e("BLEScanCallbackUserDebug", "Scan failed with error code: $errorCode")
    }
}
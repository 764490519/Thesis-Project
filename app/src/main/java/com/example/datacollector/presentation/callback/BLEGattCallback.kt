package com.example.datacollector.presentation.callback

import android.Manifest
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.datacollector.presentation.MoveSense.MoveSenseCharacteristics

class BLEGattCallback(
    private val context: Context,
    private var bluetoothGatt: BluetoothGatt?
) : BluetoothGattCallback() {

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLEGattCallbackUserDebug", "BLUETOOTH_CONNECT permission not granted!")
            return
        }

        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.i("BLEGattCallbackUserDebug", "Connected to GATT server.")
//                Toast.makeText(context, "Connected to GATT server", Toast.LENGTH_SHORT).show()
                gatt?.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.i("BLEGattCallbackUserDebug", "Disconnected from GATT server.")
                Toast.makeText(context, "Disconnected from GATT server", Toast.LENGTH_SHORT).show()
                bluetoothGatt?.close()
                bluetoothGatt = null
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BLEGattCallbackUserDebug", "BLUETOOTH_CONNECT permission not granted!")
            return
        }
        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
            Log.i("BLEGattCallbackUserDebug", "Services discovered: ${gatt?.services}")

            for (service in gatt.services) {
                Log.i("BLEGattCallbackUserDebug", "Service UUID: ${service.uuid}")

                for (characteristic in service.characteristics) {
                    Log.i("BLEGattCallbackUserDebug", "  Characteristic UUID: ${characteristic.uuid}")
                }
            }

        } else {
            Log.w("BLEGattCallbackUserDebug", "Service discovery failed: $status")
        }
    }

    override fun onCharacteristicRead(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray,
        status: Int
    ) {
        super.onCharacteristicRead(gatt, characteristic, value, status)
        if (status == BluetoothGatt.GATT_SUCCESS && characteristic != null) {
            Log.i("BLEGattCallbackUserDebug", "onCharacteristicRead")
            Log.i("BLEGattCallbackUserDebug", " Characteristic : ${MoveSenseCharacteristics.getCharacteristicName(characteristic.uuid)}")
            Log.i("BLEGattCallbackUserDebug", " ByteArray: ${value.joinToString(", ") { it.toUByte().toString() }}")
            Log.i("BLEGattCallbackUserDebug", " Value Read: ${byteArrayToString(value)}")
        }
    }

    fun byteArrayToString(data: ByteArray): String {
        return String(data, Charsets.UTF_8).trim()
    }
}
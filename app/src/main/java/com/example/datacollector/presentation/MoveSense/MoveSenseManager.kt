package com.example.datacollector.presentation.MoveSense

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.datacollector.presentation.callback.BLEGattCallback
import java.util.UUID

class MoveSenseManager(private val context: Context) {
    private var bluetoothGatt: BluetoothGatt? = null

    fun connect(device: BluetoothDevice) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        bluetoothGatt = device.connectGatt(context, false, BLEGattCallback(context, bluetoothGatt))
    }

    fun readCharacteristic(serviceUUID: UUID, characteristicUUID: UUID) {
        if (bluetoothGatt == null) {
            Log.e("MoveSenseManagerUserDebug", "Bluetooth Gatt is not connected")
            return
        }

        val service = bluetoothGatt!!.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        if (characteristic != null) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            bluetoothGatt!!.readCharacteristic(characteristic)
        } else {
            Log.e("MoveSenseManagerUserDebug", "${MoveSenseCharacteristics.getCharacteristicName(characteristicUUID)}特征值未找到")
        }
    }


    fun checkCharacteristicProperties(serviceUUID: UUID, characteristicUUID: UUID) {
        if (bluetoothGatt == null) {
            Log.e("MoveSenseManagerUserDebug", "Bluetooth Gatt is not connected")
            return
        }
        Log.i("MoveSenseManagerUserDebug", "Check Properties of Characteristic ${MoveSenseCharacteristics.getCharacteristicName(characteristicUUID)}")
        val service = bluetoothGatt!!.getService(serviceUUID)
        val characteristic = service?.getCharacteristic(characteristicUUID)
        val properties = characteristic?.properties
        if(properties == null){
            Log.e("MoveSenseManagerUserDebug", "properties is null!!")
            return
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_READ != 0) {
            Log.i("MoveSenseManagerUserDebug", "  support READ")
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_WRITE != 0) {
            Log.i("MoveSenseManagerUserDebug", "  Supports WRITE")
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
            Log.i("MoveSenseManagerUserDebug", "  Supports NOTIFY")
        }

        if (properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
            Log.i("MoveSenseManagerUserDebug", "  Supports INDICATE")
        }

    }

}



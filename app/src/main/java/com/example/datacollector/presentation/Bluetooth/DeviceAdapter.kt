package com.example.datacollector.presentation.Bluetooth

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.datacollector.R


class DeviceAdapter(
    private val context:Context,
    private var devices: List<ScanResult>,
    private val onDeviceClick: (BluetoothDevice) -> Unit
) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {

    class DeviceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val deviceName: TextView = view.findViewById(R.id.deviceName)
        val deviceAddress: TextView = view.findViewById(R.id.deviceAddress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.device_item, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        val device = devices[position].device
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        holder.deviceName.text = device.name ?: "Unknown Device"
        holder.deviceAddress.text = device.address

        holder.itemView.setOnClickListener {
            Log.i("DeviceAdapterUserDebug","${holder.deviceName.text} is clicked")
            onDeviceClick(device)
        }
    }

    override fun getItemCount(): Int = devices.size

    fun updateDevices(newDevices: List<ScanResult>) {
        devices = newDevices
        notifyDataSetChanged()  // Refresh List
    }
}
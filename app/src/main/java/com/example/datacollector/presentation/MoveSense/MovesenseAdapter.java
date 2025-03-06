package com.example.datacollector.presentation.MoveSense;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.datacollector.R;
import com.example.datacollector.presentation.MoveSense.bluetooth.MdsRx;
import com.example.datacollector.presentation.MoveSense.model.MdsConnectedDevice;
import com.example.datacollector.presentation.MoveSense.model.MdsDeviceInfoNewSw;
import com.example.datacollector.presentation.MoveSense.model.MdsDeviceInfoOldSw;
import com.example.datacollector.presentation.MoveSense.sensorList.SensorListActivity;
import com.example.datacollector.presentation.MoveSense.utils.ThrowableToastingAction;
import com.movesense.mds.Mds;
import com.movesense.mds.internal.connectivity.MovesenseConnectedDevices;
import com.movesense.mds.internal.connectivity.MovesenseDevice;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MovesenseAdapter extends RecyclerView.Adapter<MovesenseAdapter.DeviceViewHolder> {

    private final String TAG = this.getClass().getName() + "UserDebug";

    private List<ScanResult> devices;
    private View.OnClickListener listener;

    public MovesenseAdapter(List<ScanResult> devices,View.OnClickListener listener) {
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_item,parent,false);
        return new DeviceViewHolder(view);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = devices.get(position).getDevice();
        holder.deviceName.setText(device.getName());
        holder.deviceAddress.setText(device.getAddress());
        holder.itemView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

    public void updateDevices(List<ScanResult> newDevices){
        devices = newDevices;
        notifyDataSetChanged();
    }



    public static class DeviceViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView deviceAddress;

        public DeviceViewHolder(View view) {
            super(view);
            deviceName = view.findViewById(R.id.deviceName);
            deviceAddress = view.findViewById(R.id.deviceAddress);
        }
    }



}

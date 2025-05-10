package com.example.datacollector.presentation.model;

import androidx.annotation.NonNull;

public class SensorData implements Comparable<SensorData>{
    public long timestamp;
    public String dataType;
    public String data;

    public SensorData(long timestamp, String dataType, String data) {
        this.timestamp = timestamp;
        this.dataType = dataType;
        this.data = data;
    }

    @Override
    public int compareTo(SensorData other) {
        return Long.compare(this.timestamp, other.timestamp);
    }

    @NonNull
    @Override
    public String toString() {
        return "timestamp: " + timestamp + ", dataType: " + dataType + ", data: " + data;
    }
}

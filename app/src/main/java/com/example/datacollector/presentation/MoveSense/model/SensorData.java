package com.example.datacollector.presentation.MoveSense.model;

import androidx.annotation.NonNull;

public class SensorData implements Comparable<SensorData>{
    public long timestamp;
    public String dataType;
    public Object data;

    public SensorData(long timestamp, String dataType, Object data) {
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

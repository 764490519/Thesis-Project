package com.example.datacollector.presentation.util;

public interface Logger {

    void logData(String data);
    void logData(String date, long timestamp, String type, String data);

}

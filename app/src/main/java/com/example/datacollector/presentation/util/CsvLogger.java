package com.example.datacollector.presentation.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class CsvLogger implements Logger {

    private static final String TAG = "CsvLoggerUserDebug";
    private PrintWriter csvWriter;
    private File csvFile;

    public CsvLogger(Context context) {
        this(context,null);
    }

    public CsvLogger(Context context, String fileName){
        setupCsvFile(context,fileName != null ? fileName : "sensor_data.csv");
    }

    private void setupCsvFile(Context context, String fileName) {
        // /storage/emulated/0/Android/data/com.example.datacollector/files/
        File dir = context.getExternalFilesDir(null);
        csvFile = new File(dir, fileName);
        if (csvFile.exists()) {
            boolean deleted = csvFile.delete();
            Log.d(TAG, "Existing CSV file deleted: " + deleted);
        }
        try {
            csvWriter = new PrintWriter(new FileWriter(csvFile, true));
            Log.d(TAG, "CSV file created at: " + csvFile.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error creating CSV file", e);
        }
    }


    public void logData(String data){
        if (csvWriter == null) return;
        try {
            csvWriter.println(data);
            csvWriter.flush();
        } catch (Exception e) {
            Log.e(TAG, "Error writing to CSV", e);
        }
    }

    public void logData(long timestamp, Integer heartRate, double[] imuAcc) {
        if (csvWriter == null) return;
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(timestamp).append(",");
            sb.append(heartRate != null ? heartRate : "").append(",");
            if (imuAcc != null) {
                sb.append(imuAcc[0]).append(",").append(imuAcc[1]).append(",").append(imuAcc[2]);
            }
            csvWriter.println(sb.toString());
            csvWriter.flush();
        } catch (Exception e) {
            Log.e(TAG, "Error writing to CSV", e);
        }
    }



    public static String formatTime(long time) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String date = format.format(time);
        return date;
    }

    @Override
    public void logData(String date, long timestamp, String type, String data) {
        logData(date + "," + timestamp + "," + type + "," + data);
    }
}

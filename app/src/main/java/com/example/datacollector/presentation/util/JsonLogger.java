package com.example.datacollector.presentation.util;

import android.content.Context;
import java.io.*;
import org.json.JSONObject;

public class JsonLogger implements Logger {
    private final File file;
    public JsonLogger(Context context, String fileName) {
        File dir = context.getExternalFilesDir(null);
        file = new File(dir, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    public void logData(JSONObject jsonObject) {
        try (FileWriter fw = new FileWriter(file, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(jsonObject.toString());
            bw.newLine();  // 每条数据一行
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void logData(String data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logData(jsonObject);
    }

    @Override
    public void logData(String date, long timestamp, String type, String data) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("date", date);
            jsonObject.put("timestamp", timestamp);
            jsonObject.put("type", type);
            jsonObject.put("data", data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        logData(jsonObject);
    }
}
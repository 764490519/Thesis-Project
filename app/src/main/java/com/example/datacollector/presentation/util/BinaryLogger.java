package com.example.datacollector.presentation.util;

import android.content.Context;
import java.io.*;

public class BinaryLogger implements Logger{
    private final File file;

    public BinaryLogger(Context context, String fileName) {
        File dir = context.getExternalFilesDir(null);
        file = new File(dir, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public void logData(String date, long timestamp, String type, String data) {
        try (FileOutputStream fos = new FileOutputStream(file, true);
             DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeLong(timestamp);
            dos.writeUTF(type);
            dos.writeUTF(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void logData(String data){
        try (FileOutputStream fos = new FileOutputStream(file, true);
             DataOutputStream dos = new DataOutputStream(fos)) {
            dos.writeUTF(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
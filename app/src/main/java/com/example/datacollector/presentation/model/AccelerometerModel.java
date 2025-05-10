package com.example.datacollector.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class AccelerometerModel implements Parcelable {

    public float x;
    public float y;
    public float z;
    public long timestamp;

    public AccelerometerModel(float x, float y, float z, long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    protected AccelerometerModel(Parcel in){
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        timestamp = in.readLong();
    }

    public static final Creator<AccelerometerModel> CREATOR = new Creator<AccelerometerModel>() {
        @NonNull
        @Override
        public AccelerometerModel createFromParcel(@NonNull Parcel in) {
            return new AccelerometerModel(in);
        }

        @NonNull
        @Override
        public AccelerometerModel[] newArray(int size) {
            return new AccelerometerModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeFloat(x);
        dest.writeFloat(y);
        dest.writeFloat(z);
        dest.writeLong(timestamp);
    }
}

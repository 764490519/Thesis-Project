package com.example.datacollector.presentation.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class GyroscopeModel implements Parcelable {

    public float x;
    public float y;
    public float z;
    public long timestamp;

    public GyroscopeModel(float x, float y, float z, long timestamp) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.timestamp = timestamp;
    }

    protected GyroscopeModel(Parcel in){
        x = in.readFloat();
        y = in.readFloat();
        z = in.readFloat();
        timestamp = in.readLong();
    }

    public static final Creator<GyroscopeModel> CREATOR = new Creator<GyroscopeModel>() {
        @NonNull
        @Override
        public GyroscopeModel createFromParcel(@NonNull Parcel in) {
            return new GyroscopeModel(in);
        }

        @NonNull
        @Override
        public GyroscopeModel[] newArray(int size) {
            return new GyroscopeModel[size];
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

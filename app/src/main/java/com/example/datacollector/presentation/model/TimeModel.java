package com.example.datacollector.presentation.model;

import com.google.gson.annotations.SerializedName;

//For subscribe /Time
public class TimeModel {

    @SerializedName("Body")
    public final long body;

    public TimeModel(long body){
        this.body = body;
    }

}

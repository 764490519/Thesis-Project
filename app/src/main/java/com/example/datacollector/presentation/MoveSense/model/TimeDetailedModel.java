package com.example.datacollector.presentation.MoveSense.model;

import com.google.gson.annotations.SerializedName;

public class TimeDetailedModel {

    @SerializedName("Content")
    public final Content content;

    public TimeDetailedModel(Content content){
        this.content = content;
    }


    public static class Content{

        @SerializedName("utcTime")
        public final long utcTime;

        @SerializedName("relativeTime")
        public final long relativeTime;

        @SerializedName("tickRate")
        public final int tickRate;;

        @SerializedName("accuracy")
        public final int accuracy;

        public Content(long utcTime, long relativeTime, int tickRate, int accuracy){
            this.utcTime = utcTime;
            this.relativeTime = relativeTime;
            this.tickRate = tickRate;
            this.accuracy = accuracy;
        }
    }

}

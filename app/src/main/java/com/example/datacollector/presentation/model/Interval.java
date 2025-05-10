package com.example.datacollector.presentation.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Interval {
    public long start;
    public long end;

    public Interval(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public static Interval getMostConfidentInterval(List<Interval> intervals) {
        class Point {
            long time;
            boolean isStart;

            Point(long time, boolean isStart) {
                this.time = time;
                this.isStart = isStart;
            }
        }

        List<Point> points = new ArrayList<>();
        for (Interval interval : intervals) {
            points.add(new Point(interval.start, true));
            points.add(new Point(interval.end, false));
        }

        points.sort((a, b) -> {
            if (a.time != b.time) {
                return Long.compare(a.time, b.time);
            } else {
                return Boolean.compare(b.isStart,a.isStart);
            }
        });

        int count = 0;
        int maxCount = 0;
        long bestStart = 0, bestEnd = 0;
        long tempStart = 0;

        for(Point point : points){
            if(point.isStart){
                count++;
                if (count > maxCount){
                    maxCount = count;
                    tempStart = point.time;
                }
            }else {
                if (count == maxCount){
                    bestStart = tempStart;
                    bestEnd = point.time;
                }
                count--;
            }
        }
        return new Interval(bestStart,bestEnd);
    };
}

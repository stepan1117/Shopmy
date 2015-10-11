package com.shopmy.shopmy.model;

import org.joda.time.LocalTime;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class TimeSpan {
    private LocalTime start;
    private LocalTime end;

    public TimeSpan(LocalTime start, LocalTime end){
        this.start = start;
        this.end = end;
    }

    public LocalTime getStart() {
        return start;
    }

    public void setStart(LocalTime start) {
        this.start = start;
    }

    public LocalTime getEnd() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end = end;
    }
}

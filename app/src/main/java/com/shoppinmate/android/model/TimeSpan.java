package com.shoppinmate.android.model;

import org.joda.time.LocalTime;

import java.io.Serializable;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class TimeSpan implements Serializable {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TimeSpan timeSpan = (TimeSpan) o;

        if (start != null ? !start.equals(timeSpan.start) : timeSpan.start != null) return false;
        return !(end != null ? !end.equals(timeSpan.end) : timeSpan.end != null);

    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (end != null ? end.hashCode() : 0);
        return result;
    }
}

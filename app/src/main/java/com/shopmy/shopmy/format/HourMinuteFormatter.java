package com.shopmy.shopmy.format;

import com.shopmy.shopmy.model.TimeSpan;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Interval;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * Created by stepan on 11. 10. 2015.
 */
public class HourMinuteFormatter {
    public static String formatLocalTime(LocalTime time) {
        String str = new Integer(time.get(DateTimeFieldType.hourOfDay())).toString();
        int minutes = time.get(DateTimeFieldType.minuteOfHour());
        if (minutes != 0) {
            str += String.format(":%02d", minutes);
        }
        return str;
    }

    public static String formatTimeSpan(TimeSpan ts) {
        return formatLocalTime(ts.getStart()) + " - " + formatLocalTime(ts.getEnd());
    }

    public static String formatInterval(Interval interval) {
        return formatLocalTime(interval.getStart().toLocalTime()) + " - " +
                formatLocalTime(interval.getEnd().toLocalTime());
    }



}

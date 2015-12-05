package com.shoppinmate.android.service;

import com.shoppinmate.android.model.ShopInfo;
import com.shoppinmate.android.model.TimeSpan;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import java.util.List;

/**
 * Created by stepan on 4. 10. 2015.
 */
public class ShopInfoService {
    public enum ShopStatus {OPEN, CLOSING_SOON, CLOSED};


    public static ShopStatus decideShopStatus(ShopInfo info){
        LocalDate now = new LocalDate();
        ShopInfo.DAYS day = ShopInfo.DAYS.values()[now.getDayOfWeek() - 1];
        List<TimeSpan> hours = info.getOpeningHours().get(day.toString());
        if (hours != null){
            for (TimeSpan span : hours){
                Interval interval = new Interval(span.getStart().toDateTimeToday(), span.getEnd().toDateTimeToday());
                if (interval.containsNow()){
                    if (interval.getEnd().isAfter(new DateTime().plusMinutes(15))){
                        return ShopStatus.OPEN;
                    } else {
                        return ShopStatus.CLOSING_SOON;
                    }
                }
            }
        }

        return ShopStatus.CLOSED;
    }

}



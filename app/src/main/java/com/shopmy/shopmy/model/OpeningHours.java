package com.shopmy.shopmy.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by stepan on 11. 10. 2015.
 */
public class OpeningHours {
    private List<TimeSpan> timeSpans = new ArrayList<>();

    public List<TimeSpan> getTimeSpans() {
        return timeSpans;
    }

}

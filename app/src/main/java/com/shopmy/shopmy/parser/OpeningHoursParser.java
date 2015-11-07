package com.shopmy.shopmy.parser;

import android.text.TextUtils;

import com.shopmy.shopmy.exception.TimeSpanParseException;
import com.shopmy.shopmy.format.HourMinuteFormatter;
import com.shopmy.shopmy.model.TimeSpan;

import org.joda.time.IllegalFieldValueException;
import org.joda.time.Instant;
import org.joda.time.Interval;
import org.joda.time.LocalTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class OpeningHoursParser {

    private static final String TIMESPAN_FORMAT = "([012]?\\d):?(\\d{2})?-([012]?\\d):?(\\d{2})?";
    private static final Pattern PATTERN = Pattern.compile(TIMESPAN_FORMAT);

    public List<TimeSpan> fromString(String str) throws TimeSpanParseException {
        List<TimeSpan> spans = new ArrayList<>();

        if (TextUtils.isEmpty(str)) {
            return null;
        }

        str = str.replace(" ", "");

        StringTokenizer tok = new StringTokenizer(str, ",");
        int tokenCount = 0;
        while (tok.hasMoreTokens()) {
            tokenCount++;
            String token = tok.nextToken();

            Matcher matcher = PATTERN.matcher(token);
            if (!matcher.matches()) {
                throw new TimeSpanParseException("Unable to match the format for " +
                        "input '" + token + "'", tokenCount);
            } else if (matcher.groupCount() != 4) {
                throw new TimeSpanParseException("Invalid number of matching groups (" +
                        matcher.groupCount() + ") for input " + token, tokenCount);
            }
            int hourStart = new Integer(matcher.group(1));
            int minuteStart = 0;
            if (matcher.group(2) != null) {
                minuteStart = new Integer(matcher.group(2));
            }
            int hourEnd = new Integer(matcher.group(3));
            int minuteEnd = 0;
            if (matcher.group(4) != null) {
                minuteEnd = new Integer(matcher.group(4));
            }
            try {
                spans.add(new TimeSpan(new LocalTime(hourStart, minuteStart),
                        new LocalTime(hourEnd, minuteEnd)));
            } catch (IllegalFieldValueException e) {
                throw new TimeSpanParseException("'" + e.getIllegalStringValue() + "' is not a " +
                        "valid value.",tokenCount,e);
            }

        }

        List<Interval> intervals = new ArrayList<>();

        for (TimeSpan ts : spans) {
            try {
                intervals.add(new Interval(ts.getStart().toDateTime(new Instant(0)),
                                ts.getEnd().toDateTime(new Instant(0)))
                );
            } catch (Exception e) {
                throw new TimeSpanParseException(
                        HourMinuteFormatter.formatTimeSpan(ts) + " is not a valid timespan. ",
                        tokenCount,
                        e);
            }
        }

        Collections.sort(intervals, new Comparator<Interval>() {
            @Override
            public int compare(Interval x, Interval y) {
                return x.getStart().compareTo(y.getStart());
            }

            @Override
            public boolean equals(Object object) {
                return false;
            }
        });

        for (int i = 0, n = intervals.size(); i < n - 1; i++) {
            if (intervals.get(i).overlaps(intervals.get(i + 1))) {
                throw new TimeSpanParseException(
                        HourMinuteFormatter.formatInterval(intervals.get(i))
                                + " overlaps with "
                                + HourMinuteFormatter.formatInterval(intervals.get(i + 1)),
                        tokenCount);
            }
        }

        return spans;
    }
}

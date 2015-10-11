package com.shopmy.shopmy.parser;

import android.text.TextUtils;

import com.shopmy.shopmy.model.TimeSpan;

import org.joda.time.LocalTime;
import org.joda.time.TimeOfDay;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class OpeningHoursParser {

    private static final String TIMESPAN_FORMAT = "(\\d?\\d):(\\d{2})-(\\d?\\d):(\\d{2})";
    private static final Pattern PATTERN = Pattern.compile(TIMESPAN_FORMAT);

    public List<TimeSpan> fromString(String str) throws ParseException {
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
                throw new ParseException("Unable to match the format " + TIMESPAN_FORMAT + " for " +
                        "input " + token, tokenCount);
            } else if (matcher.groupCount() != 4) {
                throw new ParseException("Invalid number of matching groups (" +
                        matcher.groupCount() + ") for input " + token, tokenCount);
            }
            int hourStart = new Integer(matcher.group(1));
            int minuteStart = new Integer(matcher.group(2));
            int hourEnd = new Integer(matcher.group(3));
            int minuteEnd = new Integer(matcher.group(4));
            spans.add(new TimeSpan(new LocalTime(hourStart, minuteStart),
                    new LocalTime(hourEnd, minuteEnd)));


        }

        return spans;
    }


}

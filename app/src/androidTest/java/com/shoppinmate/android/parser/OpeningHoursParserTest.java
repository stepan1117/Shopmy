package com.shoppinmate.android.parser;

import android.test.suitebuilder.annotation.SmallTest;

import com.shoppinmate.android.model.TimeSpan;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class OpeningHoursParserTest extends TestCase {

    @SmallTest
    public void testParse() throws Exception{
        OpeningHoursParser parser = new OpeningHoursParser();
        List<TimeSpan> timeSpans = parser.fromString("8:00 - 14, 05:30-19:00,0500-1430");
        assertEquals(timeSpans.size(), 3);
    }
}

package com.shopmy.shopmy.parser;

import android.test.ApplicationTestCase;
import android.test.suitebuilder.annotation.SmallTest;

import com.shopmy.shopmy.model.TimeSpan;

import junit.framework.TestCase;

import java.util.List;

/**
 * Created by Stepan on 11. 10. 2015.
 */
public class OpeningHoursParserTest extends TestCase {

    @SmallTest
    public void testParse() throws Exception{
        OpeningHoursParser parser = new OpeningHoursParser();
        List<TimeSpan> timeSpans = parser.fromString("8:00 - 14:00, 15:30-19:00");
        assertEquals(timeSpans.size(), 2);
    }
}

package com.shoppinmate.android.exception;

/**
 * Created by stepan on 11. 10. 2015.
 */
public class TimeSpanParseException extends Exception {
    private int location;

    public TimeSpanParseException(String description, int location, Throwable cause){
        super(description, cause);
        this.location = location;
    }

    public TimeSpanParseException(String description, int location){
        super(description);
        this.location = location;
    }

}

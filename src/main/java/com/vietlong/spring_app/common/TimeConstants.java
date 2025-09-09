package com.vietlong.spring_app.common;

public class TimeConstants {

    public static final long MILLISECONDS_PER_SECOND = 1000L;
    public static final long SECONDS_PER_MINUTE = 60L;
    public static final long MINUTES_PER_HOUR = 60L;
    public static final long HOURS_PER_DAY = 24L;

    public static final long MILLISECONDS_PER_HOUR = MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE * MINUTES_PER_HOUR;

    private TimeConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}

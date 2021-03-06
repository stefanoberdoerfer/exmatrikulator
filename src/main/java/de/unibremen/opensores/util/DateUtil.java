package de.unibremen.opensores.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * An Utility class for date related utility functions.
 * @author Kevin Scheck.
 */
public final class DateUtil {

    /**
     * Private constructor for no objects of this class.
     */
    private DateUtil() {

    }

    /**
     * Gets the current date time in the current time zone.
     * @return The current date time in the time zone of the exmatrikulator.
     */
    public static Date getDateTime() {
        return Calendar
                .getInstance(TimeZone.getTimeZone(Constants.SYSTEM_TIMEZONE))
                .getTime();
    }

    /**
     * Removes the time of a date.
     * Sets the date time to 00:00:00
     * @param date The date object which time should be remove.
     * @return The date with the time set to 00:00:00
     * @throws IllegalArgumentException If the date parameter is null.
     */
    public static Date removeTime(Date date) {
        if (date == null) {
            throw new IllegalArgumentException("The date can't be null");
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }


    /**
     * Returns a date from which is 10 year ago from now.
     * @return The date which is 10 years ago from now.
     */
    public static Date tenYearsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDateTime());
        cal.add(Calendar.YEAR, -10);
        return cal.getTime();
    }

    /**
     * Returns a date from which is 20 year ago from now.
     * @return The date which is 20 years ago from now.
     */
    public static Date twentyYearsAgo() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDateTime());
        cal.add(Calendar.YEAR, -20);
        return cal.getTime();
    }


    /**
     * Returns a date from which is 10 year later from now.
     * @return The date which is 10 years later from now.
     */
    public static Date tenYearsLater() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDateTime());
        cal.add(Calendar.YEAR, +10);
        return cal.getTime();
    }
}

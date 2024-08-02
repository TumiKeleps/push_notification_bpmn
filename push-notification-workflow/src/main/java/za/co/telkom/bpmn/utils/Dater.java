package za.co.telkom.bpmn.utils;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class Dater 
{
    public static  String ISO8601ToUTC(String dateTimeStr)
    {
         ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);

        // Convert the time to UTC+2
        ZonedDateTime zdtInUTCPlus2 = zdt.withZoneSameInstant(ZoneId.of("UTC+2"));

        // Format the converted time back to ISO 8601 format
        String formattedDateTime = zdtInUTCPlus2.format(DateTimeFormatter.ISO_DATE_TIME);

        return formattedDateTime;
    }

    public static String getDayBefore(String dateTimeStr) 
    {
        // Parse the given time to a ZonedDateTime
        ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);

        // Subtract one day from the given time
        ZonedDateTime zdtDayBefore = zdt.minus(1, ChronoUnit.DAYS);

        // Format the result back to ISO 8601 format
        return zdtDayBefore.format(DateTimeFormatter.ISO_DATE_TIME);
    }

    public static String getDayAfter(String dateTimeStr) 
    {
        // Parse the given time to a ZonedDateTime
        ZonedDateTime zdt = ZonedDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);

        // Add one day to the given time
        ZonedDateTime zdtDayAfter = zdt.plus(1, ChronoUnit.DAYS);

        // Format the result back to ISO 8601 format
        return zdtDayAfter.format(DateTimeFormatter.ISO_DATE_TIME);
    }
}

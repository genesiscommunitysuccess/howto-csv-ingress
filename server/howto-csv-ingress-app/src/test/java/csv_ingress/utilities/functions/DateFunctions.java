package csv_ingress.utilities.functions;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class DateFunctions {
    public static Long todayDateInEpoch() {
        final String pattern = "MM/dd/yyyy HH:mm:ss";
        final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        final LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        final String formattedStartOfDay = startOfDay.format(dateTimeFormatter);
        return LocalDateTime.parse(formattedStartOfDay, dateTimeFormatter).toEpochSecond(ZoneOffset.UTC);
    }

    public static String todayDate_MM_dd_yyyy() {
        final String dateFormat = "yyyy-dd-MM";
        ZonedDateTime currentDateTime = ZonedDateTime.now(ZoneId.of("America/New_York"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return currentDateTime.format(formatter);
    }
}

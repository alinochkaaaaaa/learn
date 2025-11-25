package org;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderParser {

    public static class Result {
        private final LocalDateTime triggerTime;
        private final String text;

        public Result(LocalDateTime triggerTime, String text) {
            this.triggerTime = triggerTime;
            this.text = text;
        }

        public LocalDateTime getTriggerTime() {
            return triggerTime;
        }

        public String getText() {
            return text;
        }
    }

    public static Result parse(String input) {
        if (!input.toLowerCase().startsWith("напомни ")) return null;

        String rest = input.substring("напомни ".length()).trim();

        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.of(9, 0); // утро по умолчанию

        if (rest.startsWith("сегодня ")) {
            rest = rest.substring("сегодня ".length());
        } else if (rest.startsWith("завтра ")) {
            date = date.plusDays(1);
            rest = rest.substring("завтра ".length());
        } else if (rest.startsWith("послезавтра ")) {
            date = date.plusDays(2);
            rest = rest.substring("послезавтра ".length());
        }

        Pattern timePattern = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})\\s*(.*)");
        Matcher timeMatcher = timePattern.matcher(rest);
        if (timeMatcher.matches()) {
            int hour = Integer.parseInt(timeMatcher.group(1));
            int minute = Integer.parseInt(timeMatcher.group(2));
            String reminderText = timeMatcher.group(3).trim();
            if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                time = LocalTime.of(hour, minute);
                return new Result(LocalDateTime.of(date, time), reminderText);
            }
        }

        // Если нет времени — использовать утро
        Pattern dateOnlyPattern = Pattern.compile("(сегодня|завтра|послезавтра)?\\s*(.*)");
        Matcher m = dateOnlyPattern.matcher(rest);
        if (m.matches()) {
            String text = m.group(2).trim();
            if (!text.isEmpty()) {
                return new Result(LocalDateTime.of(date, time), text);
            }
        }

        return null;
    }
}
package org;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReminderParser {
    private static final LocalTime DEFAULT_TIME = LocalTime.of(9, 0);
    private static final Pattern TIME_PATTERN = Pattern.compile("в\\s+(\\d{1,2}):(\\d{2})\\s*(.*)", Pattern.CASE_INSENSITIVE);
    private static final Pattern RELATIVE_PATTERN = Pattern.compile(
            "через\\s+(\\d+)\\s+(минут[ауы]?|час[аов]?|день|дня|дней)\\s*(.*)",
            Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DATE_PATTERN = Pattern.compile("(\\d{1,2})[./](\\d{1,2})(?:[./](\\d{2,4}))?");

    public static class ParseResult {
        private final LocalDateTime triggerTime;
        private final String text;

        public ParseResult(LocalDateTime triggerTime, String text) {
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

    public static ParseResult parse(String input) {
        if (input == null || !input.toLowerCase().startsWith("напомни ")) {
            return null;
        }

        String rest = input.substring("напомни ".length()).trim();

        LocalDateTime now = LocalDateTime.now();
        LocalDate date = now.toLocalDate();
        LocalTime time = now.toLocalTime();
        String reminderText = "";

        Matcher relativeMatcher = RELATIVE_PATTERN.matcher(rest);
        if (relativeMatcher.matches()) {
            try {
                int amount = Integer.parseInt(relativeMatcher.group(1));
                String unit = relativeMatcher.group(2).toLowerCase();
                reminderText = relativeMatcher.group(3).trim();

                LocalDateTime triggerTime = now;

                if (unit.startsWith("минут")) {
                    triggerTime = now.plusMinutes(amount);
                } else if (unit.startsWith("час")) {
                    triggerTime = now.plusHours(amount);
                } else if (unit.startsWith("день")) {
                    triggerTime = now.plusDays(amount);
                }

                if (!isValidReminderTime(triggerTime)) {
                    return null;
                }

                return new ParseResult(triggerTime, reminderText);

            } catch (Exception e) {
                System.err.println(" Ошибка парсинга относительного времени: " + e.getMessage());
            }
        }

        Matcher timeMatcher = TIME_PATTERN.matcher(rest);
        if (timeMatcher.matches()) {
            try {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                reminderText = timeMatcher.group(3).trim();


                LocalDateTime triggerTime = LocalDateTime.of(date, time);
                if (!isValidReminderTime(triggerTime)) {
                    return null;
                }

                return new ParseResult(triggerTime, reminderText);

            } catch (Exception e) {
                System.err.println(" Ошибка парсинга времени: " + e.getMessage());
            }
        }

        if (rest.startsWith("сегодня ")) {
            rest = rest.substring("сегодня ".length());
            date = LocalDate.now();
        } else if (rest.startsWith("завтра ")) {
            rest = rest.substring("завтра ".length());
            date = LocalDate.now().plusDays(1);
        } else if (rest.startsWith("послезавтра ")) {
            rest = rest.substring("послезавтра ".length());
            date = LocalDate.now().plusDays(2);
        }

        Matcher dateMatcher = DATE_PATTERN.matcher(rest);
        if (dateMatcher.find()) {
            try {
                int day = Integer.parseInt(dateMatcher.group(1));
                int month = Integer.parseInt(dateMatcher.group(2));
                int year = dateMatcher.group(3) != null ?
                        Integer.parseInt(dateMatcher.group(3)) :
                        LocalDate.now().getYear();

                if (year < 100) {
                    year += 2000;
                }

                date = LocalDate.of(year, month, day);
                rest = rest.substring(dateMatcher.end()).trim();

            } catch (Exception e) {
                System.err.println(" Ошибка парсинга даты: " + e.getMessage());
            }
        }

        timeMatcher = TIME_PATTERN.matcher(rest);
        if (timeMatcher.matches()) {
            try {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                reminderText = timeMatcher.group(3).trim();

                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    time = LocalTime.of(hour, minute);
                }


            } catch (Exception e) {
                System.err.println(" Ошибка парсинга времени: " + e.getMessage());
            }
        } else {
            time = DEFAULT_TIME;
            reminderText = rest;
        }

        if (reminderText.isEmpty()) {
            System.err.println(" Текст напоминания пустой");
            return null;
        }

        LocalDateTime triggerTime = LocalDateTime.of(date, time);

        if (!isValidReminderTime(triggerTime)) {
            return null;
        }

        return new ParseResult(triggerTime, reminderText);
    }

    private static boolean isValidReminderTime(LocalDateTime triggerTime) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime maxPastTime = now.minusMinutes(5);
        LocalDateTime maxFutureTime = now.plusYears(5);

        if (triggerTime.isBefore(maxPastTime)) {
            System.err.println(" Ошибка: Нельзя установить напоминание более чем на 5 минут в прошлом");
            return false;
        }

        if (triggerTime.isAfter(maxFutureTime)) {
            System.err.println(" Ошибка: Нельзя установить напоминание более чем на 5 лет в будущем");
            return false;
        }

        if (triggerTime.isBefore(now.minusMinutes(1))) {
            System.err.println(" Ошибка: Напоминание должно быть установлено хотя бы на 1 минуту в будущем");
            return false;
        }

        return true;
    }
}
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
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

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

                System.out.println(" Распознан относительный формат: через " + amount + " " + unit);
                System.out.println("   Время срабатывания: " + triggerTime);
                System.out.println("   Текст: \"" + reminderText + "\"");

                return new ParseResult(triggerTime, reminderText);

            } catch (Exception e) {
                System.err.println("❌ Ошибка парсинга относительного времени: " + e.getMessage());
            }
        }

        Matcher timeMatcher = TIME_PATTERN.matcher(rest);
        if (timeMatcher.matches()) {
            try {
                int hour = Integer.parseInt(timeMatcher.group(1));
                int minute = Integer.parseInt(timeMatcher.group(2));
                reminderText = timeMatcher.group(3).trim();

                if (hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59) {
                    time = LocalTime.of(hour, minute);
                    if (time.isBefore(now.toLocalTime())) {
                        date = date.plusDays(1);
                        System.out.println("⏰ Время уже прошло, переношу на завтра");
                    }
                }

                System.out.println("✅ Распознано время: " + time);
                System.out.println("   Дата: " + date);
                System.out.println("   Текст: \"" + reminderText + "\"");

                return new ParseResult(LocalDateTime.of(date, time), reminderText);

            } catch (Exception e) {
                System.err.println("❌ Ошибка парсинга времени: " + e.getMessage());
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

                if (year < 100) year += 2000;

                date = LocalDate.of(year, month, day);
                rest = rest.substring(dateMatcher.end()).trim();
                System.out.println(" Распознана дата: " + date);

            } catch (Exception e) {
                System.err.println("❌ Ошибка парсинга даты: " + e.getMessage());
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

                System.out.println("✅ Распознано время: " + time);

            } catch (Exception e) {
                System.err.println("❌ Ошибка парсинга времени: " + e.getMessage());
            }
        } else {
            time = DEFAULT_TIME;
            reminderText = rest;
        }

        if (reminderText.isEmpty()) {
            System.err.println("❌ Текст напоминания пустой");
            return null;
        }

        LocalDateTime triggerTime = LocalDateTime.of(date, time);
        System.out.println("✅ Итоговое время: " + triggerTime);
        System.out.println("✅ Текст: \"" + reminderText + "\"");

        return new ParseResult(triggerTime, reminderText);
    }
}
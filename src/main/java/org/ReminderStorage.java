package org;

import java.util.List;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ReminderStorage {
    private static ReminderRepository repository;
    private static final int MAX_FUTURE_DAYS = 365 * 5;
    private static final int MAX_PAST_MINUTES = -5;

    public static void initialize(String connectionString) {
        try {
            repository = new ReminderRepository(connectionString);
        } catch (Exception e) {
            System.err.println("Ошибка инициализации ReminderStorage: " + e.getMessage());
            throw new RuntimeException("Не удалось инициализировать ReminderStorage", e);
        }
    }

    private static void validateReminderTime(LocalDateTime triggerTime) {
        LocalDateTime now = LocalDateTime.now();

        long minutesAgo = ChronoUnit.MINUTES.between(triggerTime, now);
        if (minutesAgo > Math.abs(MAX_PAST_MINUTES)) {
            throw new IllegalArgumentException("Нельзя установить напоминание более чем на " +
                    Math.abs(MAX_PAST_MINUTES) + " минут в прошлом");
        }

        long daysFuture = ChronoUnit.DAYS.between(now, triggerTime);
        if (daysFuture > MAX_FUTURE_DAYS) {
            throw new IllegalArgumentException("Нельзя установить напоминание более чем на " +
                    MAX_FUTURE_DAYS + " дней в будущем");
        }

        if (triggerTime.isBefore(now.minusMinutes(1))) {
            throw new IllegalArgumentException("Напоминание должно быть установлено хотя бы на 1 минуту в будущем");
        }
    }

    public static void add(Reminder reminder) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }

        validateReminderTime(reminder.getTriggerTime());

        repository.save(reminder);
        System.out.println("Напоминание сохранено в базу данных: " + reminder.getMessage());
    }

    public static List<Reminder> getAllByChatId(long chatId) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
        List<Reminder> reminders = repository.findActiveByChatId(chatId);
        System.out.println("Загружено напоминаний для chatId " + chatId + ": " + reminders.size());
        return reminders;
    }

    public static List<Reminder> getAllActive() {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
        List<Reminder> reminders = repository.findAllActive();
        return reminders;
    }

    public static void markAsCompleted(Reminder reminder) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
        repository.markAsCompleted(reminder);
        System.out.println("Напоминание отмечено как выполненное: " + reminder.getMessage());
    }

    public static void delete(String reminderId, long chatId) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
        repository.delete(reminderId, chatId);
    }

    public static void update(String reminderId, long chatId, Reminder updatedReminder) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }

        validateReminderTime(updatedReminder.getTriggerTime());

        repository.update(reminderId, chatId, updatedReminder);
    }

    public static boolean isValidReminderTime(LocalDateTime triggerTime) {
        try {
            validateReminderTime(triggerTime);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static String getValidationErrorMessage(LocalDateTime triggerTime) {
        LocalDateTime now = LocalDateTime.now();

        if (triggerTime.isBefore(now.minusMinutes(Math.abs(MAX_PAST_MINUTES)))) {
            return "Нельзя установить напоминание более чем на " + Math.abs(MAX_PAST_MINUTES) + " минут в прошлом";
        }

        long daysFuture = ChronoUnit.DAYS.between(now, triggerTime);
        if (daysFuture > MAX_FUTURE_DAYS) {
            return "Нельзя установить напоминание более чем на " + MAX_FUTURE_DAYS + " дней в будущем";
        }

        if (triggerTime.isBefore(now.minusMinutes(1))) {
            return "Напоминание должно быть установлено хотя бы на 1 минуту в будущем";
        }

        return null;
    }
}
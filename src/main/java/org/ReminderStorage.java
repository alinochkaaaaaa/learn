package org;

import java.util.ArrayList;
import java.util.List;

public class ReminderStorage {
    private static ReminderRepository repository;

    public static void initialize(String connectionString) {
        try {
            repository = new ReminderRepository(connectionString);
            System.out.println("ReminderStorage успешно инициализирован");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации ReminderStorage: " + e.getMessage());
            throw new RuntimeException("Не удалось инициализировать ReminderStorage", e);
        }
    }

    public static void add(Reminder reminder) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
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
        System.out.println("Всего активных напоминаний в базе: " + reminders.size());
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
        System.out.println("Напоминание удалено из базы данных: " + reminderId);
    }

    public static void update(String reminderId, long chatId, Reminder updatedReminder) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
        repository.update(reminderId, chatId, updatedReminder);
        System.out.println("Напоминание обновлено в базе данных: " + reminderId);
    }

    public static Reminder getById(String reminderId, long chatId) {
        if (repository == null) {
            throw new IllegalStateException("ReminderStorage не инициализирован. Вызовите initialize() сначала.");
        }
        return repository.findById(reminderId, chatId);
    }
}
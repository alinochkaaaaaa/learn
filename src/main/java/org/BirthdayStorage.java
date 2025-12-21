package org;

import java.time.LocalDate;
import java.util.List;

public class BirthdayStorage {
    private static BirthdayRepository repository;

    public static void initialize(String connectionString) {
        try {
            repository = new BirthdayRepository(connectionString);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось инициализировать BirthdayStorage", e);
        }
    }

    public static void addContact(BirthdayContact contact) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        repository.save(contact);
    }

    public static List<BirthdayContact> getContactsByChatId(long chatId) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        return repository.findByChatId(chatId);
    }

    public static BirthdayContact getContact(String contactId, long chatId) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        return repository.findById(contactId, chatId);
    }

    public static void updateBirthday(String contactId, long chatId, LocalDate birthday) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        repository.updateBirthday(contactId, chatId, birthday);
    }

    public static void toggleNotifications(String contactId, long chatId, boolean enabled) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        repository.toggleNotifications(contactId, chatId, enabled);
    }

    public static void updateReminderSettings(String contactId, long chatId, int daysBefore, String customMessage) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        repository.updateReminderSettings(contactId, chatId, daysBefore, customMessage);
    }

    public static void deleteContact(String contactId, long chatId) {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        repository.delete(contactId, chatId);
    }

    public static List<BirthdayContact> getAllContacts() {
        if (repository == null) {
            throw new IllegalStateException("BirthdayStorage не инициализирован");
        }
        return repository.findAll();
    }
}
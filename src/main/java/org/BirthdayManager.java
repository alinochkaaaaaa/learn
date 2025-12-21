package org;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BirthdayManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final OutputProvider outputProvider;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public BirthdayManager(OutputProvider outputProvider) {
        this.outputProvider = outputProvider;
        startBirthdayChecker();
    }

    public void addContact(long chatId, long contactId, String contactName, LocalDate birthday) {
        BirthdayContact contact = new BirthdayContact(chatId, contactId, contactName, birthday);
        BirthdayStorage.addContact(contact);
        scheduleBirthdayReminder(contact);
    }

    public void updateContactBirthday(String contactId, long chatId, LocalDate birthday) {
        BirthdayStorage.updateBirthday(contactId, chatId, birthday);
        BirthdayContact contact = BirthdayStorage.getContact(contactId, chatId);
        if (contact != null && contact.isNotificationsEnabled()) {
            scheduleBirthdayReminder(contact);
        }
    }


    private void scheduleBirthdayReminder(BirthdayContact contact) {
        if (contact.getBirthday() == null || !contact.isNotificationsEnabled()) {
            return;
        }

        LocalDate today = LocalDate.now();
        LocalDate nextBirthday = contact.getBirthday().withYear(today.getYear());

        if (nextBirthday.isBefore(today)) {
            nextBirthday = nextBirthday.plusYears(1);
        }

        LocalDate reminderDate = nextBirthday.minusDays(contact.getDaysBefore());
        LocalDateTime reminderDateTime = reminderDate.atTime(9, 0);

        long delay = java.time.Duration.between(LocalDateTime.now(), reminderDateTime).toMillis();

        if (delay > 0) {
            scheduler.schedule(() -> {
                sendBirthdayReminder(contact);
            }, delay, TimeUnit.MILLISECONDS);
        }
    }

    private void sendBirthdayReminder(BirthdayContact contact) {
        String message = contact.getCustomMessage() + ": " + contact.getContactName() +
                " (" + contact.getBirthday().format(DATE_FORMATTER) + ")";
        Reminder reminder = new Reminder(contact.getChatId(), message,
                LocalDateTime.now().plusMinutes(1), true,
                contact.getContactName(), contact.getContactId());
        ReminderStorage.add(reminder);
    }

    private void startBirthdayChecker() {
        scheduler.scheduleAtFixedRate(() -> {
            checkAndScheduleBirthdays();
        }, 0, 1, TimeUnit.DAYS);
    }

    private void checkAndScheduleBirthdays() {
        List<BirthdayContact> allContacts = BirthdayStorage.getAllContacts();
        for (BirthdayContact contact : allContacts) {
            if (contact.isNotificationsEnabled() && contact.getBirthday() != null) {
                scheduleBirthdayReminder(contact);
            }
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
package org;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderService {
    private final List<Reminder> reminders = new ArrayList<>();
    private final TelegramOutputProvider outputProvider;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public ReminderService(TelegramOutputProvider outputProvider) {
        this.outputProvider = outputProvider;
    }

    public void scheduleReminder(Reminder reminder) {
        reminders.add(reminder);
        long delay = java.time.Duration.between(LocalDateTime.now(), reminder.getTriggerTime()).toMillis();
        if (delay > 0) {
            scheduler.schedule(() -> {
                outputProvider.setCurrentChatId(reminder.getChatId());
                outputProvider.output(" Напоминание: " + reminder.getMessage());
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            // Если время уже прошло — отправить сразу
            outputProvider.setCurrentChatId(reminder.getChatId());
            outputProvider.output(" Событие прошло: " + reminder.getMessage());
        }
    }

    public List<Reminder> getAllReminders() {
        return new ArrayList<>(reminders);
    }
}
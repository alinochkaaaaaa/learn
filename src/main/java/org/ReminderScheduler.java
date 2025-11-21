package org;

import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    public void schedule(Reminder reminder, TelegramOutputProvider outputProvider) {
        long delay = java.time.Duration.between(LocalDateTime.now(), reminder.getTriggerTime()).toMillis();
        if (delay > 0) {
            scheduler.schedule(() -> {
                outputProvider.setCurrentChatId(reminder.getChatId());
                outputProvider.output("Напоминание: " + reminder.getMessage());
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            outputProvider.setCurrentChatId(reminder.getChatId());
            outputProvider.output("Напоминание (просрочено): " + reminder.getMessage());
        }
    }
}
package org;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ReminderScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final OutputProvider outputProvider;

    public ReminderScheduler(OutputProvider outputProvider) {
        this.outputProvider = outputProvider;
    }

    public void schedule(Reminder reminder) {
        long delay = java.time.Duration.between(LocalDateTime.now(), reminder.getTriggerTime()).toMillis();
        System.out.println("⏰ Планирование напоминания: " + reminder.getMessage() +
                " на " + reminder.getTriggerTime() +
                " (задержка: " + delay + "ms)");

        if (delay > 0) {
            scheduler.schedule(() -> {
                sendReminder(reminder);
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            System.out.println("⚠️ Время напоминания уже прошло, отправляю немедленно");
            sendReminder(reminder);
        }
    }

    private void sendReminder(Reminder reminder) {
        try {
            System.out.println("🔔 Отправка напоминания для chatId " + reminder.getChatId() +
                    ": " + reminder.getMessage());

            if (outputProvider instanceof TelegramOutputProvider) {
                ((TelegramOutputProvider) outputProvider).setCurrentChatId(reminder.getChatId());
            }

            outputProvider.output("🔔 Напоминание: " + reminder.getMessage());
            ReminderStorage.markAsCompleted(reminder);

        } catch (Exception e) {
            System.err.println("❌ Ошибка отправки напоминания: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void scheduleAllActiveReminders() {
        List<Reminder> activeReminders = ReminderStorage.getAllActive();
        System.out.println("📋 Начинаю планирование " + activeReminders.size() + " активных напоминаний");

        for (Reminder reminder : activeReminders) {
            schedule(reminder);
        }

        System.out.println("✅ Все активные напоминания запланированы");
    }

    // Метод для корректного завершения работы
    public void shutdown() {
        System.out.println(" Остановка планировщика напоминаний...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            System.out.println("✅ Планировщик напоминаний остановлен");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
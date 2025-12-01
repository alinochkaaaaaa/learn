package org;

import java.time.format.DateTimeFormatter;

public class MenuManager {
    private final OutputProvider outputProvider;
    private final ReminderScheduler reminderScheduler;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public MenuManager(InputProvider inputProvider, OutputProvider outputProvider, ReminderScheduler reminderScheduler) {
        this.outputProvider = outputProvider;
        this.reminderScheduler = reminderScheduler;
    }

    public void showMenu() {
        String menu = "📋 Меню - выберите действие:";
        outputProvider.outputMenu(menu);
    }

    public void handleMenuSelection(String selection, long chatId) {
        String normalized = normalize(selection);
        System.out.println(" Выбор в меню: \"" + selection + "\" -> нормализовано: \"" + normalized + "\"");

        if ("информация".equals(normalized)) {
            outputProvider.output("ℹ️ Информация о боте:");
            outputProvider.output(" Я - Telegram-бот для создания и управления напоминаниями. Вы можете создавать напоминания на определённое время, просматривать свои активные напоминания и получать уведомления в указанное время ");
            outputProvider.output("");
            showMenu();
        } else if ("создать напоминание".equals(normalized)) {
            outputProvider.output("📝 Создание напоминания:");
            outputProvider.output("Введите напоминание в формате: напомни [дата] [время] [сообщение]");
            outputProvider.output("📌 Примеры: \n" +
                    "• напомни через 5 минут выпить воды \n" +
                    "• напомни завтра в 15:00 позвонить маме");
            UserSession.setState(chatId, UserState.CREATING_REMINDER);
        } else if ("мои напоминания".equals(normalized)) {
            showReminders(chatId);
        } else if ("назад".equals(normalized)) {
            outputProvider.output("↩️ Возврат в главное меню...");
            outputProvider.showMainMenu("🏠 Главное меню - выберите действие:");
            UserSession.setState(chatId, UserState.MAIN_MENU);
        } else {
            outputProvider.output("❓ Неизвестный выбор. Используйте кнопки меню.");
            showMenu();
        }
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase()
                .replace("\u0418\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f", "информация")
                .replace("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0435", "создать напоминание")
                .replace("\u041c\u043e\u0438 \u043d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u044f", "мои напоминания")
                .replace("\u041d\u0430\u0437\u0430\u0434", "назад");
    }

    private void showReminders(long chatId) {
        try {
            var reminders = ReminderStorage.getAllByChatId(chatId);
            if (reminders.isEmpty()) {
                outputProvider.output(" У вас нет активных напоминаний.");
                outputProvider.output("Создайте новое напоминание через меню!");
            } else {
                StringBuilder sb = new StringBuilder("📋 Ваши активные напоминания:\n\n");
                for (Reminder r : reminders) {
                    sb.append("⏰ ").append(r.getTriggerTime().format(DATE_FORMATTER))
                            .append("\n💬 \"").append(r.getMessage()).append("\"\n\n");
                }
                outputProvider.output(sb.toString().trim());
                outputProvider.output("");
                outputProvider.output("Всего: " + reminders.size() + " напоминаний");
            }
            showMenu();
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении напоминаний: " + e.getMessage());
            outputProvider.output("⚠️ Произошла ошибка при загрузке напоминаний.");
            showMenu();
        }
    }
}
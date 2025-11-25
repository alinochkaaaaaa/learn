package org;

public class MenuManager {
    private final OutputProvider outputProvider;
    private final ReminderScheduler reminderScheduler;

    public MenuManager(InputProvider inputProvider, OutputProvider outputProvider, ReminderScheduler reminderScheduler) {
        this.outputProvider = outputProvider;
        this.reminderScheduler = reminderScheduler;
    }

    public void showMenu() {
        String menu = "Меню - выберите действие:";
        outputProvider.outputMenu(menu);
    }

    public void handleMenuSelection(String selection, long chatId) {
        String normalized = normalize(selection);

        if ("информация".equals(normalized)) {
            outputProvider.output("Это Telegram-бот для создания и управления напоминаниями. Вы можете создавать напоминания на определённое время и просматривать их.");
            showMenu();
        } else if ("создать напоминание".equals(normalized)) {
            outputProvider.output("Введите напоминание в формате:\nнапомни [дата] [время] [сообщение]\nПример: напомни завтра в 15:00 позвонить маме");
            UserSession.setState(chatId, UserState.CREATING_REMINDER);
        } else if ("мои напоминания".equals(normalized)) {
            showReminders(chatId);
        } else if ("назад".equals(normalized)) {
            outputProvider.output("Возврат в главное меню...");
            outputProvider.showMainMenu("Главное меню - выберите действие:");
            UserSession.setState(chatId, UserState.MAIN_MENU);
        } else {
            outputProvider.output("Неизвестный выбор. Используйте кнопки меню.");
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
        var reminders = ReminderStorage.getAllByChatId(chatId);
        if (reminders.isEmpty()) {
            outputProvider.output("У вас нет активных напоминаний.");
        } else {
            StringBuilder sb = new StringBuilder("Ваши напоминания:\n");
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
            for (Reminder r : reminders) {
                sb.append("- ").append(r.getTriggerTime().format(fmt))
                        .append(": \"").append(r.getMessage()).append("\"\n");
            }
            outputProvider.output(sb.toString().trim());
        }
        showMenu();
    }
}
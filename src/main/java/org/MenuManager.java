package org;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MenuManager {
    private final OutputProvider outputProvider;
    private final ReminderScheduler reminderScheduler;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public MenuManager(InputProvider inputProvider, OutputProvider outputProvider, ReminderScheduler reminderScheduler) {
        this.outputProvider = outputProvider;
        this.reminderScheduler = reminderScheduler;
    }

    public void showMenu() {
        String menu = "\uD83D\uDCCB Меню - выберите действие:";
        outputProvider.outputMenu(menu);
    }

    public void handleMenuSelection(String selection, long chatId) {
        String normalized = normalize(selection);
        System.out.println("Выбор в меню: \"" + selection + "\" -> нормализовано: \"" + normalized + "\"");

        if ("информация".equals(normalized)) {
            outputProvider.output("ℹ\uFE0F Информация о боте:");
            outputProvider.output("Я - Telegram-бот для создания и управления напоминаниями. Вы можете создавать напоминания на определённое время, просматривать свои активные напоминания и получать уведомления в указанное время");
            outputProvider.output("");
            showMenu();
        } else if ("создать напоминание".equals(normalized)) {
            outputProvider.output("\uD83D\uDCDD Создание напоминания:");
            outputProvider.output("Введите напоминание в формате: напомни [дата] [время] [сообщение]");
            outputProvider.output("\uD83D\uDCCC Примеры:\n" +
                    "• напомни через 5 минут выпить воды\n" +
                    "• напомни завтра в 15:00 позвонить маме");
            outputProvider.output("↩\uFE0F Или введите 'назад' для возврата в меню");
            UserSession.setState(chatId, UserState.CREATING_REMINDER);
        } else if ("мои напоминания".equals(normalized)) {
            showRemindersMenu(chatId);
        } else if ("назад".equals(normalized)) {
            outputProvider.output("↩\uFE0F Возврат в главное меню...");
            outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
            UserSession.setState(chatId, UserState.MAIN_MENU);
        } else {
            outputProvider.output("❓ Неизвестный выбор. Используйте кнопки меню.");
            showMenu();
        }
    }

    public void showRemindersMenu(long chatId) {
        try {
            var reminders = ReminderStorage.getAllByChatId(chatId);
            if (reminders.isEmpty()) {
                outputProvider.output("У вас нет активных напоминаний.");
                outputProvider.output("Создайте новое напоминание через меню!");
                showMenu();
            } else {
                StringBuilder sb = new StringBuilder("\uD83D\uDCCB Ваши активные напоминания:\n\n");

                int index = 1;
                for (Reminder r : reminders) {
                    sb.append(index).append("⏰ ").append(r.getTriggerTime().format(DATE_FORMATTER))
                            .append("\n💬 \"").append(r.getMessage()).append("\"\n\n");
                    index++;
                }

                sb.append("Введите номер напоминания для управления им.");
                sb.append("\nИли введите 'назад' для возврата в меню.");

                outputProvider.output(sb.toString().trim());
                UserSession.setRemindersList(chatId, reminders);
                UserSession.setState(chatId, UserState.VIEWING_REMINDERS);
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при получении напоминаний: " + e.getMessage());
            outputProvider.output("⚠\uFE0F Произошла ошибка при загрузке напоминаний.");
            showMenu();
        }
    }

    public void handleReminderSelection(String selection, long chatId) {
        if ("назад".equalsIgnoreCase(selection.trim())) {
            showMenu();
            UserSession.setState(chatId, UserState.IN_MENU);
            return;
        }

        try {
            int reminderIndex = Integer.parseInt(selection) - 1;
            List<Reminder> reminders = UserSession.getRemindersList(chatId);

            if (reminders != null && reminderIndex >= 0 && reminderIndex < reminders.size()) {
                Reminder selectedReminder = reminders.get(reminderIndex);
                UserSession.setSelectedReminder(chatId, selectedReminder);

                outputProvider.output("\uD83D\uDCDD Напоминание:");
                outputProvider.output("Дата и время: " + selectedReminder.getTriggerTime().format(DATE_FORMATTER));
                outputProvider.output("Текст: " + selectedReminder.getMessage());
                outputProvider.output("\nВыберите действие:");
                outputProvider.output("1. Редактировать\n" +
                        "2. Удалить\n" +
                        "3. Назад к списку\n");

                UserSession.setState(chatId, UserState.EDITING_REMINDER);
            } else {
                outputProvider.output("❓ Неверный номер напоминания.");
                showRemindersMenu(chatId);
            }
        } catch (NumberFormatException e) {
            outputProvider.output("Пожалуйста, введите номер напоминания или 'назад'.");
            showRemindersMenu(chatId);
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
}
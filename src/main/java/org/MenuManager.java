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
        String menu = "\uD83D\uDCCB <b>Меню - выберите действие</b>:";
        outputProvider.outputMenu(menu);
    }

    public void handleMenuSelection(String selection, long chatId) {
        String normalized = normalize(selection);

        UserSession session = UserSession.getSession(chatId);

        // Если пользователь в состоянии CREATING_REMINDER и ввел "назад"
        if (session.getState() == UserState.CREATING_REMINDER && "назад".equals(normalized)) {
            showMenu();
            session.setState(UserState.IN_MENU);
            return;
        }
        if ("информация".equals(normalized)) {
            outputProvider.output("ℹ\uFE0F Информация о боте:");
            outputProvider.output("Я - Telegram-бот для создания и управления напоминаниями. " +
                    "Вы можете создавать <b>напоминания</b> на определённое время и просматривать их," +
                    "<b>управлять днями рождения</b> контактов и получать уведомления.");
            outputProvider.output("\n📱 <b>Особенности</b>:\n" +
                    "• Пересылка контактов из телефонной книги или ручное добавление\n" +
                    "• Уведомления о днях рождения");
            outputProvider.output("");
            showMenu();
        } else if ("создать напоминание".equals(normalized)) {
            outputProvider.output("\uD83D\uDCDD <b>Создание напоминания:</b>");
            outputProvider.output("Введите напоминание в формате: <b>напомни [дата] [время] [сообщение]</b>");
            outputProvider.output("\uD83D\uDCCC <b>Примеры:</b>\n" +
                    "• напомни через 5 минут/завтра в 15:00/25.12.2025 в 10:00 выпить воды\n" +
                    "↩\uFE0F Или выберите 'назад' для возврата в меню");
            session.setState(UserState.CREATING_REMINDER);
        } else if ("мои напоминания".equals(normalized)) {
            showRemindersMenu(chatId);
        } else if ("дни рождения".equals(normalized)) {
            CommandProcessor processor = new CommandProcessor(null, outputProvider, this, reminderScheduler, null);
            processor.showBirthdayMenu(chatId);
        } else if ("назад".equals(normalized)) {
            outputProvider.showMainMenu("\uD83C\uDFE0 <b>Главное меню </b> - выберите действие:");
            session.setState(UserState.MAIN_MENU);
        } else {
            outputProvider.output("❓ Неизвестный выбор. Используйте кнопки меню.");
            showMenu();
        }
    }

    private void showRemindersMenu(long chatId) {
        UserSession session = UserSession.getSession(chatId);

        // Получаем все активные напоминания пользователя
        List<Reminder> reminders = ReminderStorage.getAllByChatId(chatId);

        if (reminders.isEmpty()) {
            outputProvider.output("📭 У вас пока нет активных напоминаний.");
            outputProvider.output("\nЧтобы создать напоминание используйте <b>'Создать напоминание'</b> или напишите: напомни [дата] [время] [сообщение]");
            outputProvider.output("");
            showMenu();
            return;
        }

        // Сохраняем список напоминаний в сессии
        session.setRemindersList(reminders);

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83D\uDCCB <b>Ваши активные напоминания:</b>\n\n");


        for (int i = 0; i < reminders.size(); i++) {
            Reminder reminder = reminders.get(i);
            sb.append(i + 1).append(". ");
            sb.append("<b>").append(reminder.getMessage()).append("</b>\n");
            sb.append("   ⏰ <b>Когда:</b> ").append(reminder.getTriggerTime().format(DATE_FORMATTER)).append("\n\n");
        }

        sb.append("<b>Выберите напоминание для управления</b> - введите номер или введите <b>'назад'</b> для возврата в меню");

        outputProvider.output(sb.toString());
        session.setState(UserState.VIEWING_REMINDERS);
    }

    public void handleReminderSelection(String input, long chatId) {
        UserSession session = UserSession.getSession(chatId);
        List<Reminder> reminders = session.getRemindersList();

        if (reminders == null || reminders.isEmpty()) {
            outputProvider.output("❌ Список напоминаний пуст или не загружен.");
            showMenu();
            session.setState(UserState.IN_MENU);
            return;
        }

        if ("назад".equalsIgnoreCase(input.trim())) {
            showMenu();
            session.setState(UserState.IN_MENU);
            return;
        }

        try {
            int index = Integer.parseInt(input.trim()) - 1;

            if (index >= 0 && index < reminders.size()) {
                Reminder selectedReminder = reminders.get(index);
                session.setSelectedReminder(selectedReminder);

                showReminderDetails(selectedReminder, chatId);
            } else {
                outputProvider.output("❌ Неверный номер.");
                showRemindersMenu(chatId);
            }
        } catch (NumberFormatException e) {
            outputProvider.output("❌ Пожалуйста, введите номер напоминания");
            showRemindersMenu(chatId);
        }
    }

    private void showReminderDetails(Reminder reminder, long chatId) {
        UserSession session = UserSession.getSession(chatId);

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83D\uDCCB <b>Детали напоминания:</b>\n\n");
        sb.append("📝 <b>Сообщение:</b> ").append(reminder.getMessage()).append("\n");
        sb.append("⏰ <b>Время:</b> ").append(reminder.getTriggerTime().format(DATE_FORMATTER)).append("\n");
        sb.append("\n<b>Выберите действие:</b>");
        sb.append("\n1. ✏️ Редактировать");
        sb.append("\n2. 🗑️ Удалить");
        sb.append("\n3. ↩️ Назад к списку");

        outputProvider.output(sb.toString());
        session.setState(UserState.EDITING_REMINDER);
    }

    public void handleReminderAction(String input, long chatId) {
        UserSession session = UserSession.getSession(chatId);
        Reminder reminder = session.getSelectedReminder();

        if (reminder == null) {
            outputProvider.output("❌ Напоминание не выбрано.");
            showMenu();
            session.setState(UserState.IN_MENU);
            return;
        }

        String normalized = input.trim().toLowerCase();

        if ("1".equals(normalized) || "редактировать".equals(normalized) || "✏️".equals(normalized)) {
            outputProvider.output("✏️ <b>Редактирование напоминания:</b>");
            outputProvider.output("Текущий текст: " + reminder.getMessage());
            outputProvider.output("Текущее время: " + reminder.getTriggerTime().format(DATE_FORMATTER));
            outputProvider.output("\nВведите новое напоминание в формате: <b>напомни [дата] [время] [новое сообщение]</b>");
            outputProvider.output("\nИли введите 'назад' для отмены");
            session.setState(UserState.CREATING_REMINDER);
        } else if ("2".equals(normalized) || "удалить".equals(normalized) || "🗑️".equals(normalized)) {
            outputProvider.output("🗑️ <b>Удаление напоминания:</b>");
            outputProvider.output("Вы действительно хотите удалить напоминание?");
            outputProvider.output("<b>" + reminder.getMessage() + "</b>");
            outputProvider.output("\nВведите <b>'да'</b> или <b>'нет'</b> для отмены");
            session.setState(UserState.DELETING_REMINDER);
        } else if ("3".equals(normalized) || "назад".equals(normalized) || "↩️".equals(normalized)) {
            showRemindersMenu(chatId);
        } else {
            outputProvider.output("❌ Неизвестное действие. Выберите 1, 2 или 3.");
            showReminderDetails(reminder, chatId);
        }
    }

    public void handleDeleteConfirmation(String input, long chatId) {
        UserSession session = UserSession.getSession(chatId);
        Reminder reminder = session.getSelectedReminder();

        if (reminder == null) {
            outputProvider.output("❌ Напоминание не выбрано.");
            showMenu();
            session.setState(UserState.IN_MENU);
            return;
        }

        String normalized = input.trim().toLowerCase();

        if ("да".equals(normalized)) {
            try {
                if (reminder.getId() != null) {
                    ReminderStorage.delete(reminder.getId(), chatId);
                }

                outputProvider.output("✅ Напоминание успешно удалено!");
                session.clearSelectedReminder();
                showMenu();
                session.setState(UserState.IN_MENU);
            } catch (Exception e) {
                outputProvider.output("❌ Ошибка при удалении напоминания: " + e.getMessage());
                showReminderDetails(reminder, chatId);
            }
        } else if ("нет".equals(normalized)) {
            outputProvider.output("✅ Удаление отменено.");
            showReminderDetails(reminder, chatId);
        } else {
            outputProvider.output("Вы действительно хотите удалить напоминание? Пожалуйста, введите 'да' или 'нет'.");
            outputProvider.output("<b>" + reminder.getMessage() + "</b>");
        }
    }

    private String normalize(String input) {
        if (input == null) return "";
        return input.trim().toLowerCase()
                .replace("\u0418\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f", "информация")
                .replace("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0435", "создать напоминание")
                .replace("\u041c\u043e\u0438 \u043d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u044f", "мои напоминания")
                .replace("\u0414\u043d\u0438 \u0440\u043e\u0436\u0434\u0435\u043d\u0438\u044f", "дни рождения")
                .replace("\u041d\u0430\u0437\u0430\u0434", "назад");
    }
}
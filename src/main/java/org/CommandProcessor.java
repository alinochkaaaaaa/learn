package org;

import java.time.format.DateTimeFormatter;

public class CommandProcessor {
    private final OutputProvider outputProvider;
    private final MenuManager menuManager;
    private final ReminderScheduler reminderScheduler;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private boolean isRunning = true;

    public CommandProcessor(
            InputProvider inputProvider,
            OutputProvider outputProvider,
            MenuManager menuManager,
            ReminderScheduler reminderScheduler) {
        this.outputProvider = outputProvider;
        this.menuManager = menuManager;
        this.reminderScheduler = reminderScheduler;
    }

    public void processCommand(String command, long chatId) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        UserState state = UserSession.getState(chatId);
        String cmd = command.trim();

        System.out.println("Обработка команды: \"" + cmd + "\" от chatId " + chatId +
                ", состояние: " + state);

        switch (state) {
            case MAIN_MENU:
                handleMainMenu(cmd, chatId);
                break;
            case IN_MENU:
                menuManager.handleMenuSelection(cmd, chatId);
                break;
            case CREATING_REMINDER:
                handleCreateReminder(cmd, chatId);
                break;
            case VIEWING_REMINDERS:
                menuManager.handleReminderSelection(cmd, chatId);
                break;
            case EDITING_REMINDER:
                handleEditingReminderAction(cmd, chatId);
                break;
            case DELETING_REMINDER:
                handleDeleteReminder(cmd, chatId);
                break;
        }
    }

    private String normalize(String input) {
        if (input == null) return "";
        String normalized = input.trim().toLowerCase();

        normalized = normalized
                .replace("\u041c\u0435\u043d\u044e", "меню")
                .replace("\u0421\u0442\u0430\u0440\u0442", "старт")
                .replace("\u041f\u043e\u043c\u043e\u0449\u044c", "помощь")
                .replace("\u0412\u044b\u0445\u043e\u0434", "выход")
                .replace("\u0418\u043d\u0444\u043e\u0440\u043c\u0430\u0446\u0438\u044f", "информация")
                .replace("\u0421\u043e\u0437\u0434\u0430\u0442\u044c \u043d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u0435", "создать напоминание")
                .replace("\u041c\u043e\u0438 \u043d\u0430\u043f\u043e\u043c\u0438\u043d\u0430\u043d\u0438\u044f", "мои напоминания")
                .replace("\u041d\u0430\u0437\u0430\u0434", "назад");

        return normalized;
    }

    private void handleMainMenu(String command, long chatId) {
        String normalized = normalize(command);

        if ("старт".equals(normalized) || "/start".equalsIgnoreCase(command)) {
            outputProvider.output(" Добро пожаловать! Я ваш бот.");
            outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
            UserSession.setState(chatId, UserState.MAIN_MENU);
        } else if ("меню".equals(normalized)) {
            menuManager.showMenu();
            UserSession.setState(chatId, UserState.IN_MENU);
        } else if ("помощь".equals(normalized) || "/help".equalsIgnoreCase(command)) {
            outputProvider.output("\uD83D\uDCDA Справка по боту:");
            outputProvider.output("Используйте кнопки меню для навигации. Для создания напоминания используйте формат:");
            outputProvider.output("  напомни [дата] [время] [сообщение]");
            outputProvider.output("\uD83D\uDCCC Примеры:\n" +
                    "• напомни через 5 минут выпить воды\n" +
                    "• напомни завтра в 15:00 позвонить маме");
            outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
        } else if ("выход".equals(normalized) || "/exit".equalsIgnoreCase(command)) {
            outputProvider.output("\uD83D\uDC4B Завершение работы бота...");
            isRunning = false;
        } else {
            outputProvider.output("❌ Неизвестная команда. Используйте кнопки меню.");
            outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
        }
    }

    private void handleCreateReminder(String command, long chatId) {
        try {
            if ("назад".equalsIgnoreCase(command.trim()) || "отмена".equalsIgnoreCase(command.trim())) {
                outputProvider.output("❌ Отмена создания напоминания");
                menuManager.showMenu();
                UserSession.setState(chatId, UserState.IN_MENU);
                return;
            }

            ReminderParser.ParseResult result = ReminderParser.parse(command);

            if (result != null && result.getTriggerTime() != null && !result.getText().isEmpty()) {
                Reminder reminder = new Reminder(chatId, result.getText(), result.getTriggerTime());

                try {
                    ReminderStorage.add(reminder);
                    reminderScheduler.schedule(reminder);

                    String formattedTime = result.getTriggerTime().format(DATE_TIME_FORMATTER);
                    outputProvider.output("✅ Напоминание успешно установлено!");
                    outputProvider.output("Дата и время: " + formattedTime);
                    outputProvider.output("Текст: \"" + result.getText() + "\"");
                    outputProvider.output("⏰ Я напомню вам в указанное время.");

                    menuManager.showMenu();
                    UserSession.setState(chatId, UserState.IN_MENU);

                } catch (Exception e) {
                    System.err.println("❌ Ошибка при сохранении напоминания: " + e.getMessage());
                    e.printStackTrace();
                    outputProvider.output("❌ Ошибка при сохранении напоминания: " + e.getMessage());
                    outputProvider.output("Попробуйте снова или обратитесь к администратору.");
                }
            } else {
                outputProvider.output("❌ Не удалось распознать напоминание.");
                outputProvider.output("Формат: напомни [дата] [время] [сообщение]");
                outputProvider.output("\uD83D\uDCCC Примеры:\n" +
                        "• напомни через 5 минут выпить воды\n" +
                        "• напомни завтра в 15:00 позвонить маме");
                outputProvider.output("Или введите 'назад' для возврата в меню");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при обработке напоминания: " + e.getMessage());
            e.printStackTrace();
            outputProvider.output("❌ Произошла ошибка. Попробуйте снова.");
        }
    }

    private void handleEditingReminderAction(String command, long chatId) {
        String cmd = command.trim().toLowerCase();

        if ("1".equals(command.trim()) || "редактировать".equals(cmd)) {
            outputProvider.output("Введите новое напоминание в формате: напомни [дата] [время] [сообщение]");
            outputProvider.output("Или введите 'отмена' ");
            UserSession.setState(chatId, UserState.EDITING_REMINDER);
        }
        else if ("2".equals(command.trim()) || "удалить".equals(cmd)) {
            Reminder selectedReminder = UserSession.getSelectedReminder(chatId);
            if (selectedReminder != null) {
                outputProvider.output("Подтвердите удаление напоминания:");
                outputProvider.output("Текст: \"" + selectedReminder.getMessage() + "\"");
                outputProvider.output("Введите 'да' для удаления или 'нет' для отмены");
                UserSession.setState(chatId, UserState.DELETING_REMINDER);
            } else {
                outputProvider.output("❌ Напоминание не найдено.");
                menuManager.showMenu();
                UserSession.setState(chatId, UserState.IN_MENU);
            }
        }
        else if ("3".equals(command.trim()) || "назад к списку".equals(cmd) || "назад".equals(cmd)) {
            menuManager.showRemindersMenu(chatId);
            UserSession.setState(chatId, UserState.VIEWING_REMINDERS);
        }
        else if ("отмена".equals(cmd)) {
            outputProvider.output("❌ Отмена действия.");
            menuManager.showRemindersMenu(chatId);
            UserSession.setState(chatId, UserState.VIEWING_REMINDERS);
        }
        else {
            handleEditingReminderText(command, chatId);
        }
    }

    private void handleEditingReminderText(String command, long chatId) {
        if ("отмена".equalsIgnoreCase(command.trim())) {
            outputProvider.output("❌ Отмена редактирования.");
            menuManager.showRemindersMenu(chatId);
            UserSession.setState(chatId, UserState.VIEWING_REMINDERS);
            return;
        }

        try {
            Reminder selectedReminder = UserSession.getSelectedReminder(chatId);
            if (selectedReminder == null) {
                outputProvider.output("❌ Напоминание не найдено.");
                menuManager.showMenu();
                UserSession.setState(chatId, UserState.IN_MENU);
                return;
            }

            ReminderParser.ParseResult result = ReminderParser.parse(command);

            if (result != null && result.getTriggerTime() != null && !result.getText().isEmpty()) {
                Reminder updatedReminder = new Reminder(chatId, result.getText(), result.getTriggerTime());
                updatedReminder.setId(selectedReminder.getId());

                ReminderStorage.update(selectedReminder.getId(), chatId, updatedReminder);

                String formattedTime = result.getTriggerTime().format(DATE_TIME_FORMATTER);
                outputProvider.output("✅ Напоминание успешно обновлено!");
                outputProvider.output("Новая дата и время: " + formattedTime);
                outputProvider.output("Новый текст: \"" + result.getText() + "\"");

                menuManager.showMenu();
                UserSession.setState(chatId, UserState.IN_MENU);
            } else {
                outputProvider.output("❌ Не удалось распознать новое напоминание.");
                outputProvider.output("Формат: напомни [дата] [время] [сообщение]");
                outputProvider.output("Или введите 'отмена' ");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при обновлении напоминания: " + e.getMessage());
            outputProvider.output("❌ Ошибка при обновлении напоминания: " + e.getMessage());
        }
    }

    private void handleDeleteReminder(String command, long chatId) {
        Reminder selectedReminder = UserSession.getSelectedReminder(chatId);
        if (selectedReminder == null) {
            outputProvider.output("❌ Напоминание не найдено.");
            menuManager.showMenu();
            UserSession.setState(chatId, UserState.IN_MENU);
            return;
        }

        String cmd = command.trim().toLowerCase();

        if ("да".equals(cmd) || "подтвердить".equals(cmd) || "yes".equals(cmd)) {
            try {
                ReminderStorage.delete(selectedReminder.getId(), chatId);
                outputProvider.output("✅ Напоминание успешно удалено.");
                menuManager.showMenu();
                UserSession.setState(chatId, UserState.IN_MENU);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении: " + e.getMessage());
                outputProvider.output("❌ Ошибка при удалении напоминания: " + e.getMessage());
            }
        } else if ("нет".equals(cmd) || "отмена".equals(cmd) || "no".equals(cmd)) {
            outputProvider.output("Удаление отменено.");
            menuManager.showRemindersMenu(chatId);
            UserSession.setState(chatId, UserState.VIEWING_REMINDERS);
        } else {
            outputProvider.output("Пожалуйста, подтвердите удаление. Введите 'да' или 'нет'.");
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
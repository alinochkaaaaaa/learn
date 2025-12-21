package org;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CommandProcessor {
    private final OutputProvider outputProvider;
    private final MenuManager menuManager;
    private final ReminderScheduler reminderScheduler;
    private final BirthdayManager birthdayManager;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private boolean isRunning = true;

    private String pendingBirthdayCommand = null;
    private String pendingBirthdayData = null;

    public CommandProcessor(
            InputProvider inputProvider,
            OutputProvider outputProvider,
            MenuManager menuManager,
            ReminderScheduler reminderScheduler,
            BirthdayManager birthdayManager) {
        this.outputProvider = outputProvider;
        this.menuManager = menuManager;
        this.reminderScheduler = reminderScheduler;
        this.birthdayManager = birthdayManager;
    }

    public void processCommand(String command, long chatId) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        UserSession session = UserSession.getSession(chatId);
        String cmd = command.trim();

        // Обработка команды "назад" для всех состояний
        if (cmd.equalsIgnoreCase("назад") || cmd.equals("↩ Назад в меню") || cmd.equals("↩ Назад")) {
            handleBackCommand(chatId, session.getState());
            return;
        }

        // Проверяем, не ожидаем ли мы команду дня рождения после нажатия кнопки
        if (pendingBirthdayCommand != null && !cmd.startsWith("/") && !cmd.startsWith("напомни")) {
            if (pendingBirthdayCommand.equals("add")) {
                handlePendingAddBirthday(cmd, chatId);
                return;
            } else if (pendingBirthdayCommand.equals("find")) {
                // Поиск контакта
                findBirthdayContact(cmd, chatId);
                pendingBirthdayCommand = null;
                return;
            } else if (pendingBirthdayCommand.equals("delete")) {
                // Удаление контакта - принимаем полное имя
                deleteBirthdayContact(cmd, chatId);
                pendingBirthdayCommand = null;
                return;
            }
        }

        if (cmd.equalsIgnoreCase("старт") || cmd.equalsIgnoreCase("/start")) {
            handleStartCommand(chatId);
            return;
        }

        if (cmd.equalsIgnoreCase("меню") || cmd.equalsIgnoreCase("/menu")) {
            menuManager.showMenu();
            session.setState(UserState.IN_MENU);
            pendingBirthdayCommand = null;
            return;
        }

        if (cmd.equalsIgnoreCase("помощь") || cmd.equalsIgnoreCase("/help")) {
            showHelp(chatId);
            pendingBirthdayCommand = null;
            return;
        }

        if (cmd.equalsIgnoreCase("выход") || cmd.equalsIgnoreCase("/exit")) {
            handleExitCommand(chatId);
            pendingBirthdayCommand = null;
            return;
        }

        // Обработка кнопок из меню дней рождения
        if (cmd.equals("📋 Мои контакты") || cmd.equals("➕ Добавить контакт") ||
                cmd.equals("⚙️ Настройки") || cmd.equals("↩ Назад в меню") ||
                cmd.equals("➕ Добавить вручную") || cmd.equals("🎂 Добавить дату") ||
                cmd.equals("🔍 Найти контакт") || cmd.equals("🗑️ Удалить контакт") ||
                cmd.equals("📋 Вернуться к контактам") || cmd.equals("🎂 Добавить дату сейчас") ||
                cmd.equals("↩ Назад к контактам")) {
            handleBirthdayMenuButton(cmd, chatId);
            return;
        }

        switch (session.getState()) {
            case ADDING_BIRTHDAY_NAME:
                handleContactNameInput(cmd, chatId);
                return;
            case ADDING_BIRTHDAY_PHONE:
                handleContactPhoneInput(cmd, chatId);
                return;
            case ADDING_BIRTHDAY_DATE:
                handleContactDateInput(cmd, chatId);
                return;
            case DELETING_BIRTHDAY:
                handleDeleteBirthdayConfirmation(cmd, chatId);
                return;
        }

        if (cmd.startsWith("др ") || cmd.startsWith("/birthday")) {
            handleBirthdayCommand(cmd, chatId);
            return;
        }

        String normalized = normalize(cmd);

        switch (session.getState()) {
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
                menuManager.handleReminderAction(cmd, chatId);
                break;
            case DELETING_REMINDER:
                menuManager.handleDeleteConfirmation(cmd, chatId);
                break;
            case MANAGING_BIRTHDAYS:
                handleBirthdayManagement(cmd, chatId);
                break;
            case ADDING_BIRTHDAY_CONTACT_MANUALLY:
                handleManualContactStart(cmd, chatId);
                break;
            case ADDING_BIRTHDAY_NAME:
                handleContactNameInput(cmd, chatId);
                return;
            case ADDING_BIRTHDAY_PHONE:
                handleContactPhoneInput(cmd, chatId);
                return;
            case ADDING_BIRTHDAY_DATE:
                handleContactDateInput(cmd, chatId);
                return;
            case DELETING_BIRTHDAY:
                handleDeleteBirthdayConfirmation(cmd, chatId);
                return;
        }

        // Сбрасываем ожидание команды, если пользователь ввел что-то другое
        if (!cmd.equalsIgnoreCase("пропустить")) {
            pendingBirthdayCommand = null;
            pendingBirthdayData = null;
        }

    }

    private void handleStartCommand(long chatId) {
        outputProvider.output("Добро пожаловать! Я ваш <b>бот.</b>\n" +
                "Я помогу вам не забывать о важных событиях и днях рождения!");
        outputProvider.output("\n📱 <b>Вы можете</b>:\n" +
                "• Создавать напоминания\n" +
                "• Пересылать контакты из телефонной книги\n" +
                "• Добавлять дни рождения");
        outputProvider.showMainMenu("\uD83C\uDFE0 <b>Главное меню</b> - выберите действие:");
        UserSession.getSession(chatId).setState(UserState.MAIN_MENU);
        clearPendingCommands();
    }

    private void handleBirthdayMenuButton(String button, long chatId) {
        UserSession session = UserSession.getSession(chatId);

        System.out.println("Обработка кнопки дней рождения: " + button);
        clearPendingCommands();

        switch (button) {
            case "📋 Мои контакты":
                showAllBirthdays(chatId);
                session.setState(UserState.MANAGING_BIRTHDAYS);
                break;
            case "➕ Добавить контакт":
                startAddContact(chatId);
                session.setState(UserState.ADDING_BIRTHDAY_CONTACT_MANUALLY);
                break;
            case "➕ Добавить вручную":
                startManualContactInput(chatId);
                break;
            case "⚙️ Настройки":
                showNotificationSettings(chatId);
                session.setState(UserState.MANAGING_BIRTHDAYS);
                break;
            case "↩ Назад в меню":
                outputProvider.showMainMenu("\uD83C\uDFE0 Возвращаемся в <b>главное меню</b>:");
                session.setState(UserState.MAIN_MENU);
                break;
            case "🎂 Добавить дату":
                outputProvider.output("✏️ <b>Добавление даты рождения</b>\n\n");
                outputProvider.output("Введите имя контакта и дату рождения в формате: <b>[Имя Фамилия] [ДД.ММ.ГГГГ]</b> или введите <b>'назад'</b> для возврата");
                pendingBirthdayCommand = "add";
                break;
            case "🔍 Найти контакт":
                outputProvider.output("🔍 <b>Поиск контакта</b>\n" +
                        "Введите полное имя контакта для поиска или введите <b>'назад'</b> для возврата");
                pendingBirthdayCommand = "find";
                break;
            case "🗑️ Удалить контакт":
                outputProvider.output("🗑️ <b>Удаление контакта</b>\n" +
                        "Введите полное имя контакта для удаления или введите <b>'назад'</b> для возврата");
                pendingBirthdayCommand = "delete";
                break;
            case "📋 Вернуться к контактам":
                showAllBirthdays(chatId);
                session.setState(UserState.MANAGING_BIRTHDAYS);
                break;
            case "🎂 Добавить дату сейчас":
                outputProvider.output("✏️ <b>Добавление даты рождения</b>\n\n");
                outputProvider.output("Введите имя контакта и дату рождения в формате:");
                outputProvider.output("<b>[Имя Фамилия] [ДД.ММ.ГГГГ]</b>\n");
                outputProvider.output("\n<b>Пример:</b>\n" +
                        "Иван Иванов 15.05.1990\n" +
                        "Или введите <b>'назад'</b> для возврата");
                pendingBirthdayCommand = "add";
                break;
            case "↩ Назад к контактам":
                showAllBirthdays(chatId);
                session.setState(UserState.MANAGING_BIRTHDAYS);
                break;
        }
    }

    private void handlePendingAddBirthday(String input, long chatId) {
        // Пытаемся извлечь дату из конца строки
        String[] parts = input.split("\\s+");

        if (parts.length < 2) {
            outputProvider.output("❌ <b>Неверный формат!</b>\n");
            outputProvider.output("Введите имя и дату рождения: [Имя Фамилия] [ДД.ММ.ГГГГ]");
            return;
        }

        String lastPart = parts[parts.length - 1];
        String dateStr = null;
        String name = "";

        // Проверяем, является ли последняя часть датой
        if (lastPart.matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
            dateStr = lastPart;
            // Имя - все части кроме последней
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) name += " ";
                name += parts[i];
            }
        } else {
            // Если нет даты в конце, просим ввести дату
            pendingBirthdayData = input; // Сохраняем имя
            outputProvider.output("✅ <b>Имя сохранено:</b> " + input + "\n\n");
            outputProvider.output("Теперь введите дату рождения в формате <b>ДД.ММ.ГГГГ</b>:");
            return;
        }

        if (dateStr != null && !name.isEmpty()) {
            handleAddBirthday(name, dateStr, chatId);
            pendingBirthdayCommand = null;
            pendingBirthdayData = null;
        }
    }

    private void handleBackCommand(long chatId, UserState state) {
        UserSession session = UserSession.getSession(chatId);

        switch (state) {
            case ADDING_BIRTHDAY_NAME:
            case ADDING_BIRTHDAY_PHONE:
            case ADDING_BIRTHDAY_DATE:
                showBirthdayMenu(chatId);
                break;
            case ADDING_BIRTHDAY_CONTACT_MANUALLY:
                showBirthdayMenu(chatId);
                break;
            case MANAGING_BIRTHDAYS:
            case DELETING_BIRTHDAY:
                outputProvider.showMainMenu("\uD83C\uDFE0 Возвращаемся в главное меню:");
                session.setState(UserState.MAIN_MENU);
                break;
            default:
                outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
                session.setState(UserState.MAIN_MENU);
                break;
        }
        clearPendingCommands();
    }

    private void handleExitCommand(long chatId) {
        outputProvider.output("\uD83D\uDC4B Завершение работы...");
        outputProvider.output("Спасибо за использование бота! До встречи!");
        UserSession.clearSession(chatId);
        isRunning = false;
        clearPendingCommands();
    }

    private void handleMainMenu(String command, long chatId) {
        String normalized = normalize(command);

        if ("старт".equals(normalized) || "/start".equalsIgnoreCase(command)) {
            handleStartCommand(chatId);
        } else if ("меню".equals(normalized) || "/menu".equalsIgnoreCase(command)) {
            menuManager.showMenu();
            UserSession.getSession(chatId).setState(UserState.IN_MENU);
        } else if ("помощь".equals(normalized) || "/help".equalsIgnoreCase(command)) {
            showHelp(chatId);
        } else if ("дни рождения".equals(normalized) || "/birthdays".equalsIgnoreCase(command)) {
            showBirthdayMenu(chatId);
        } else if ("выход".equals(normalized) || "/exit".equalsIgnoreCase(command)) {
            handleExitCommand(chatId);
        } else {
            outputProvider.output("Неизвестная команда. Используйте кнопки меню.");
            outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
        }
        clearPendingCommands();
    }

    private void showHelp(long chatId) {
        String helpText = "\uD83D\uDCDA <b>Справка по боту:</b>\n\n";
        helpText += "<b>Основные команды:</b>\n";
        helpText += "• <b>Старт</b> - начать работу\n";
        helpText += "• <b>Меню</b> - основное меню\n";
        helpText += "• <b>Помощь</b> - показать справку\n";
        helpText += "• <b>Дни рождения</b> - управление днями рождения\n";
        helpText += "• <b>Выход</b> - завершить работу\n\n";

        helpText += "<b>Создание напоминания:</b>\n";
        helpText += "Формат: <b>напомни [дата] [время] [сообщение]</b>\n";
        helpText += "Примеры:\n";
        helpText += "•  напомни через 5 минут выпить/завтра в 15:00/25.12.2025 в 10:00 воды\n\n";

        helpText += "📱 <b>Вы можете:</b>\n";
        helpText += "1. Переслать контакт из телефонной книги\n";
        helpText += "2. Добавить вручную через меню <b>'Дни рождения'</b>\n\n";

        outputProvider.output(helpText);

        UserSession.getSession(chatId).setState(UserState.MAIN_MENU);
        clearPendingCommands();
    }

    public void showBirthdayMenu(long chatId) {
        String menuText = "\uD83C\uDF89 <b>Управление днями рождения</b>\n\n";
        menuText += "📱 <b>Вы можете:</b>\n";
        menuText += "1. Переслать контакт из телефонной книги\n";
        menuText += "2. Добавить контакт вручную\n";
        menuText += "3. Просмотреть все контакты\n\n";
        menuText += "<b>Выберите действие:</b>";

        String keyboard = "{\"keyboard\":[[\"📋 Мои контакты\",\"➕ Добавить контакт\"],[\"⚙️ Настройки\",\"↩ Назад в меню\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";

        sendTelegramMessageWithKeyboard(chatId, menuText, keyboard);
        UserSession.getSession(chatId).setState(UserState.MANAGING_BIRTHDAYS);
        clearPendingCommands();
    }

    private void handleBirthdayManagement(String command, long chatId) {
        System.out.println("Обработка текстовой команды в меню дней рождения: " + command);

        if ("↩ Назад в меню".equals(command) || "назад".equalsIgnoreCase(command) || "меню".equalsIgnoreCase(command)) {
            outputProvider.showMainMenu("\uD83C\uDFE0 Возвращаемся в главное меню:");
            UserSession.getSession(chatId).setState(UserState.MAIN_MENU);
            clearPendingCommands();
            return;
        }

        outputProvider.output("❌ <b>Пожалуйста, используйте кнопки для выбора действия!</b>");
        showBirthdayMenu(chatId);
    }

    private void startAddContact(long chatId) {
        String instruction = "\uD83D\uDCDD <b>Добавление нового контакта</b>\n\n";
        instruction += "📱 <b>Способы добавления:</b>\n";
        instruction += "1. Перешлите контакт из телефонной книги\n";
        instruction += "2. Введите данные вручную\n\n";

        String keyboard = "{\"keyboard\":[[\"➕ Добавить вручную\",\"↩ Назад в меню\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";

        sendTelegramMessageWithKeyboard(chatId, instruction, keyboard);
        UserSession.getSession(chatId).setState(UserState.ADDING_BIRTHDAY_CONTACT_MANUALLY);
        clearPendingCommands();
    }

    private void handleManualContactStart(String command, long chatId) {
        if (command.equalsIgnoreCase("➕ Добавить вручную") || command.equalsIgnoreCase("добавить")) {
            startManualContactInput(chatId);
        } else if (command.equalsIgnoreCase("↩ Назад в меню") || command.equalsIgnoreCase("назад")) {
            showBirthdayMenu(chatId);
        } else {
            outputProvider.output("Для начала ручного ввода нажмите: <b>➕ Добавить вручную</b>");
            outputProvider.output("Или нажмите '↩ Назад в меню' для возврата");
        }
        clearPendingCommands();
    }

    private void startManualContactInput(long chatId) {
        outputProvider.output("✍️ <b>Ручной ввод контакта</b>\n\n");
        outputProvider.output("Введите имя контакта:");
        outputProvider.output("\nИли введите 'назад' для возврата");
        UserSession.getSession(chatId).setState(UserState.ADDING_BIRTHDAY_NAME);
        clearPendingCommands();
    }

    private void handleContactNameInput(String name, long chatId) {
        if (name.trim().isEmpty()) {
            outputProvider.output("❌ Имя не может быть пустым. Пожалуйста, введите имя:");
            return;
        }

        if (name.equalsIgnoreCase("назад")) {
            startAddContact(chatId);
            return;
        }

        UserSession session = UserSession.getSession(chatId);

        // Создаем временный контакт
        BirthdayContact tempContact = new BirthdayContact(chatId, System.currentTimeMillis(), name.trim());
        session.setSelectedBirthdayContact(tempContact);

        outputProvider.output("✅ Имя сохранено: <b>" + name + "</b>\n\n");
        outputProvider.output("Введите номер телефона или нажмите кнопку <b>Пропустить телефон</b>");

        String keyboard = "{\"keyboard\":[[\"📱 Пропустить телефон\",\"↩ Назад\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessageWithKeyboard(chatId, "Выберите действие:", keyboard);

        session.setState(UserState.ADDING_BIRTHDAY_PHONE);
        clearPendingCommands();
    }

    private void handleContactPhoneInput(String phone, long chatId) {
        UserSession session = UserSession.getSession(chatId);
        BirthdayContact contact = session.getSelectedBirthdayContact();

        if (contact == null) {
            outputProvider.output("❌ Ошибка: контакт не найден. Начните заново.");
            showBirthdayMenu(chatId);
            return;
        }

        if (phone.equalsIgnoreCase("назад")) {
            startManualContactInput(chatId);
            return;
        }

        if (phone.equalsIgnoreCase("📱 Пропустить телефон") || phone.equalsIgnoreCase("пропустить")) {
            outputProvider.output("✅ Телефон пропущен\n\n");
        } else {
            String cleanedPhone = phone.replaceAll("[^\\d+]", "");
            if (!cleanedPhone.isEmpty()) {
                contact.setCustomMessage("Телефон: " + cleanedPhone);
                outputProvider.output("✅ Телефон сохранен: " + cleanedPhone + "\n\n");
            } else {
                outputProvider.output("✅ Телефон не указан\n\n");
            }
        }

        outputProvider.output("Введите дату рождения в формате <b>ДД.ММ.ГГГГ</b> или нажмите кнопку <b>Пропустить дату</b>:");

        String keyboard = "{\"keyboard\":[[\"🎂 Пропустить дату\",\"↩ Назад\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessageWithKeyboard(chatId, "Выберите действие:", keyboard);

        session.setState(UserState.ADDING_BIRTHDAY_DATE);
        clearPendingCommands();
    }

    private void handleContactDateInput(String dateStr, long chatId) {
        UserSession session = UserSession.getSession(chatId);
        BirthdayContact contact = session.getSelectedBirthdayContact();

        if (contact == null) {
            outputProvider.output("❌ Ошибка: контакт не найден. Начните заново.");
            showBirthdayMenu(chatId);
            return;
        }

        if (dateStr.equalsIgnoreCase("назад")) {
            session.setState(UserState.ADDING_BIRTHDAY_PHONE);
            outputProvider.output("Введите номер телефона или нажмите кнопку <b>Пропустить телефон</b>:");
            String keyboard = "{\"keyboard\":[[\"📱 Пропустить телефон\",\"↩ Назад\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
            sendTelegramMessageWithKeyboard(chatId, "Выберите действие:", keyboard);
            return;
        }

        if (dateStr.equalsIgnoreCase("🎂 Пропустить дату") || dateStr.equalsIgnoreCase("пропустить")) {
            BirthdayStorage.addContact(contact);
            outputProvider.output("✅ <b>Контакт сохранен без даты рождения!</b>\n\n");
            outputProvider.output("👤 <b>" + contact.getContactName() + "</b>");
            if (contact.getCustomMessage() != null && !contact.getCustomMessage().isEmpty() &&
                    !contact.getCustomMessage().equals("Напоминание о дне рождения")) {
                outputProvider.output("📱 " + contact.getCustomMessage());
            }
            outputProvider.output("🎂 Дата рождения: ❌ не указана\n");
            outputProvider.output("\nВы можете добавить дату рождения позже командой:" +
                    "<b>др добавить " + contact.getContactName() + " [ДД.ММ.ГГГГ]</b>");

            String keyboard = "{\"keyboard\":[[\"🎂 Добавить дату сейчас\",\"📋 Мои контакты\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
            sendTelegramMessageWithKeyboard(chatId, "Что дальше?", keyboard);

        } else {
            try {
                LocalDate birthday = LocalDate.parse(dateStr, DATE_FORMATTER);

                if (birthday.isAfter(LocalDate.now())) {
                    outputProvider.output("❌ Дата рождения не может быть в будущем. Пожалуйста, введите корректную дату.");
                    return;
                }

                // Устанавливаем дату рождения
                contact.setBirthday(birthday);

                // Сохраняем контакт
                BirthdayStorage.addContact(contact);

                // Показываем информацию о контакте
                StringBuilder response = new StringBuilder();
                response.append("✅ <b>Контакт успешно сохранен!</b>\n\n");
                response.append("👤 <b>").append(contact.getContactName()).append("</b>\n");

                if (contact.getCustomMessage() != null && !contact.getCustomMessage().isEmpty() &&
                        !contact.getCustomMessage().equals("Напоминание о дне рождения")) {
                    response.append("📱 ").append(contact.getCustomMessage()).append("\n");
                }

                response.append("🎂 <b>Дата рождения:</b> ").append(birthday.format(DATE_FORMATTER)).append("\n");

                // Рассчитываем возраст
                int age = LocalDate.now().getYear() - birthday.getYear();
                if (birthday.getDayOfYear() > LocalDate.now().getDayOfYear()) {
                    age--;
                }
                response.append("📅 <b>Возраст:</b> ").append(age).append(" лет\n");

                // Рассчитываем до следующего дня рождения
                LocalDate today = LocalDate.now();
                LocalDate nextBirthday = birthday.withYear(today.getYear());
                if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                    nextBirthday = nextBirthday.plusYears(1);
                }
                long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextBirthday);
                response.append("⏳ <b>До следующего дня рождения:</b> ").append(daysUntil).append(" дней\n");

                response.append("\n🔔 <b>Уведомления:</b> включены\n");
                response.append("⏰ <b>Напоминание за:</b> ").append(contact.getDaysBefore()).append(" день(ей) до дня рождения");

                outputProvider.output(response.toString());

                String keyboard = "{\"keyboard\":[[\"📋 Мои контакты\",\"➕ Добавить контакт\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
                sendTelegramMessageWithKeyboard(chatId, "Что дальше?", keyboard);

            } catch (DateTimeParseException e) {
                outputProvider.output("❌ Неверный формат даты. Используйте <b>ДД.ММ.ГГГГ</b>. Пожалуйста, введите дату снова.");
                return;
            }
        }

        // Очищаем временный контакт
        session.clearSelectedBirthdayContact();
        clearPendingCommands();
    }

    private void sendTelegramMessageWithKeyboard(long chatId, String text, String keyboard) {
        try {
            if (outputProvider instanceof TelegramOutputProvider) {
                TelegramOutputProvider telegramOutput = (TelegramOutputProvider) outputProvider;

                String urlString = "https://api.telegram.org/bot" + telegramOutput.getBotToken() + "/sendMessage";

                StringBuilder postDataBuilder = new StringBuilder();
                postDataBuilder.append("chat_id=").append(chatId)
                        .append("&text=").append(java.net.URLEncoder.encode(text, "UTF-8"))
                        .append("&parse_mode=HTML")
                        .append("&reply_markup=").append(java.net.URLEncoder.encode(keyboard, "UTF-8"));

                String postData = postDataBuilder.toString();

                java.net.URL url = new java.net.URL(urlString);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000);
                conn.setReadTimeout(10000);

                try (java.io.OutputStream os = conn.getOutputStream()) {
                    byte[] input = postData.getBytes("UTF-8");
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("❌ Ошибка отправки сообщения с клавиатурой: " + responseCode);
                } else {
                    System.out.println("✅ Сообщение с клавиатурой успешно отправлено");
                }

                conn.disconnect();
            }
        } catch (Exception e) {
            System.err.println("Ошибка отправки сообщения с клавиатурой: " + e.getMessage());
            outputProvider.output(text);
        }
    }

    public void processContactFromTelegram(long chatId, long contactId, String contactName, String phoneNumber) {
        System.out.println("Обработка контакта от Telegram: " + contactName + " (ID: " + contactId + ", Телефон: " + phoneNumber + ")");

        outputProvider.setCurrentChatId(chatId);

        // Проверяем, существует ли уже такой контакт
        List<BirthdayContact> existingContacts = BirthdayStorage.getContactsByChatId(chatId);
        BirthdayContact existingContact = null;

        for (BirthdayContact contact : existingContacts) {
            if (contact.getContactId() == contactId || contact.getContactName().equalsIgnoreCase(contactName)) {
                existingContact = contact;
                break;
            }
        }

        if (existingContact != null) {
            StringBuilder response = new StringBuilder();
            response.append("✅ <b>Контакт уже есть в вашем списке!</b>\n\n");
            response.append("👤 <b>").append(existingContact.getContactName()).append("</b>\n");

            if (existingContact.getBirthday() != null) {
                response.append("🎂 <b>Дата рождения:</b> ").append(existingContact.getBirthday().format(DATE_FORMATTER)).append("\n");
                response.append("\nЧтобы изменить дату рождения:\n");
                response.append("<b>др добавить ").append(existingContact.getContactName()).append(" [Новая_дата]</b>");
            } else {
                response.append("🎂 <b>Дата рождения:</b> ❌ не указана\n");
                response.append("\nЧтобы добавить дату рождения:\n");
                response.append("<b>др добавить ").append(existingContact.getContactName()).append(" [ДД.ММ.ГГГГ]</b>");
            }

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                response.append("\n\n📱 <b>Телефон:</b> ").append(phoneNumber);
            }

            outputProvider.output(response.toString());

        } else {
            // Создаем новый контакт
            BirthdayContact newContact = new BirthdayContact(chatId, contactId, contactName);
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                newContact.setCustomMessage("Телефон: " + phoneNumber);
            }

            try {
                // Сохраняем контакт без даты рождения
                BirthdayStorage.addContact(newContact);

                StringBuilder response = new StringBuilder();
                response.append("✅ <b>Контакт успешно добавлен!</b>\n\n");
                response.append("👤 <b>").append(contactName).append("</b>\n");

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    response.append("📱 <b>Телефон:</b> ").append(phoneNumber).append("\n");
                }

                response.append("🎂 <b>Дата рождения:</b> ❌ не указана\n\n");
                response.append("<b>Чтобы добавить дату рождения:</b>\n");
                response.append("<b>др добавить ").append(contactName).append(" [ДД.ММ.ГГГГ]</b>\n\n");
                response.append("<b>Например:</b>\n");
                response.append("<b>др добавить ").append(contactName).append(" 15.05.1990</b>");

                outputProvider.output(response.toString());

            } catch (Exception e) {
                System.err.println("Ошибка при сохранении контакта: " + e.getMessage());
                outputProvider.output("❌ <b>Ошибка при сохранении контакта:</b> " + e.getMessage());
            }
        }

        showBirthdayMenu(chatId);
        clearPendingCommands();
    }

    private void handleBirthdayCommand(String command, long chatId) {
        System.out.println("Обработка команды дня рождения: " + command);

        if (command.startsWith("/birthday")) {
            String rest = command.substring("/birthday".length()).trim();
            if (rest.isEmpty()) {
                showBirthdayMenu(chatId);
                return;
            }
            command = "др " + rest;
        }

        String[] parts = command.substring("др".length()).trim().split("\\s+", 3);

        if (parts.length == 0 || parts[0].isEmpty()) {
            showBirthdayMenu(chatId);
            return;
        }

        String subCommand = parts[0].toLowerCase();

        if (subCommand.equals("добавить") && parts.length >= 3) {
            // parts[1] может быть только именем, parts[2] - дата
            String name = parts[1];
            String dateStr = parts[2];
            handleAddBirthday(name, dateStr, chatId);
        } else if (subCommand.equals("список")) {
            showAllBirthdays(chatId);
        } else if (subCommand.equals("найти") && parts.length >= 2) {
            // parts[1] содержит полное имя
            String name = parts[1];
            if (parts.length > 2) {
                // Если имя состоит из нескольких слов
                name = parts[1] + " " + parts[2];
            }
            findBirthdayContact(name, chatId);
        } else if (subCommand.equals("удалить") && parts.length >= 2) {
            // parts[1] содержит полное имя
            String name = parts[1];
            if (parts.length > 2) {
                // Если имя состоит из нескольких слов
                name = parts[1] + " " + parts[2];
            }
            deleteBirthdayContact(name, chatId);
        } else if (subCommand.equals("настройки") && parts.length >= 2) {
            String rest = parts.length > 1 ? parts[1] : "";
            if (parts.length > 2) {
                rest = parts[1] + " " + parts[2];
            }

            String[] settingsParts = rest.split("\\s+(?=[^\\s]*;[^\\s]*$)");

            if (settingsParts.length >= 2) {
                String name = settingsParts[0];
                String settings = settingsParts[1];
                handleBirthdaySettings(name, settings, chatId);
            } else {
                int semicolonIndex = rest.lastIndexOf(',');
                if (semicolonIndex > 0) {
                    String settings = rest.substring(semicolonIndex + 1).trim();
                    String name = rest.substring(0, semicolonIndex).trim();

                    // Удаляем возможные числа из имени
                    name = name.replaceAll("\\s+\\d+\\s*$", "").trim();

                    handleBirthdaySettings(name, settings, chatId);
                } else {
                    String name = rest.trim();
                    handleBirthdaySettings(name, "", chatId);
                }
            }
        } else if (subCommand.equals("включить") && parts.length >= 2) {
            String name = parts[1];
            if (parts.length > 2) {
                name = parts[1] + " " + parts[2];
            }
            handleToggleNotifications(name, true, chatId);
        } else if (subCommand.equals("выключить") && parts.length >= 2) {
            String name = parts[1];
            if (parts.length > 2) {
                name = parts[1] + " " + parts[2];
            }
            handleToggleNotifications(name, false, chatId);
        } else {
            String rest = String.join(" ", parts);
            String[] tokens = rest.split("\\s+");
            for (int i = tokens.length - 1; i >= 0; i--) {
                if (tokens[i].matches("\\d{1,2}\\.\\d{1,2}\\.\\d{4}")) {
                    String dateStr = tokens[i];
                    String name = "";
                    for (int j = 0; j < i; j++) {
                        if (j > 0) name += " ";
                        name += tokens[j];
                    }
                    if (!name.isEmpty()) {
                        handleAddBirthday(name, dateStr, chatId);
                        return;
                    }
                }
            }

            outputProvider.output("❌ <b>Неизвестная команда или неверный формат</b>\n\n");
        }

        clearPendingCommands();
    }

    private void handleAddBirthday(String name, String dateStr, long chatId) {
        System.out.println("Добавление дня рождения для \"" + name + "\": " + dateStr);

        try {
            LocalDate birthday = LocalDate.parse(dateStr, DATE_FORMATTER);

            if (birthday.isAfter(LocalDate.now())) {
                outputProvider.output("❌ <b>Ошибка:</b> Дата рождения не может быть в будущем");
                return;
            }

            List<BirthdayContact> existingContacts = BirthdayStorage.getContactsByChatId(chatId);
            BirthdayContact existingContact = null;

            // Ищем контакт по имени (точное совпадение)
            for (BirthdayContact contact : existingContacts) {
                if (contact.getContactName().equalsIgnoreCase(name)) {
                    existingContact = contact;
                    break;
                }
            }

            if (existingContact != null) {
                // Обновляем существующий контакт
                BirthdayStorage.updateBirthday(existingContact.getId(), chatId, birthday);
                existingContact.setBirthday(birthday);

                // Обновляем напоминания
                if (birthdayManager != null) {
                    birthdayManager.updateContactBirthday(existingContact.getId(), chatId, birthday);
                }

                outputProvider.output("✅ <b>Дата рождения обновлена!</b>\n\n");
            } else {
                // Создаем новый контакт
                long contactId = System.currentTimeMillis();
                if (birthdayManager != null) {
                    birthdayManager.addContact(chatId, contactId, name, birthday);
                } else {
                    BirthdayContact newContact = new BirthdayContact(chatId, contactId, name, birthday);
                    BirthdayStorage.addContact(newContact);
                }

                outputProvider.output("✅ <b>Контакт создан!</b>\n\n");
            }

            outputProvider.output("👤 <b>" + name + "</b>");
            outputProvider.output("🎂 <b>Дата рождения:</b> " + birthday.format(DATE_FORMATTER));

            int age = LocalDate.now().getYear() - birthday.getYear();
            if (birthday.getDayOfYear() > LocalDate.now().getDayOfYear()) {
                age--;
            }
            outputProvider.output("📅 <b>Возраст:</b> " + age + " лет");

            LocalDate today = LocalDate.now();
            LocalDate nextBirthday = birthday.withYear(today.getYear());
            if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                nextBirthday = nextBirthday.plusYears(1);
            }
            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextBirthday);
            outputProvider.output("⏳ <b>До следующего дня рождения:</b> " + daysUntil + " дней");

            outputProvider.output("\n🔔 <b>Уведомления:</b> включены");
            outputProvider.output("⏰ <b>Напоминание за:</b> 1 день до дня рождения");

            String keyboard = "{\"keyboard\":[[\"📋 Мои контакты\",\"➕ Добавить контакт\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
            sendTelegramMessageWithKeyboard(chatId, "Что дальше?", keyboard);

        } catch (DateTimeParseException e) {
            outputProvider.output("❌ <b>Неверный формат даты.</b> Используйте <b>ДД.ММ.ГГГГ</b>");
            outputProvider.output("\nПопробуйте снова:");
            outputProvider.output("<b>" + name + " [ДД.ММ.ГГГГ]</b>");

            // Снова ждем ввод
            pendingBirthdayCommand = "add";
            pendingBirthdayData = name;
        } catch (Exception e) {
            System.err.println("Ошибка при добавлении дня рождения: " + e.getMessage());
            e.printStackTrace();
            outputProvider.output("❌ <b>Произошла ошибка:</b> " + e.getMessage());
        }
    }

    private void handleToggleNotifications(String name, boolean enabled, long chatId) {
        List<BirthdayContact> contacts = BirthdayStorage.getContactsByChatId(chatId);
        BirthdayContact foundContact = null;

        for (BirthdayContact contact : contacts) {
            if (contact.getContactName().equalsIgnoreCase(name)) {
                foundContact = contact;
                break;
            }
        }

        if (foundContact == null) {
            outputProvider.output("❌ Контакт \"" + name + "\" не найден");
            return;
        }

        try {
            BirthdayStorage.toggleNotifications(foundContact.getId(), chatId, enabled);
            outputProvider.output("✅ <b>Уведомления " + (enabled ? "включены" : "выключены") + " для " + name + "</b>");
        } catch (Exception e) {
            System.err.println("Ошибка при изменении настроек уведомлений: " + e.getMessage());
            outputProvider.output("❌ <b>Ошибка при изменении настроек:</b> " + e.getMessage());
        }
    }

    private void showAllBirthdays(long chatId) {
        List<BirthdayContact> contacts = BirthdayStorage.getContactsByChatId(chatId);

        if (contacts.isEmpty()) {
            outputProvider.output("\uD83D\uDE14 <b>У вас пока нет контактов с днями рождения</b>");
            outputProvider.output("\n<b>Вы можете:</b>\n" +
                    "1. Переслать контакт из телефонной книги\n" +
                    "2. Добавить контакт вручную через меню");

            String keyboard = "{\"keyboard\":[[\"➕ Добавить контакт\",\"↩ Назад в меню\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
            sendTelegramMessageWithKeyboard(chatId, "Что дальше?", keyboard);
            UserSession.getSession(chatId).setState(UserState.MANAGING_BIRTHDAYS);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83C\uDF89 <b>Ваши контакты:</b>\n\n");

        LocalDate today = LocalDate.now();
        int countWithBirthday = 0;
        int countWithoutBirthday = 0;

        for (BirthdayContact contact : contacts) {
            if (contact.getBirthday() != null) {
                countWithBirthday++;

                LocalDate nextBirthday = contact.getBirthday().withYear(today.getYear());
                if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                    nextBirthday = nextBirthday.plusYears(1);
                }

                long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextBirthday);

                sb.append("👤 <b>").append(contact.getContactName()).append("</b>\n");
                sb.append("   🎂 <b>Дата:</b> ").append(contact.getBirthday().format(DATE_FORMATTER)).append("\n");
                sb.append("   📅 <b>До дня рождения:</b> ").append(daysUntil).append(" дней\n");
                sb.append("   ⏰ <b>Напоминать за:</b> ").append(contact.getDaysBefore()).append(" дня(ей)\n");
                sb.append("   🔔 <b>Уведомления:</b> ").append(contact.isNotificationsEnabled() ? "✅" : "❌").append("\n");

                if (contact.getCustomMessage() != null && !contact.getCustomMessage().isEmpty() &&
                        !contact.getCustomMessage().equals("Напоминание о дне рождения")) {
                    sb.append("   📱 ").append(contact.getCustomMessage()).append("\n");
                }
                sb.append("\n");
            } else {
                countWithoutBirthday++;
            }
        }

        if (countWithoutBirthday > 0) {
            sb.append("\n📝 <b>Контакты без даты рождения (").append(countWithoutBirthday).append("):</b>\n");
            for (BirthdayContact contact : contacts) {
                if (contact.getBirthday() == null) {
                    sb.append("👤 ").append(contact.getContactName());
                    if (contact.getCustomMessage() != null && !contact.getCustomMessage().isEmpty() &&
                            !contact.getCustomMessage().equals("Напоминание о дне рождения")) {
                        sb.append(" (").append(contact.getCustomMessage()).append(")");
                    }
                    sb.append("\n");
                }
            }
            sb.append("\n<b>Чтобы добавить дату рождения:</b>\n " +
                    "др добавить [Имя Фамилия] [ДД.ММ.ГГГГ]");
        }

        sb.append("\n<b>Используйте команды для управления:</b>");
        sb.append("\n• <b>др найти [Имя Фамилия]</b> - показать детали");
        sb.append("\n• <b>др удалить [Имя Фамилия]</b> - удалить контакт");
        sb.append("\n• <b>др добавить [Имя Фамилия] [ДД.ММ.ГГГГ]</b> - добавить дату рождения");

        outputProvider.output(sb.toString());

        String keyboard = "{\"keyboard\":[[\"🎂 Добавить дату\",\"🔍 Найти контакт\"],[\"🗑️ Удалить контакт\",\"↩ Назад в меню\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessageWithKeyboard(chatId, "Выберите действие:", keyboard);

        UserSession.getSession(chatId).setState(UserState.MANAGING_BIRTHDAYS);
        clearPendingCommands();
    }

    private void findBirthdayContact(String name, long chatId) {
        List<BirthdayContact> contacts = BirthdayStorage.getContactsByChatId(chatId);
        BirthdayContact foundContact = null;

        // Поиск точного совпадения
        for (BirthdayContact contact : contacts) {
            if (contact.getContactName().equalsIgnoreCase(name)) {
                foundContact = contact;
                break;
            }
        }

        // ищем частичное
        if (foundContact == null) {
            for (BirthdayContact contact : contacts) {
                if (contact.getContactName().toLowerCase().contains(name.toLowerCase())) {
                    foundContact = contact;
                    break;
                }
            }
        }

        if (foundContact == null) {
            outputProvider.output("❌ Контакт \"" + name + "\" не найден");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("\uD83D\uDC64 <b>Детали контакта:</b>\n\n");
        sb.append("👤 <b>Имя:</b> ").append(foundContact.getContactName()).append("\n");

        if (foundContact.getBirthday() != null) {
            sb.append("🎂 <b>Дата рождения:</b> ").append(foundContact.getBirthday().format(DATE_FORMATTER)).append("\n");

            LocalDate today = LocalDate.now();
            LocalDate nextBirthday = foundContact.getBirthday().withYear(today.getYear());
            if (nextBirthday.isBefore(today) || nextBirthday.isEqual(today)) {
                nextBirthday = nextBirthday.plusYears(1);
            }

            long daysUntil = java.time.temporal.ChronoUnit.DAYS.between(today, nextBirthday);
            sb.append("📅 <b>До следующего дня рождения:</b> ").append(daysUntil).append(" дней\n");

            int age = today.getYear() - foundContact.getBirthday().getYear();
            if (foundContact.getBirthday().getDayOfYear() > today.getDayOfYear()) {
                age--;
            }
            sb.append("📊 <b>Возраст:</b> ").append(age).append(" лет\n");
        } else {
            sb.append("🎂 <b>Дата рождения:</b> ❌ не указана\n");
        }

        sb.append("🔔 <b>Уведомления:</b> ").append(foundContact.isNotificationsEnabled() ? "✅ включены" : "❌ выключены").append("\n");
        sb.append("⏰ <b>Напоминать за:</b> ").append(foundContact.getDaysBefore()).append(" дня(ей)\n");

        if (foundContact.getCustomMessage() != null && !foundContact.getCustomMessage().isEmpty() &&
                !foundContact.getCustomMessage().equals("Напоминание о дне рождения")) {
            sb.append("📱 <b>Контакты:</b> ").append(foundContact.getCustomMessage()).append("\n");
        }

        sb.append("\n<b>Команды для управления:</b>\n");
        sb.append("• <b>др добавить ").append(foundContact.getContactName()).append(" [ДД.ММ.ГГГГ]</b> - добавить/изменить дату рождения\n");
        sb.append("• <b>др удалить ").append(foundContact.getContactName()).append("</b> - удалить контакт\n");
        sb.append("• <b>др настройки ").append(foundContact.getContactName()).append(" [дни];[текст]</b> - изменить настройки");

        outputProvider.output(sb.toString());
        clearPendingCommands();
    }

    private void deleteBirthdayContact(String name, long chatId) {
        List<BirthdayContact> contacts = BirthdayStorage.getContactsByChatId(chatId);
        BirthdayContact foundContact = null;

        // Поиск точного совпадения
        for (BirthdayContact contact : contacts) {
            if (contact.getContactName().equalsIgnoreCase(name)) {
                foundContact = contact;
                break;
            }
        }

        // ищем частичное
        if (foundContact == null) {
            for (BirthdayContact contact : contacts) {
                if (contact.getContactName().toLowerCase().contains(name.toLowerCase())) {
                    foundContact = contact;
                    break;
                }
            }
        }

        if (foundContact == null) {
            outputProvider.output("❌ Контакт \"" + name + "\" не найден");
            return;
        }

        outputProvider.output("❓ <b>Вы действительно хотите удалить контакт \"" + foundContact.getContactName() + "\"?</b> Введите 'да' или 'нет' для отмены.");

        UserSession.getSession(chatId).setSelectedBirthdayContact(foundContact);
        UserSession.getSession(chatId).setState(UserState.DELETING_BIRTHDAY);
        clearPendingCommands();
    }

    private void handleDeleteBirthdayConfirmation(String command, long chatId) {
        UserSession session = UserSession.getSession(chatId);
        BirthdayContact contact = session.getSelectedBirthdayContact();

        if (contact == null) {
            outputProvider.output("❌ Контакт не найден");
            showBirthdayMenu(chatId);
            return;
        }

        String cmd = command.trim().toLowerCase();

        if ("да".equals(cmd) || "yes".equals(cmd) || "подтвердить".equals(cmd) || "удалить".equals(cmd)) {
            try {
                BirthdayStorage.deleteContact(contact.getId(), chatId);
                outputProvider.output("✅ <b>Контакт \"" + contact.getContactName() + "\" успешно удалён</b>");
                session.clearSelectedBirthdayContact();
                outputProvider.showMainMenu("\uD83C\uDFE0 Возвращаемся в главное меню:");
                session.setState(UserState.MAIN_MENU);
            } catch (Exception e) {
                System.err.println("Ошибка при удалении контакта: " + e.getMessage());
                outputProvider.output("❌ <b>Ошибка при удалении контакта:</b> " + e.getMessage());
                showBirthdayMenu(chatId);
            }
        } else if ("нет".equals(cmd) || "no".equals(cmd) || "отмена".equals(cmd) || "отменить".equals(cmd)) {
            outputProvider.output("✅ Удаление отменено");
            session.clearSelectedBirthdayContact();
            showBirthdayMenu(chatId);
            session.setState(UserState.MANAGING_BIRTHDAYS);
        } else {
            outputProvider.output("❌ Пожалуйста, подтвердите удаление. Введите 'да' или 'нет':");
            outputProvider.output("Вы действительно хотите удалить контакт \"" + contact.getContactName() + "\"?");
        }
        clearPendingCommands();
    }

    private void handleBirthdaySettings(String name, String settings, long chatId) {
        System.out.println("Обработка настроек для: \"" + name + "\", настройки: \"" + settings + "\"");

        List<BirthdayContact> contacts = BirthdayStorage.getContactsByChatId(chatId);
        BirthdayContact foundContact = null;

        // Поиск по точному совпадению
        for (BirthdayContact contact : contacts) {
            if (contact.getContactName().equalsIgnoreCase(name)) {
                foundContact = contact;
                break;
            }
        }

        // Если не нашли точное совпадение, ищем частичное
        if (foundContact == null) {
            for (BirthdayContact contact : contacts) {
                if (contact.getContactName().toLowerCase().contains(name.toLowerCase())) {
                    foundContact = contact;
                    break;
                }
            }
        }

        if (foundContact == null) {
            outputProvider.output("❌ Контакт \"" + name + "\" не найден");
            outputProvider.output("Доступные контакты:");
            for (BirthdayContact contact : contacts) {
                outputProvider.output("- " + contact.getContactName());
            }
            return;
        }

        if (settings.isEmpty()) {
            outputProvider.output("📋 <b>Текущие настройки для " + foundContact.getContactName() + ":</b>\n");
            outputProvider.output("🔔 Уведомления: " + (foundContact.isNotificationsEnabled() ? "✅ включены" : "❌ выключены"));
            outputProvider.output("⏰ Напоминать за: " + foundContact.getDaysBefore() + " дня(ей)");
            outputProvider.output("💬 Текст напоминания: " + foundContact.getCustomMessage());
            outputProvider.output("\n<b>Формат для изменения:</b>");
            outputProvider.output("<b>др настройки " + foundContact.getContactName() + " [дни];[текст]</b>");
            outputProvider.output("\n<b>Пример:</b>");
            outputProvider.output("<b>др настройки " + foundContact.getContactName() + " 3;Не забудь купить подарок!</b>");
            return;
        }

        String[] parts = settings.split(";", 2);
        if (parts.length != 2) {
            outputProvider.output("❌ Неверный формат настроек. Используйте: [дни],[текст]");
            outputProvider.output("Пример: 3,Не забудь купить подарок!");
            return;
        }

        try {
            int daysBefore = Integer.parseInt(parts[0].trim());
            if (daysBefore < 1 || daysBefore > 30) {
                outputProvider.output("❌ Количество дней должно быть от 1 до 30");
                return;
            }

            String customMessage = parts[1].trim();
            if (customMessage.isEmpty()) {
                outputProvider.output("❌ Текст напоминания не может быть пустым");
                return;
            }

            BirthdayStorage.updateReminderSettings(foundContact.getId(), chatId, daysBefore, customMessage);

            outputProvider.output("✅ <b>Настройки обновлены!</b>\n");
            outputProvider.output("👤 Контакт: " + foundContact.getContactName());
            outputProvider.output("⏰ Напоминать за: " + daysBefore + " дня(ей)");
            outputProvider.output("💬 Текст: " + customMessage);

        } catch (NumberFormatException e) {
            outputProvider.output("❌ Неверный формат количества дней. Используйте число от 1 до 30");
        }
        clearPendingCommands();
    }

    private void showNotificationSettings(long chatId) {
        String settingsText = "\uD83D\uDD27 <b>Настройки уведомлений о днях рождения:</b>\n\n";
        settingsText += "<b>Для изменения настроек контакта используйте команду:</b>\n";
        settingsText += "др настройки <b>[Имя] [дни];[текст]</b>\n\n";
        settingsText += "<b>Пример:</b>\n";
        settingsText += "др настройки Иван 3;Не забудь купить подарок для Ивана!\n\n";
        settingsText += "<b>Где:</b>\n";
        settingsText += "• <b>[дни]</b> - за сколько дней напоминать (1-30)\n";
        settingsText += "• <b>[текст]</b> - текст напоминания\n\n";
        settingsText += "<b>Для включения/выключения уведомлений:</b>\n";
        settingsText += "• др включить <b>[Имя]</b>\n";
        settingsText += "• др выключить <b>[Имя]</b>";

        outputProvider.output(settingsText);

        String keyboard = "{\"keyboard\":[[\"📋 Мои контакты\",\"↩ Назад в меню\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessageWithKeyboard(chatId, "Выберите действие:", keyboard);
        clearPendingCommands();
    }

    private String normalize(String input) {
        if (input == null) return "";
        String normalized = input.trim().toLowerCase();

        normalized = normalized.replace("📋", "")
                .replace("➕", "")
                .replace("⚙️", "")
                .replace("↩", "")
                .replace("\uD83C\uDF89", "")
                .replace("\uD83D\uDCDD", "")
                .replace("\u270D\uFE0F", "")
                .replace("\uD83D\uDD27", "")
                .replace("\uD83D\uDC64", "")
                .replace("\uD83D\uDE14", "")
                .trim();

        return normalized;
    }

    private void handleCreateReminder(String command, long chatId) {
        System.out.println("Обработка создания напоминания: \"" + command + "\" для chatId " + chatId);
        // Проверяем, не хочет ли пользователь вернуться назад
        if (command.equalsIgnoreCase("назад")) {
            menuManager.showMenu();
            UserSession.getSession(chatId).setState(UserState.IN_MENU);
            return;
        }
        ReminderParser.ParseResult parseResult = ReminderParser.parse(command);

        if (parseResult != null) {
            Reminder reminder = new Reminder(chatId, parseResult.getText(), parseResult.getTriggerTime());
            ReminderStorage.add(reminder);
            reminderScheduler.schedule(reminder);

            outputProvider.output("✅ <b>Напоминание создано!</b>\n");
            outputProvider.output("📝 <b>" + parseResult.getText() + "</b>");
            outputProvider.output("⏰ <b>" + parseResult.getTriggerTime().format(DATE_TIME_FORMATTER) + "</b>");

            outputProvider.showMainMenu("\uD83C\uDFE0 Возвращаемся в главное меню:");
            UserSession.getSession(chatId).setState(UserState.MAIN_MENU);
        } else {
            outputProvider.output("❌ <b>Не удалось распознать напоминание</b>\n");
            outputProvider.output("<b>Примеры форматов:</b>\n" +
                    "• напомни через 5 минут/завтра в 15:00/25.12.2025 в 10:00 выпить воды");
            outputProvider.output("<b>Попробуйте еще раз:</b>");
        }
        clearPendingCommands();
    }

    private void clearPendingCommands() {
        pendingBirthdayCommand = null;
        pendingBirthdayData = null;
    }

    public boolean isRunning() {
        return isRunning;
    }
}
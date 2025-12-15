package org;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SimpleTelegramBot {
    private final String botToken;
    private final TelegramInputProvider inputProvider;
    private final TelegramOutputProvider outputProvider;
    private final ReminderScheduler reminderScheduler;
    private final BirthdayManager birthdayManager;
    private CommandProcessor processor;
    private boolean running = true;

    public SimpleTelegramBot(String botToken,
                             TelegramInputProvider inputProvider,
                             TelegramOutputProvider outputProvider,
                             ReminderScheduler reminderScheduler,
                             BirthdayManager birthdayManager) {
        this.botToken = botToken;
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
        this.reminderScheduler = reminderScheduler;
        this.birthdayManager = birthdayManager;
    }

    public void setProcessor(CommandProcessor processor) {
        this.processor = processor;
    }

    public void start() {
        System.out.println("Запуск Telegram бота...");
        Thread botThread = new Thread(this::pollUpdates);
        botThread.setDaemon(true);
        botThread.start();
    }

    public void stop() {
        running = false;
        if (reminderScheduler != null) {
            reminderScheduler.shutdown();
        }
        if (birthdayManager != null) {
            birthdayManager.shutdown();
        }
    }

    private void pollUpdates() {
        int lastUpdateId = 0;

        while (running) {
            try {
                String updatesJson = sendGetRequest("getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=30");

                if (updatesJson.contains("\"ok\":true") && updatesJson.contains("\"result\":")) {
                    String[] updates = updatesJson.split("\"update_id\"");

                    for (int i = 1; i < updates.length; i++) {
                        String update = updates[i];

                        int idStart = update.indexOf(":") + 1;
                        int idEnd = update.indexOf(",", idStart);
                        if (idEnd == -1) idEnd = update.indexOf("}", idStart);
                        if (idEnd <= idStart) continue;
                        lastUpdateId = Integer.parseInt(update.substring(idStart, idEnd).trim());

                        if (update.contains("\"message\"")) {
                            processMessage(update);
                        }
                    }
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Ошибка в боте: " + e.getMessage());
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }

    private void processMessage(String updateJson) {
        try {
            int chatIdIndex = updateJson.indexOf("\"chat\"");
            if (chatIdIndex == -1) return;

            int idIndex = updateJson.indexOf("\"id\"", chatIdIndex);
            if (idIndex == -1) return;

            int idStart = updateJson.indexOf(":", idIndex) + 1;
            int idEnd = updateJson.indexOf(",", idStart);
            if (idEnd == -1) idEnd = updateJson.indexOf("}", idStart);
            if (idEnd <= idStart) return;

            long chatId = Long.parseLong(updateJson.substring(idStart, idEnd).trim());

            outputProvider.setCurrentChatId(chatId);

            if (updateJson.contains("\"contact\"")) {
                processContactFromMessage(updateJson, chatId);
                return;
            }

            // Проверяем, есть ли пересланный контакт
            if (updateJson.contains("\"forward_from\"")) {
                processForwardedContact(updateJson, chatId);
                return;
            }

            int textIndex = updateJson.indexOf("\"text\"");
            if (textIndex == -1) return;

            int textStart = updateJson.indexOf(":", textIndex) + 1;
            int textEnd = updateJson.indexOf(",", textStart);
            if (textEnd == -1) textEnd = updateJson.indexOf("}", textStart);
            if (textEnd <= textStart) return;

            String rawText = updateJson.substring(textStart, textEnd).trim();
            if (rawText.startsWith("\"") && rawText.endsWith("\"")) {
                rawText = rawText.substring(1, rawText.length() - 1);
            }

            String decodedText = decodeUnicodeEscapes(rawText);
            String text = decodedText;

            System.out.println("Получено сообщение от " + chatId + ": \"" + text + "\"");

            if ("/start".equalsIgnoreCase(text)) {
                outputProvider.output("\uD83C\uDF89 Добро пожаловать в <b>бот-напоминальщик!</b> Я помогу вам не забывать о важных событиях и днях рождения!");
                outputProvider.output("\n📱 <b>Вы можете</b>:\n" +
                        "• Создавать напоминания\n" +
                        "• Пересылать контакты из телефонной книги\n" +
                        "• Добавлять дни рождения вручную");
                outputProvider.showMainMenu("\uD83C\uDFE0 Главное меню - выберите действие:");
                UserSession.getSession(chatId).setState(UserState.MAIN_MENU);
                return;
            }

            if ("/help".equalsIgnoreCase(text)) {
                showHelp(chatId);
                return;
            }

            if ("/menu".equalsIgnoreCase(text)) {
                outputProvider.outputMenu("\uD83D\uDCCB Меню - выберите действие:");
                UserSession.getSession(chatId).setState(UserState.IN_MENU);
                return;
            }

            if ("/birthdays".equalsIgnoreCase(text)) {
                if (processor != null) {
                    processor.showBirthdayMenu(chatId);
                }
                return;
            }

            if ("/exit".equalsIgnoreCase(text)) {
                outputProvider.output("\uD83D\uDC4B Завершение работы...");
                outputProvider.output("Спасибо за использование бота! До встречи!");
                UserSession.clearSession(chatId);
                return;
            }

            if (processor != null) {
                new Thread(() -> {
                    processor.processCommand(text, chatId);
                }).start();
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showHelp(long chatId) {
        String helpText = "\uD83D\uDCDA <b>Справка по боту:</b>\n\n";
        helpText += "<b>Основные команды:</b>\n";
        helpText += "• <b>/start</b> - начать работу\n";
        helpText += "• <b>/menu</b> - основное меню\n";
        helpText += "• <b>/help</b> - показать справку\n";
        helpText += "• <b>/birthdays</b> - управление днями рождения\n";
        helpText += "• <b>/exit</b> - завершить работу\n\n";

        helpText += "<b>Создание напоминания:</b>\n";
        helpText += "Формат: <b>напомни [дата] [время] [сообщение]</b>\n";
        helpText += "Примеры:\n";
        helpText += "• напомни через 5 минут выпить/завтра в 15:00/25.12.2025 в 10:00 воды\n";

        helpText += "<b>Управление днями рождения:</b>\n";
        helpText += "📱 <b>Вы можете:</b>\n";
        helpText += "• Переслать контакт из телефонной книги или добавить вручную через меню <b>'Дни рождения'</b>\n";

        helpText += "<b>Команды для дней рождения:</b>\n";
        helpText += "• <b>др добавить [имя] [ДД.ММ.ГГГГ]</b>\n";
        helpText += "• <b>др список</b>\n";
        helpText += "• <b>др найти [имя]</b>\n";
        helpText += "• <b>др удалить [имя]</b>\n";

        outputProvider.output(helpText);

        // Показываем главное меню после справки
        outputProvider.showMainMenu("\uD83C\uDFE0 Вернуться в главное меню:");
        UserSession.getSession(chatId).setState(UserState.MAIN_MENU);
    }

    private void processContactFromMessage(String updateJson, long chatId) {
        try {
            System.out.println("Обнаружен контакт в сообщении (отправлен через кнопку 'Отправить контакт')");

            int contactIndex = updateJson.indexOf("\"contact\"");
            if (contactIndex == -1) return;

            // Извлекаем данные контакта
            long contactId = extractContactField(updateJson, contactIndex, "user_id", -1);
            String phoneNumber = extractContactField(updateJson, contactIndex, "phone_number", "");
            String firstName = extractContactField(updateJson, contactIndex, "first_name", "");
            String lastName = extractContactField(updateJson, contactIndex, "last_name", "");

            String contactName = firstName;
            if (!lastName.isEmpty()) {
                contactName += " " + lastName;
            }

            // Если нет user_id, создаем из phone_number
            if (contactId == -1 && !phoneNumber.isEmpty()) {
                String phoneDigits = phoneNumber.replaceAll("[^\\d]", "");
                if (!phoneDigits.isEmpty()) {
                    try {
                        contactId = Long.parseLong(phoneDigits.substring(Math.max(0, phoneDigits.length() - 9)));
                    } catch (NumberFormatException e) {
                        contactId = System.currentTimeMillis();
                    }
                } else {
                    contactId = System.currentTimeMillis();
                }
            }

            System.out.println("Обработка контакта: " + contactName + " (ID: " + contactId + ", Телефон: " + phoneNumber + ")");

            if (processor != null) {
                processor.processContactFromTelegram(chatId, contactId, contactName, phoneNumber);
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки контакта из сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processForwardedContact(String updateJson, long chatId) {
        try {

            int forwardFromIndex = updateJson.indexOf("\"forward_from\"");
            if (forwardFromIndex == -1) return;

            // Извлекаем данные пересланного пользователя
            long contactId = extractForwardedField(updateJson, forwardFromIndex, "id", -1);
            String firstName = extractForwardedField(updateJson, forwardFromIndex, "first_name", "");
            String lastName = extractForwardedField(updateJson, forwardFromIndex, "last_name", "");
            String username = extractForwardedField(updateJson, forwardFromIndex, "username", "");

            // Формируем имя контакта
            String contactName = firstName;
            if (!lastName.isEmpty()) {
                contactName += " " + lastName;
            }
            if (contactName.isEmpty() && !username.isEmpty()) {
                contactName = "@" + username;
            }
            if (contactName.isEmpty()) {
                contactName = "Неизвестный контакт";
            }

            System.out.println("Обработка пересланного контакта: " + contactName + " (ID: " + contactId + ")");

            if (processor != null) {
                // Для пересланных контактов нет номера телефона
                processor.processContactFromTelegram(chatId, contactId, contactName, null);
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки пересланного контакта: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String extractContactField(String json, int contactIndex, String fieldName, String defaultValue) {
        try {
            int fieldIndex = json.indexOf("\"" + fieldName + "\"", contactIndex);
            if (fieldIndex == -1) return defaultValue;

            int valueStart = json.indexOf(":", fieldIndex) + 1;
            int valueEnd = json.indexOf(",", valueStart);
            if (valueEnd == -1) valueEnd = json.indexOf("}", valueStart);
            if (valueEnd <= valueStart) return defaultValue;

            String value = json.substring(valueStart, valueEnd).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            return decodeUnicodeEscapes(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String extractForwardedField(String json, int forwardIndex, String fieldName, String defaultValue) {
        try {
            int fieldIndex = json.indexOf("\"" + fieldName + "\"", forwardIndex);
            if (fieldIndex == -1) return defaultValue;

            int valueStart = json.indexOf(":", fieldIndex) + 1;
            int valueEnd = json.indexOf(",", valueStart);
            if (valueEnd == -1) valueEnd = json.indexOf("}", valueStart);
            if (valueEnd <= valueStart) return defaultValue;

            String value = json.substring(valueStart, valueEnd).trim();
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }

            return decodeUnicodeEscapes(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private long extractContactField(String json, int contactIndex, String fieldName, long defaultValue) {
        try {
            String strValue = extractContactField(json, contactIndex, fieldName, "");
            if (strValue.isEmpty()) return defaultValue;
            return Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private long extractForwardedField(String json, int forwardIndex, String fieldName, long defaultValue) {
        try {
            String strValue = extractForwardedField(json, forwardIndex, fieldName, "");
            if (strValue.isEmpty()) return defaultValue;
            return Long.parseLong(strValue);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String decodeUnicodeEscapes(String input) {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); ) {
            if (i + 6 <= input.length() && input.charAt(i) == '\\' && input.charAt(i + 1) == 'u') {
                try {
                    int codePoint = Integer.parseInt(input.substring(i + 2, i + 6), 16);
                    result.append((char) codePoint);
                    i += 6;
                } catch (NumberFormatException e) {
                    result.append(input.charAt(i));
                    i++;
                }
            } else {
                result.append(input.charAt(i));
                i++;
            }
        }
        return result.toString();
    }

    private String sendGetRequest(String method) throws IOException {
        String urlStr = "https://api.telegram.org/bot" + botToken + "/" + method;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            try (BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = errorReader.readLine()) != null) {
                    response.append(line);
                }
            } catch (Exception ex) {
                response.append("{\"ok\":false,\"error\":\"").append(e.getMessage()).append("\"}");
            }
        } finally {
            conn.disconnect();
        }
        return response.toString();
    }
}
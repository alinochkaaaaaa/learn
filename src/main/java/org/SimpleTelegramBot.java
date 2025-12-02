package org;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SimpleTelegramBot {
    private final String botToken;
    private final TelegramInputProvider inputProvider;
    private final TelegramOutputProvider outputProvider;
    private final ReminderScheduler reminderScheduler;
    private CommandProcessor processor;
    private boolean running = true;

    public SimpleTelegramBot(String botToken,
                             TelegramInputProvider inputProvider,
                             TelegramOutputProvider outputProvider,
                             ReminderScheduler reminderScheduler) {
        this.botToken = botToken;
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
        this.reminderScheduler = reminderScheduler;
    }

    public void setProcessor(CommandProcessor processor) {
        this.processor = processor;
    }

    public void start() {
        System.out.println(" Запуск Telegram бота...");
        Thread botThread = new Thread(this::pollUpdates);
        botThread.setDaemon(true);
        botThread.start();
        System.out.println("✅ Бот запущен и ожидает сообщений");
    }

    public void stop() {
        System.out.println(" Остановка бота...");
        running = false;
        if (reminderScheduler != null) {
            reminderScheduler.shutdown();
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
                System.err.println("❌ Ошибка в боте: " + e.getMessage());
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

            System.out.println(" Получено сообщение от " + chatId + ": \"" + text + "\"");
            outputProvider.setCurrentChatId(chatId);

            // Проверка системных команд
            if ("/start".equalsIgnoreCase(text)) {
                System.out.println("✅ Обработка команды /start для chatId " + chatId);
                outputProvider.output("🎉 Добро пожаловать в бот-напоминальщик!");
                outputProvider.showMainMenu("🏠 Главное меню - выберите действие:");
                UserSession.setState(chatId, UserState.MAIN_MENU);
                return;
            }

            if ("/help".equalsIgnoreCase(text)) {
                System.out.println("✅ Обработка команды /help для chatId " + chatId);
                outputProvider.output("📚 Справка по боту:");
                outputProvider.output("• Используйте кнопки меню для навигации");
                outputProvider.output("• Для создания напоминания используйте формат:");
                outputProvider.output("  напомни [дата] [время] [сообщение]");
                outputProvider.output("• Пример: напомни завтра в 15:00 позвонить маме");
                outputProvider.showMainMenu("🏠 Главное меню - выберите действие:");
                return;
            }

            if ("/reminders".equalsIgnoreCase(text)) {
                System.out.println("✅ Обработка команды /reminders для chatId " + chatId);
                var reminders = ReminderStorage.getAllByChatId(chatId);
                if (reminders.isEmpty()) {
                    outputProvider.output(" У вас нет активных напоминаний.");
                } else {
                    StringBuilder sb = new StringBuilder("📋 Ваши напоминания:\n\n");
                    java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                    for (Reminder r : reminders) {
                        sb.append("⏰ ").append(r.getTriggerTime().format(fmt))
                                .append("\n💬 \"").append(r.getMessage()).append("\"\n\n");
                    }
                    outputProvider.output(sb.toString().trim());
                }
                outputProvider.showMainMenu("🏠 Главное меню - выберите действие:");
                return;
            }

            if ("/status".equalsIgnoreCase(text)) {
                System.out.println("✅ Обработка команды /status для chatId " + chatId);
                outputProvider.output(" Статус бота:");
                outputProvider.output("• Бот работает ✅");
                outputProvider.output("• MongoDB подключена ✅");
                outputProvider.output("• Напоминания активны ✅");
                return;
            }

            // Передача команды процессору
            if (processor != null) {
                System.out.println("🔄 Передача команды процессору: " + text);
                new Thread(() -> {
                    processor.processCommand(text, chatId);
                }).start();
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка обработки сообщения: " + e.getMessage());
            e.printStackTrace();
        }
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
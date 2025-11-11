package org;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class SimpleTelegramBot {
    private final String botToken;
    private final TelegramInputProvider inputProvider;
    private final TelegramOutputProvider outputProvider;
    private CommandProcessor processor;
    private boolean running = true;

    public SimpleTelegramBot(String botToken, TelegramInputProvider inputProvider,
                             TelegramOutputProvider outputProvider) {
        this.botToken = botToken;
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
    }

    public void setProcessor(CommandProcessor processor) {
        this.processor = processor;
    }

    public void start() {
        System.out.println("🤖 Запуск Telegram бота...");
        Thread botThread = new Thread(this::pollUpdates);
        botThread.setDaemon(true);
        botThread.start();
    }

    public void stop() {
        running = false;
    }

    private void pollUpdates() {
        int lastUpdateId = 0;

        while (running) {
            try {
                String updatesJson = sendGetRequest("getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=30");

                // Простой парсинг JSON без библиотек
                if (updatesJson.contains("\"ok\":true") && updatesJson.contains("\"result\":")) {
                    String[] updates = updatesJson.split("\"update_id\"");

                    for (int i = 1; i < updates.length; i++) {
                        String update = updates[i];

                        // Извлекаем update_id
                        int idStart = update.indexOf(":") + 1;
                        int idEnd = update.indexOf(",", idStart);
                        if (idEnd == -1) idEnd = update.indexOf("}", idStart);
                        lastUpdateId = Integer.parseInt(update.substring(idStart, idEnd).trim());

                        // Ищем сообщение
                        if (update.contains("\"message\"")) {
                            processMessage(update);
                        }
                    }
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Ошибка в боте: " + e.getMessage());
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
            // Извлекаем chat_id
            int chatIdIndex = updateJson.indexOf("\"chat\"");
            int idIndex = updateJson.indexOf("\"id\"", chatIdIndex);
            int idStart = updateJson.indexOf(":", idIndex) + 1;
            int idEnd = updateJson.indexOf(",", idStart);
            if (idEnd == -1) idEnd = updateJson.indexOf("}", idStart);
            long chatId = Long.parseLong(updateJson.substring(idStart, idEnd).trim());

            // Извлекаем текст сообщения
            int textIndex = updateJson.indexOf("\"text\"");
            int textStart = updateJson.indexOf(":", textIndex) + 1;
            int textEnd = updateJson.indexOf(",", textStart);
            if (textEnd == -1) textEnd = updateJson.indexOf("}", textStart);
            String text = updateJson.substring(textStart, textEnd).trim().replace("\"", "");

            System.out.println("📨 Получено сообщение: " + text + " от " + chatId);

            // Обрабатываем специальные команды
            if ("/start".equals(text)) {
                outputProvider.setCurrentChatId(chatId);
                outputProvider.output("Добро пожаловать! Я ваш бот. Введите команду или 'help' для помощи.");
                return;
            }

            // Передаем ввод в основную логику
            inputProvider.addInput(text, chatId);
            outputProvider.setCurrentChatId(chatId);

            // Запускаем обработку команды в отдельном потоке
            if (processor != null) {
                new Thread(() -> {
                    processor.processCommand(text);
                }).start();
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
        }
    }

    private String sendGetRequest(String method) throws IOException {
        String urlStr = "https://api.telegram.org/bot" + botToken + "/" + method;
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        return response.toString();
    }
}
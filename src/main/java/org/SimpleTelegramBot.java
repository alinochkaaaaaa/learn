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
        System.out.println("Запуск Telegram бота...");
        Thread botThread = new Thread(this::pollUpdates); //Создание нового потока с именем botThread
        botThread.setDaemon(true); //если основное приложение завершится, бот тоже остановится
        botThread.start();
    }

    public void stop() {
        running = false;
    }

    private void pollUpdates() {
        int lastUpdateId = 0; //для отслеживания ID последнего обработанного обновления

        while (running) {
            try {
                String updatesJson = sendGetRequest("getUpdates?offset=" + (lastUpdateId + 1) + "&timeout=30");
                //обновления с ID больше последнего, long-polling: ждем до 30 секунд для новых сообщений

                // ответ API и получение массива обновлений
                if (updatesJson.contains("\"ok\":true") && updatesJson.contains("\"result\":")) {
                    String[] updates = updatesJson.split("\"update_id\"");

                    for (int i = 1; i < updates.length; i++) {
                        String update = updates[i];

                        // update_id
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

            // Устанавливаем текущий chatId
            outputProvider.setCurrentChatId(chatId);

            // Обрабатываем команду /start отдельно для показа главного меню
            if ("/start".equals(text)) {
                outputProvider.output("Добро пожаловать! Я ваш бот.");
                outputProvider.showMainMenu("Главное меню - выберите действие:");
                return;
            }

            // Передаем ввод в основную логику
            inputProvider.addInput(text, chatId);

            // Запускаем обработку команды в отдельном потоке
            if (processor != null) {
                new Thread(() -> {
                    processor.processCommand(text);
                }).start();
            }

        } catch (Exception e) {
            System.err.println("Ошибка обработки сообщения: " + e.getMessage());
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
            // Читаем ошибку если есть
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
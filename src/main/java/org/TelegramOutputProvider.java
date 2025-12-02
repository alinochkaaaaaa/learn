package org;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TelegramOutputProvider implements OutputProvider {
    private final String botToken;
    private Long currentChatId;

    public TelegramOutputProvider(String botToken) {
        this.botToken = botToken;
        System.out.println(" TelegramOutputProvider создан с токеном: " + botToken.substring(0, Math.min(10, botToken.length())) + "...");
    }

    public void setCurrentChatId(Long chatId) {
        this.currentChatId = chatId;
    }

    @Override
    public void output(String message) {
        if (currentChatId != null && message != null && !message.trim().isEmpty()) {
            sendTelegramMessage(currentChatId, message, null);
        } else if (currentChatId == null) {
            System.err.println("⚠️ Не установлен currentChatId для отправки сообщения");
        }
    }

    @Override
    public void outputMenu(String menu) {
        if (currentChatId != null) {
            sendTelegramMessageWithMenu(currentChatId, menu);
        }
    }

    @Override
    public void showMessage(String message) {
        output(message);
    }

    @Override
    public void showMainMenu(String message) {
        if (currentChatId != null) {
            sendTelegramMessageWithMainMenu(currentChatId, message);
        }
    }

    private void sendTelegramMessage(Long chatId, String text, String replyMarkup) {
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            StringBuilder postDataBuilder = new StringBuilder();
            postDataBuilder.append("chat_id=").append(chatId)
                    .append("&text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8.name()))
                    .append("&parse_mode=HTML");

            if (replyMarkup != null) {
                postDataBuilder.append("&reply_markup=").append(URLEncoder.encode(replyMarkup, StandardCharsets.UTF_8.name()));
            }

            String postData = postDataBuilder.toString();
            System.out.println(" Отправка сообщения в Telegram для chatId " + chatId + ": " +
                    text.substring(0, Math.min(50, text.length())) + "...");

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("❌ Ошибка отправки сообщения в Telegram: " + responseCode);
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String errorLine;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    System.err.println("Детали ошибки: " + errorResponse.toString());
                }
            } else {
                System.out.println("✅ Сообщение успешно отправлено");
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("❌ Ошибка при отправке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendTelegramMessageWithMainMenu(Long chatId, String text) {
        String keyboard = "{\"keyboard\":[[\"Старт\",\"Меню\"],[\"Помощь\",\"Выход\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessage(chatId, text, keyboard);
    }

    private void sendTelegramMessageWithMenu(Long chatId, String text) {
        String keyboard = "{\"keyboard\":[[\"Информация\",\"Создать напоминание\"],[\"Мои напоминания\",\"Назад\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessage(chatId, text, keyboard);
    }
}
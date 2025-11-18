package org;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class TelegramOutputProvider implements OutputProvider {
    private final String botToken;
    private Long currentChatId;

    public TelegramOutputProvider(String botToken) {
        this.botToken = botToken;
    }

    public void setCurrentChatId(Long chatId) {
        this.currentChatId = chatId;
    }

    @Override
    public void output(String message) {
        if (currentChatId != null) {
            sendTelegramMessage(currentChatId, message);
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

    private void sendTelegramMessage(Long chatId, String text) {
        sendTelegramMessage(chatId, text, null);
    }

    private void sendTelegramMessageWithMainMenu(Long chatId, String text) {
        String keyboard = "{\"keyboard\":[[\"start\",\"menu\"],[\"help\",\"exit\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessage(chatId, text, keyboard);
    }

    private void sendTelegramMessageWithMenu(Long chatId, String text) {
        String keyboard = "{\"keyboard\":[[\"1\",\"2\"],[\"3\",\"4\"]],\"resize_keyboard\":true,\"one_time_keyboard\":false}";
        sendTelegramMessage(chatId, text, keyboard);
    }

    private void sendTelegramMessage(Long chatId, String text, String replyMarkup) {
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            StringBuilder postDataBuilder = new StringBuilder();
            postDataBuilder.append("chat_id=").append(chatId)
                    .append("&text=").append(URLEncoder.encode(text, StandardCharsets.UTF_8.name()));

            if (replyMarkup != null) {
                postDataBuilder.append("&reply_markup=").append(URLEncoder.encode(replyMarkup, StandardCharsets.UTF_8.name()));
            }

            String postData = postDataBuilder.toString();

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = postData.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Ошибка отправки сообщения в Telegram: " + responseCode);

                // Читаем ошибку
                try (BufferedReader errorReader = new BufferedReader(
                        new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                    String errorLine;
                    StringBuilder errorResponse = new StringBuilder();
                    while ((errorLine = errorReader.readLine()) != null) {
                        errorResponse.append(errorLine);
                    }
                    System.err.println("Детали ошибки: " + errorResponse.toString());
                }
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
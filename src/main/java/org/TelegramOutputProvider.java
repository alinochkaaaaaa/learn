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

    }

    @Override
    public void showMessage(String message) {
        output(message);
    }

    private void sendTelegramMessage(Long chatId, String text) {
        try {
            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String postData = "chat_id=" + chatId + "&text=" + URLEncoder.encode(text, StandardCharsets.UTF_8.name());

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
            }

            conn.disconnect();

        } catch (Exception e) {
            System.err.println("Ошибка при отправке сообщения: " + e.getMessage());
        }
    }
}
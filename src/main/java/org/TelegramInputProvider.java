package org;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TelegramInputProvider implements InputProvider {
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    // потокобезопасная блокирующая очередь для хранения входящих сообщений
    private Long currentChatId;
    // ID текущего чата

    public void setCurrentChatId(Long chatId) {
        this.currentChatId = chatId;
    }

    public Long getCurrentChatId() {
        return currentChatId;
    }

    public void addInput(String input, Long chatId) {
        this.currentChatId = chatId; // добавляет сообщение в очередь
        inputQueue.offer(input); // неблокирующее добавление элемента в очередь
    }

    @Override
    public String getInput() {
        try {
            return inputQueue.take(); // ждет пока в очереди появится элемент
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    @Override
    public boolean hasInput() {
        return !inputQueue.isEmpty();
    } // есть ли сообщения в очереди
}
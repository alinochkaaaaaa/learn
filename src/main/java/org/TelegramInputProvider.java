package org;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TelegramInputProvider implements InputProvider {
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private Long currentChatId;

    public void setCurrentChatId(Long chatId) {
        this.currentChatId = chatId;
    }

    public Long getCurrentChatId() {
        return currentChatId;
    }

    public void addInput(String input, Long chatId) {
        this.currentChatId = chatId;
        inputQueue.offer(input);
    }

    @Override
    public String getInput() {
        try {
            return inputQueue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        }
    }

    @Override
    public boolean hasInput() {
        return !inputQueue.isEmpty();
    }
}
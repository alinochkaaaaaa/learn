package org;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TelegramInputProvider implements InputProvider {
    private final BlockingQueue<String> inputQueue = new LinkedBlockingQueue<>();
    private Long currentChatId;

    public void setCurrentChatId(Long chatId) {
        this.currentChatId = chatId;
        System.out.println(" Установлен currentChatId: " + chatId);
    }

    public Long getCurrentChatId() {
        return currentChatId;
    }

    public void addInput(String input, Long chatId) {
        this.currentChatId = chatId;
        boolean added = inputQueue.offer(input);
        if (added) {
            System.out.println(" Сообщение добавлено в очередь: \"" + input + "\" для chatId " + chatId);
            System.out.println(" Размер очереди: " + inputQueue.size());
        } else {
            System.err.println("❌ Не удалось добавить сообщение в очередь");
        }
    }

    @Override
    public String getInput() {
        try {
            String input = inputQueue.take();
            System.out.println(" Извлечение сообщения из очереди: \"" + input + "\"");
            return input;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("❌ Прерывание при получении ввода");
            return "";
        }
    }

    @Override
    public boolean hasInput() {
        boolean hasInput = !inputQueue.isEmpty();
        System.out.println(" Проверка очереди: " + (hasInput ? "есть сообщения" : "очередь пуста"));
        return hasInput;
    }
}
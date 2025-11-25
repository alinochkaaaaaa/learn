package org;

import java.time.LocalDateTime;

public class Reminder {
    private final long chatId;
    private final String message;
    private final LocalDateTime triggerTime;

    public Reminder(long chatId, String message, LocalDateTime triggerTime) {
        this.chatId = chatId;
        this.message = message;
        this.triggerTime = triggerTime;
    }

    public long getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTriggerTime() {
        return triggerTime;
    }
}
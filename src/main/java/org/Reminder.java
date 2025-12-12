package org;

import java.time.LocalDateTime;

public class Reminder {
    private final long chatId;
    private final String message;
    private final LocalDateTime triggerTime;
    private String id;

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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", chatId=" + chatId +
                ", message='" + message + '\'' +
                ", triggerTime=" + triggerTime +
                '}';
    }
}
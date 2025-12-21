package org;

import java.time.LocalDateTime;

public class Reminder {
    private final long chatId;
    private final String message;
    private final LocalDateTime triggerTime;
    private String id;
    private boolean isBirthdayReminder;
    private String contactName;
    private long contactId;

    public Reminder(long chatId, String message, LocalDateTime triggerTime) {
        this.chatId = chatId;
        this.message = message;
        this.triggerTime = triggerTime;
        this.isBirthdayReminder = false;
    }

    public Reminder(long chatId, String message, LocalDateTime triggerTime, boolean isBirthdayReminder, String contactName, long contactId) {
        this.chatId = chatId;
        this.message = message;
        this.triggerTime = triggerTime;
        this.isBirthdayReminder = isBirthdayReminder;
        this.contactName = contactName;
        this.contactId = contactId;
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

    public boolean isBirthdayReminder() {
        return isBirthdayReminder;
    }

    public String getContactName() {
        return contactName;
    }

    public long getContactId() {
        return contactId;
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "id='" + id + '\'' +
                ", chatId=" + chatId +
                ", message='" + message + '\'' +
                ", triggerTime=" + triggerTime +
                ", isBirthdayReminder=" + isBirthdayReminder +
                ", contactName='" + contactName + '\'' +
                ", contactId=" + contactId +
                '}';
    }
}
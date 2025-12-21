package org;

import java.time.LocalDate;

public class BirthdayContact {
    private String id;
    private long chatId;
    private long contactId;
    private String contactName;
    private LocalDate birthday;
    private boolean notificationsEnabled;
    private int daysBefore;
    private String customMessage;

    public BirthdayContact(long chatId, long contactId, String contactName) {
        this.chatId = chatId;
        this.contactId = contactId;
        this.contactName = contactName;
        this.notificationsEnabled = true;
        this.daysBefore = 1;
        this.customMessage = "Напоминание о дне рождения";
    }

    public BirthdayContact(long chatId, long contactId, String contactName, LocalDate birthday) {
        this.chatId = chatId;
        this.contactId = contactId;
        this.contactName = contactName;
        this.birthday = birthday;
        this.notificationsEnabled = true;
        this.daysBefore = 1;
        this.customMessage = "Напоминание о дне рождения";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getChatId() {
        return chatId;
    }

    public long getContactId() {
        return contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public LocalDate getBirthday() {
        return birthday;
    }

    public void setBirthday(LocalDate birthday) {
        this.birthday = birthday;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getDaysBefore() {
        return daysBefore;
    }

    public void setDaysBefore(int daysBefore) {
        this.daysBefore = daysBefore;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    @Override
    public String toString() {
        return "BirthdayContact{" +
                "id='" + id + '\'' +
                ", chatId=" + chatId +
                ", contactId=" + contactId +
                ", contactName='" + contactName + '\'' +
                ", birthday=" + birthday +
                ", notificationsEnabled=" + notificationsEnabled +
                ", daysBefore=" + daysBefore +
                ", customMessage='" + customMessage + '\'' +
                '}';
    }
}
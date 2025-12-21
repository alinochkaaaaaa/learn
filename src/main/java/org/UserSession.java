package org;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSession {
    private static final Map<Long, UserSession> userSessions = new HashMap<>();

    private final long chatId;
    private UserState state;
    private List<Reminder> remindersList;
    private Reminder selectedReminder;
    private BirthdayContact selectedBirthdayContact;

    private UserSession(long chatId) {
        this.chatId = chatId;
        this.state = UserState.MAIN_MENU;
    }

    public static UserSession getSession(long chatId) {
        return userSessions.computeIfAbsent(chatId, UserSession::new);
    }

    public static void clearSession(long chatId) {
        userSessions.remove(chatId);
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public List<Reminder> getRemindersList() {
        return remindersList;
    }

    public void setRemindersList(List<Reminder> remindersList) {
        this.remindersList = remindersList;
        System.out.println("Список напоминаний установлен для пользователя " + chatId + ": " +
                (remindersList != null ? remindersList.size() : 0) + " напоминаний");
    }

    public Reminder getSelectedReminder() {
        return selectedReminder;
    }

    public void setSelectedReminder(Reminder selectedReminder) {
        this.selectedReminder = selectedReminder;
        System.out.println("Выбрано напоминание для пользователя " + chatId + ": " +
                (selectedReminder != null ? selectedReminder.getMessage() : "null"));
    }

    public BirthdayContact getSelectedBirthdayContact() {
        return selectedBirthdayContact;
    }

    public void setSelectedBirthdayContact(BirthdayContact selectedBirthdayContact) {
        this.selectedBirthdayContact = selectedBirthdayContact;
        System.out.println("Выбран контакт для пользователя " + chatId + ": " +
                (selectedBirthdayContact != null ? selectedBirthdayContact.getContactName() : "null"));
    }

    public void clearSelectedReminder() {
        this.selectedReminder = null;
    }

    public void clearSelectedBirthdayContact() {
        this.selectedBirthdayContact = null;
    }

    public long getChatId() {
        return chatId;
    }

    @Deprecated
    public static Reminder getSelectedReminder(long chatId) {
        return getSession(chatId).getSelectedReminder();
    }

    @Deprecated
    public static void clearSelectedReminder(long chatId) {
        getSession(chatId).clearSelectedReminder();
    }

    public static void printAllSessions() {
        System.out.println("Активные сессии пользователей:");
        for (UserSession session : userSessions.values()) {
            System.out.println("  • " + session.getChatId() + " -> " + session.getState());
        }
    }
}
package org;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserSession {
    private static final Map<Long, UserState> sessions = new HashMap<>();
    private static final Map<Long, List<Reminder>> remindersLists = new HashMap<>();
    private static final Map<Long, Reminder> selectedReminders = new HashMap<>();

    public static void setState(long chatId, UserState state) {
        sessions.put(chatId, state);
        System.out.println("Состояние пользователя " + chatId + " установлено: " + state);
    }

    public static UserState getState(long chatId) {
        UserState state = sessions.getOrDefault(chatId, UserState.MAIN_MENU);
        System.out.println("Состояние пользователя " + chatId + ": " + state);
        return state;
    }

    public static void clearState(long chatId) {
        sessions.remove(chatId);
        System.out.println("Состояние пользователя " + chatId + " очищено");
    }

    public static void setRemindersList(long chatId, List<Reminder> reminders) {
        remindersLists.put(chatId, reminders);
        System.out.println("Список напоминаний установлен для пользователя " + chatId + ": " + reminders.size() + " напоминаний");
    }

    public static List<Reminder> getRemindersList(long chatId) {
        return remindersLists.get(chatId);
    }

    public static void setSelectedReminder(long chatId, Reminder reminder) {
        selectedReminders.put(chatId, reminder);
        System.out.println("Выбрано напоминание для пользователя " + chatId + ": " + reminder.getMessage());
    }

    public static Reminder getSelectedReminder(long chatId) {
        return selectedReminders.get(chatId);
    }

    public static void clearSelectedReminder(long chatId) {
        selectedReminders.remove(chatId);
        System.out.println("Выбранное напоминание очищено для пользователя " + chatId);
    }

    public static void printAllSessions() {
        System.out.println("Активные сессии пользователей:");
        for (Map.Entry<Long, UserState> entry : sessions.entrySet()) {
            System.out.println("  • " + entry.getKey() + " -> " + entry.getValue());
        }
    }
}
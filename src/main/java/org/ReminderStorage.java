package org;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReminderStorage {
    private static final List<Reminder> reminders = new ArrayList<>();

    public static void add(Reminder reminder) {
        reminders.add(reminder);
    }

    public static List<Reminder> getAllByChatId(long chatId) {
        return reminders.stream()
                .filter(r -> r.getChatId() == chatId)
                .collect(Collectors.toList());
    }

    public static List<Reminder> getAll() {
        return new ArrayList<>(reminders);
    }

    public static void clear() {
        reminders.clear();
    }
}
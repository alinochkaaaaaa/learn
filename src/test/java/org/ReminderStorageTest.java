package org;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class ReminderStorageTest {

    @BeforeEach
    void setUp() {
        ReminderStorage.clear();
    }

    @Test
    void testAddAndGetReminder() {
        Reminder reminder = new Reminder(123L, "Test message", LocalDateTime.now());
        ReminderStorage.add(reminder);

        List<Reminder> reminders = ReminderStorage.getAllByChatId(123L);
        assertEquals(1, reminders.size());
        assertEquals(reminder, reminders.get(0));
    }

    @Test
    void testGetAllByChatIdMultipleUsers() {
        Reminder reminder1 = new Reminder(123L, "User 1 message", LocalDateTime.now());
        Reminder reminder2 = new Reminder(456L, "User 2 message", LocalDateTime.now());

        ReminderStorage.add(reminder1);
        ReminderStorage.add(reminder2);

        List<Reminder> user1Reminders = ReminderStorage.getAllByChatId(123L);
        List<Reminder> user2Reminders = ReminderStorage.getAllByChatId(456L);

        assertEquals(1, user1Reminders.size());
        assertEquals(1, user2Reminders.size());
        assertEquals("User 1 message", user1Reminders.get(0).getMessage());
        assertEquals("User 2 message", user2Reminders.get(0).getMessage());
    }

    @Test
    void testGetAllReminders() {
        Reminder reminder1 = new Reminder(123L, "Message 1", LocalDateTime.now());
        Reminder reminder2 = new Reminder(456L, "Message 2", LocalDateTime.now());

        ReminderStorage.add(reminder1);
        ReminderStorage.add(reminder2);

        List<Reminder> allReminders = ReminderStorage.getAll();
        assertEquals(2, allReminders.size());
    }

    @Test
    void testClearStorage() {
        Reminder reminder = new Reminder(123L, "Test message", LocalDateTime.now());
        ReminderStorage.add(reminder);

        assertEquals(1, ReminderStorage.getAll().size());

        ReminderStorage.clear();
        assertEquals(0, ReminderStorage.getAll().size());
    }
}
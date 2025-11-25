package org;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ReminderTest {

    @Test
    void testReminderCreation() {
        LocalDateTime triggerTime = LocalDateTime.of(2024, 1, 1, 10, 30);
        Reminder reminder = new Reminder(123L, "Test message", triggerTime);

        assertEquals(123L, reminder.getChatId());
        assertEquals("Test message", reminder.getMessage());
        assertEquals(triggerTime, reminder.getTriggerTime());
    }

    @Test
    void testReminderGetters() {
        LocalDateTime now = LocalDateTime.now();
        Reminder reminder = new Reminder(456L, "Another message", now);

        assertEquals(456L, reminder.getChatId());
        assertEquals("Another message", reminder.getMessage());
        assertEquals(now, reminder.getTriggerTime());
    }
}
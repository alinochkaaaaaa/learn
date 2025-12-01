package org;

import org.junit.Test;
import java.time.LocalDateTime;
import static org.junit.Assert.*;

public class ReminderTest {

    @Test
    public void testReminderCreation() {
        // Тест 1: Создание напоминания
        LocalDateTime triggerTime = LocalDateTime.of(2025, 12, 1, 15, 30);
        Reminder reminder = new Reminder(123456789L, "Тестовое напоминание", triggerTime);

        assertEquals(123456789L, reminder.getChatId());
        assertEquals("Тестовое напоминание", reminder.getMessage());
        assertEquals(triggerTime, reminder.getTriggerTime());
    }

    @Test
    public void testReminderEquality() {
        // Тест 2: Проверка равенства напоминаний
        LocalDateTime time1 = LocalDateTime.now();
        LocalDateTime time2 = time1.plusMinutes(5);

        Reminder reminder1 = new Reminder(123L, "Сообщение 1", time1);
        Reminder reminder2 = new Reminder(123L, "Сообщение 1", time1);
        Reminder reminder3 = new Reminder(456L, "Сообщение 1", time1);
        Reminder reminder4 = new Reminder(123L, "Сообщение 2", time1);
        Reminder reminder5 = new Reminder(123L, "Сообщение 1", time2);

        // Напоминания должны быть равны только если все поля совпадают
        // Так как нет equals метода, сравниваем поля напрямую
        assertEquals(reminder1.getChatId(), reminder2.getChatId());
        assertEquals(reminder1.getMessage(), reminder2.getMessage());
        assertEquals(reminder1.getTriggerTime(), reminder2.getTriggerTime());

        assertNotEquals(reminder1.getChatId(), reminder3.getChatId());
        assertNotEquals(reminder1.getMessage(), reminder4.getMessage());
        assertNotEquals(reminder1.getTriggerTime(), reminder5.getTriggerTime());
    }

    @Test
    public void testReminderWithSpecialCharacters() {
        // Тест 3: Специальные символы в сообщении
        String messageWithSpecialChars = "Напоминание с 🎉 эмодзи и #хештегом!";
        Reminder reminder = new Reminder(123L, messageWithSpecialChars, LocalDateTime.now());

        assertEquals(messageWithSpecialChars, reminder.getMessage());
    }

    @Test
    public void testReminderToString() {
        // Тест 4: Проверка toString метода
        LocalDateTime time = LocalDateTime.of(2025, 12, 1, 10, 0);
        Reminder reminder = new Reminder(999L, "Test reminder", time);

        String toStringResult = reminder.toString();
        assertNotNull(toStringResult);
        assertTrue(toStringResult.contains("999"));
        assertTrue(toStringResult.contains("Test reminder"));
        assertTrue(toStringResult.contains("2025-12-01T10:00"));
    }

    @Test
    public void testReminderWithPastTime() {
        // Негативный тест 2: Время в прошлом (должно создаваться без проблем)
        LocalDateTime pastTime = LocalDateTime.now().minusDays(1);
        Reminder reminder = new Reminder(123L, "Прошлое напоминание", pastTime);

        assertEquals(pastTime, reminder.getTriggerTime());
        assertTrue("Время должно быть в прошлом",
                reminder.getTriggerTime().isBefore(LocalDateTime.now()));
    }

    @Test
    public void testReminderWithZeroChatId() {
        // Тест: ChatId = 0
        Reminder reminder = new Reminder(0L, "Test", LocalDateTime.now());
        assertEquals(0L, reminder.getChatId());
    }

    @Test
    public void testReminderWithVeryLongMessage() {
        // Тест: Очень длинное сообщение
        String longMessage = "Очень ".repeat(100) + "длинное сообщение";
        Reminder reminder = new Reminder(123L, longMessage, LocalDateTime.now());

        assertEquals(longMessage, reminder.getMessage());
        assertTrue(reminder.getMessage().length() > 100);
    }
}
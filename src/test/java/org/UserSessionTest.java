package org;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @Test
    @DisplayName("Создание и получение сессии пользователя")
    void testGetSession() {
        long chatId = 987654321L;

        // Получаем сессию (должна быть создана)
        UserSession session1 = UserSession.getSession(chatId);
        assertNotNull(session1);
        assertEquals(chatId, session1.getChatId());
        assertEquals(UserState.MAIN_MENU, session1.getState());

        // Получаем ту же сессию второй раз
        UserSession session2 = UserSession.getSession(chatId);
        assertSame(session1, session2);

        // Очищаем сессию
        UserSession.clearSession(chatId);
    }

    @Test
    @DisplayName("Изменение состояния пользователя")
    void testUserStateChanges() {
        long chatId = 111222333L;
        UserSession session = UserSession.getSession(chatId);

        // Проверяем начальное состояние
        assertEquals(UserState.MAIN_MENU, session.getState());

        // Меняем состояние
        session.setState(UserState.CREATING_REMINDER);
        assertEquals(UserState.CREATING_REMINDER, session.getState());

        // Меняем еще раз
        session.setState(UserState.VIEWING_REMINDERS);
        assertEquals(UserState.VIEWING_REMINDERS, session.getState());

        UserSession.clearSession(chatId);
    }

    @Test
    @DisplayName("Установка и получение списка напоминаний")
    void testRemindersListManagement() {
        long chatId = 555666777L;
        UserSession session = UserSession.getSession(chatId);

        // Создаем тестовые напоминания
        Reminder reminder1 = new Reminder(chatId, "Тест 1",
                LocalDateTime.now().plusHours(1));
        Reminder reminder2 = new Reminder(chatId, "Тест 2",
                LocalDateTime.now().plusHours(2));

        List<Reminder> reminders = Arrays.asList(reminder1, reminder2);

        // Устанавливаем список
        session.setRemindersList(reminders);

        // Проверяем получение
        List<Reminder> retrieved = session.getRemindersList();
        assertNotNull(retrieved);
        assertEquals(2, retrieved.size());
        assertEquals("Тест 1", retrieved.get(0).getMessage());
        assertEquals("Тест 2", retrieved.get(1).getMessage());

        UserSession.clearSession(chatId);
    }

    @Test
    @DisplayName("Выбор и очистка напоминания")
    void testSelectedReminderManagement() {
        long chatId = 888999000L;
        UserSession session = UserSession.getSession(chatId);

        Reminder reminder = new Reminder(chatId, "Тестовое напоминание",
                LocalDateTime.now().plusHours(1));

        // Устанавливаем выбранное напоминание
        session.setSelectedReminder(reminder);

        // Проверяем получение
        Reminder selected = session.getSelectedReminder();
        assertNotNull(selected);
        assertEquals("Тестовое напоминание", selected.getMessage());
        assertEquals(chatId, selected.getChatId());

        // Очищаем
        session.clearSelectedReminder();
        assertNull(session.getSelectedReminder());

        UserSession.clearSession(chatId);
    }

    @Test
    @DisplayName("Очистка сессии пользователя")
    void testClearSession() {
        long chatId1 = 100200300L;
        long chatId2 = 400500600L;

        // Создаем две сессии
        UserSession session1 = UserSession.getSession(chatId1);
        UserSession session2 = UserSession.getSession(chatId2);

        // Проверяем, что сессии созданы
        assertNotNull(session1);
        assertNotNull(session2);

        // Очищаем одну сессию
        UserSession.clearSession(chatId1);

        // Создаем новую сессию для того же chatId (должна быть новая)
        UserSession newSession1 = UserSession.getSession(chatId1);
        assertNotSame(session1, newSession1);

        // Вторая сессия должна остаться той же
        UserSession sameSession2 = UserSession.getSession(chatId2);
        assertSame(session2, sameSession2);

        // Очищаем оставшиеся сессии
        UserSession.clearSession(chatId1);
        UserSession.clearSession(chatId2);
    }
}
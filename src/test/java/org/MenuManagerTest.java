package org;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.time.LocalDateTime;

class MenuManagerTest {
    private TestOutputProvider testOutputProvider;
    private TestInputProvider testInputProvider;
    private TestReminderScheduler testReminderScheduler;
    private MenuManager menuManager;

    static class TestOutputProvider implements OutputProvider {
        private StringBuilder output = new StringBuilder();
        private String lastMenuMessage;

        @Override
        public void output(String message) {
            output.append(message).append("\n");
            System.out.println("Test Output: " + message);
        }

        @Override
        public void outputMenu(String menu) {
            lastMenuMessage = menu;
            output.append("[MENU]: ").append(menu).append("\n");
            System.out.println("Test Menu: " + menu);
        }

        @Override
        public void showMessage(String message) {
            output(message);
        }

        @Override
        public void showMainMenu(String message) {
            output("[MAIN MENU]: " + message);
        }

        @Override
        public void setCurrentChatId(Long chatId) {
            // Не используется в тестах
        }

        public String getOutput() {
            return output.toString();
        }

        public boolean contains(String text) {
            return output.toString().contains(text);
        }

        public void clear() {
            output = new StringBuilder();
            lastMenuMessage = null;
        }
    }

    static class TestInputProvider implements InputProvider {
        private String input;

        @Override
        public String getInput() {
            return input;
        }

        @Override
        public boolean hasInput() {
            return input != null && !input.isEmpty();
        }

        public void setInput(String input) {
            this.input = input;
        }
    }

    static class TestReminderScheduler extends ReminderScheduler {
        private int scheduledCount = 0;

        public TestReminderScheduler(OutputProvider outputProvider) {
            super(outputProvider);
        }

        @Override
        public void schedule(Reminder reminder) {
            scheduledCount++;
            System.out.println("Test: Напоминание запланировано: " + reminder.getMessage());
        }

        public int getScheduledCount() {
            return scheduledCount;
        }
    }

    @BeforeEach
    void setUp() {
        testOutputProvider = new TestOutputProvider();
        testInputProvider = new TestInputProvider();
        testReminderScheduler = new TestReminderScheduler(testOutputProvider);
        menuManager = new MenuManager(testInputProvider, testOutputProvider, testReminderScheduler);

        UserSession.clearState(12345L);
    }

    @Test
    @DisplayName("Тест 1: Показать меню")
    void testShowMenu() {
        menuManager.showMenu();

        String output = testOutputProvider.getOutput();
        assertNotNull(output);
        assertTrue(output.contains("Меню") || output.contains("меню"));
    }

    @Test
    @DisplayName("Тест 2: Обработка выбора 'Информация'")
    void testHandleMenuSelection_Information() {
        long chatId = 12345L;
        String selection = "Информация";

        menuManager.handleMenuSelection(selection, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Информация о боте") || output.contains("бот"));
        assertTrue(testOutputProvider.contains("меню") || testOutputProvider.contains("Меню"));
    }

    @Test
    @DisplayName("Тест 3: Обработка выбора 'Создать напоминание'")
    void testHandleMenuSelection_CreateReminder() {
        long chatId = 12345L;
        String selection = "Создать напоминание";

        menuManager.handleMenuSelection(selection, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Создание") || output.contains("напоминани"));
        assertTrue(output.contains("напомни [дата] [время] [сообщение]"));

        // Проверяем, что состояние изменилось
        assertEquals(UserState.CREATING_REMINDER, UserSession.getState(chatId));
    }

    @Test
    @DisplayName("Тест 4: Обработка выбора напоминания по номеру")
    void testHandleReminderSelection_ValidNumber() {
        long chatId = 12345L;
        String selection = "1";

        // Создаем тестовое напоминание
        Reminder testReminder = new Reminder(chatId, "Тестовое напоминание",
                LocalDateTime.now().plusHours(1));
        List<Reminder> reminders = Arrays.asList(testReminder);
        UserSession.setRemindersList(chatId, reminders);
        UserSession.setState(chatId, UserState.VIEWING_REMINDERS);

        menuManager.handleReminderSelection(selection, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Напоминание") || output.contains("Дата и время"));
        assertTrue(output.contains("Выберите действие") || output.contains("1. Редактировать"));

        // Проверяем, что состояние изменилось
        assertEquals(UserState.EDITING_REMINDER, UserSession.getState(chatId));

        // Проверяем, что напоминание сохранено в сессии
        assertNotNull(UserSession.getSelectedReminder(chatId));
        assertEquals("Тестовое напоминание", UserSession.getSelectedReminder(chatId).getMessage());

        UserSession.clearState(chatId);
        UserSession.clearSelectedReminder(chatId);
    }
}
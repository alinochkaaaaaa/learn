package org;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

class CommandProcessorTest {
    private TestOutputProvider testOutputProvider;
    private TestInputProvider testInputProvider;
    private TestMenuManager testMenuManager;
    private TestReminderScheduler testReminderScheduler;
    private CommandProcessor commandProcessor;

    static class TestOutputProvider implements OutputProvider {
        private StringBuilder output = new StringBuilder();

        @Override
        public void output(String message) {
            output.append(message).append("\n");
            System.out.println("Test Output: " + message);
        }

        @Override
        public void outputMenu(String menu) {
            output.append("[MENU]: ").append(menu).append("\n");
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
        }
    }

    static class TestInputProvider implements InputProvider {
        @Override
        public String getInput() {
            return "";
        }

        @Override
        public boolean hasInput() {
            return false;
        }
    }

    static class TestMenuManager extends MenuManager {
        private int showMenuCalled = 0;
        private int showRemindersMenuCalled = 0;
        private String lastSelection;
        private long lastChatId;

        public TestMenuManager(InputProvider inputProvider, OutputProvider outputProvider,
                               ReminderScheduler reminderScheduler) {
            super(inputProvider, outputProvider, reminderScheduler);
        }

        @Override
        public void showMenu() {
            showMenuCalled++;
            super.showMenu();
        }

        @Override
        public void showRemindersMenu(long chatId) {
            showRemindersMenuCalled++;
            lastChatId = chatId;
            super.showRemindersMenu(chatId);
        }

        @Override
        public void handleMenuSelection(String selection, long chatId) {
            lastSelection = selection;
            lastChatId = chatId;
            super.handleMenuSelection(selection, chatId);
        }

        @Override
        public void handleReminderSelection(String selection, long chatId) {
            lastSelection = selection;
            lastChatId = chatId;
            super.handleReminderSelection(selection, chatId);
        }

        public int getShowMenuCalled() {
            return showMenuCalled;
        }

        public int getShowRemindersMenuCalled() {
            return showRemindersMenuCalled;
        }

        public String getLastSelection() {
            return lastSelection;
        }

        public long getLastChatId() {
            return lastChatId;
        }
    }

    static class TestReminderScheduler extends ReminderScheduler {
        public TestReminderScheduler(OutputProvider outputProvider) {
            super(outputProvider);
        }

        @Override
        public void schedule(Reminder reminder) {
            System.out.println("Test: Напоминание запланировано: " + reminder.getMessage());
        }
    }

    @BeforeEach
    void setUp() {
        testOutputProvider = new TestOutputProvider();
        testInputProvider = new TestInputProvider();
        testReminderScheduler = new TestReminderScheduler(testOutputProvider);
        testMenuManager = new TestMenuManager(testInputProvider, testOutputProvider, testReminderScheduler);
        commandProcessor = new CommandProcessor(testInputProvider, testOutputProvider,
                testMenuManager, testReminderScheduler);

        UserSession.clearState(12345L);
        UserSession.clearState(67890L);
    }

    @Test
    @DisplayName("Тест 1: Обработка команды /start")
    void testProcessCommand_Start() {
        long chatId = 12345L;
        String command = "/start";

        commandProcessor.processCommand(command, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Добро пожаловать") || output.contains("Главное меню"));

        // Проверяем, что состояние установлено
        assertEquals(UserState.MAIN_MENU, UserSession.getState(chatId));
    }

    @Test
    @DisplayName("Тест 2: Обработка команды /help")
    void testProcessCommand_Help() {
        long chatId = 12345L;
        String command = "/help";
        UserSession.setState(chatId, UserState.MAIN_MENU);

        commandProcessor.processCommand(command, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Справка") || output.contains("напомни [дата]"));
        assertTrue(output.contains("Примеры") || output.contains("через 5 минут"));
    }

    @Test
    @DisplayName("Тест 3: Создание напоминания с командой 'назад'")
    void testHandleCreateReminder_BackCommand() {
        long chatId = 12345L;
        String command = "назад";
        UserSession.setState(chatId, UserState.CREATING_REMINDER);

        commandProcessor.processCommand(command, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Отмена") || output.contains("назад"));

        // Проверяем, что showMenu был вызван
        assertTrue(testMenuManager.getShowMenuCalled() > 0);

        // Проверяем, что состояние изменилось
        assertEquals(UserState.IN_MENU, UserSession.getState(chatId));
    }

    @Test
    @DisplayName("Тест 4: Обработка неизвестной команды в главном меню")
    void testHandleMainMenu_UnknownCommand() {
        long chatId = 12345L;
        String command = "неизвестная_команда";
        UserSession.setState(chatId, UserState.MAIN_MENU);

        commandProcessor.processCommand(command, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Неизвестная") || output.contains("кнопки меню"));
    }

    @Test
    @DisplayName("Тест 5: Обработка выхода из бота")
    void testHandleMainMenu_ExitCommand() {
        long chatId = 12345L;
        String command = "выход";
        UserSession.setState(chatId, UserState.MAIN_MENU);

        commandProcessor.processCommand(command, chatId);

        String output = testOutputProvider.getOutput();
        assertTrue(output.contains("Завершение") || output.contains("выход"));

        // Проверяем, что isRunning установлен в false
        assertFalse(commandProcessor.isRunning());
    }
}
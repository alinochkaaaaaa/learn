package org;

public class SimpleUnitTests {

    public static class TestResult {
        public int passed = 0;
        public int failed = 0;

        public void assertTrue(boolean condition, String message) {
            if (condition) {
                System.out.println("PASS: " + message);
                passed++;
            } else {
                System.out.println("FAIL: " + message);
                failed++;
            }
        }

        public void assertEquals(Object expected, Object actual, String message) {
            boolean condition = (expected == null && actual == null) ||
                    (expected != null && expected.equals(actual));
            if (condition) {
                System.out.println("PASS: " + message + " (expected: " + expected + ", actual: " + actual + ")");
                passed++;
            } else {
                System.out.println("FAIL: " + message + " (expected: " + expected + ", actual: " + actual + ")");
                failed++;
            }
        }

        public void printSummary() {
            System.out.println("\n TEST SUMMARY:");
            System.out.println("Passed: " + passed);
            System.out.println("Failed: " + failed);
        }
    }

    // Тестовый InputProvider
    static class TestInput implements InputProvider {
        private String input;

        public void setInput(String input) {
            this.input = input;
        }

        @Override
        public String getInput() {
            return input;
        }

        @Override
        public boolean hasInput() {
            return input != null;
        }
    }

    // Тестовый OutputProvider
    static class TestOutput implements OutputProvider {
        public String lastMessage;
        public String lastMenu;
        public String lastMainMenu;

        @Override
        public void output(String message) {
            this.lastMessage = message;
        }

        @Override
        public void outputMenu(String menu) {
            this.lastMenu = menu;
        }

        @Override
        public void showMessage(String message) {
            this.lastMessage = message;
        }

        @Override
        public void showMainMenu(String message) {
            this.lastMainMenu = message;
        }

        public void reset() {
            lastMessage = null;
            lastMenu = null;
            lastMainMenu = null;
        }
    }

    // ТЕСТ 1: Проверка отображения меню
    public static void testMenuDisplay(TestResult result) {
        System.out.println("\n Тест 1: Отображение меню");

        TestInput input = new TestInput();
        TestOutput output = new TestOutput();
        MenuManager menuManager = new MenuManager(input, output);

        // Показываем меню
        menuManager.showMenu();

        // Проверяем, что меню содержит нужные пункты
        if (output.lastMenu != null) {
            result.assertTrue(output.lastMenu.contains("\uD83D\uDCCB Меню:"), "Меню должно содержать заголовок");
            result.assertTrue(output.lastMenu.contains("1 - Информация о боте"), "Меню должно содержать пункт 1");
            result.assertTrue(output.lastMenu.contains("2 - Текущее время"), "Меню должно содержать пункт 2");
            result.assertTrue(output.lastMenu.contains("3 - Текущая дата"), "Меню должно содержать пункт 3");
            result.assertTrue(output.lastMenu.contains("4 - Вернуться назад"), "Меню должно содержать пункт 4");
        }
    }

    // ТЕСТ 2: Проверка обработки пунктов меню
    public static void testMenuOptions(TestResult result) {
        System.out.println("\n Тест 2: Обработка пунктов меню");

        TestInput input = new TestInput();
        TestOutput output = new TestOutput();
        MenuManager menuManager = new MenuManager(input, output);

        // (Информация о боте)
        menuManager.processMenuChoice("1");
        result.assertTrue(output.lastMessage.contains("Информация о боте"),
                "Пункт 1 должен показывать информацию о боте");

        // (Возврат назад)
        output.reset();
        menuManager.processMenuChoice("4");
        result.assertTrue(output.lastMessage.contains("Возврат в главное меню"),
                "Пункт 4 должен возвращать в главное меню");
    }

    // ТЕСТ 3: Проверка неизвестной команды
    public static void testUnknownCommand(TestResult result) {
        System.out.println("\n Тест 3: Обработка неизвестной команды");

        TestInput input = new TestInput();
        TestOutput output = new TestOutput();
        MenuManager menuManager = new MenuManager(input, output);
        CommandProcessor processor = new CommandProcessor(input, output, menuManager);

        // Выполняем неизвестную команду
        processor.processCommand("unknown_command");

        // Проверяем результаты
        result.assertTrue(output.lastMessage.contains("Неизвестная команда"),
                "Неизвестная команда должна показывать ошибку");
        result.assertTrue(output.lastMessage.contains("unknown_command"),
                "Сообщение об ошибке должно содержать команду");
    }


    public static void main(String[] args) {
        System.out.println("🚀 Запуск Unit-тестов для Telegram бота\n");

        TestResult result = new TestResult();

        testMenuDisplay(result);
        testMenuOptions(result);
        testUnknownCommand(result);

        // Выводим итоги
        result.printSummary();
    }
}
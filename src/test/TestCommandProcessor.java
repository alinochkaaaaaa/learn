package test;

// TestCommandProcessor.java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TestCommandProcessor {

    @Test
    public void testCommandProcessorCreation() {
        TestInputProvider inputProvider = new TestInputProvider();
        TestOutputProvider outputProvider = new TestOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);

        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);
        assertNotNull(processor, "CommandProcessor должен создаваться");
    }

    @Test
    public void testStartCommand() {
        TestInputProvider inputProvider = new TestInputProvider();
        TestOutputProvider outputProvider = new TestOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);

        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);
        processor.processCommand("start");

        String lastMessage = outputProvider.getLastMessage();
        assertTrue(lastMessage.contains("запустили консольного бота"),
                "Команда start должна выводить приветствие");
    }

    @Test
    public void testHelpCommand() {
        TestInputProvider inputProvider = new TestInputProvider();
        TestOutputProvider outputProvider = new TestOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);

        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);
        processor.processCommand("help");

        String lastMessage = outputProvider.getLastMessage();
        assertTrue(lastMessage.contains("Доступные команды"),
                "Команда help должна показывать список команд");
    }

    @Test
    public void testExitCommand() {
        TestInputProvider inputProvider = new TestInputProvider();
        TestOutputProvider outputProvider = new TestOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);

        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);

        assertTrue(processor.isRunning(), "Изначально процессор должен быть активен");
        processor.processCommand("exit");
        assertFalse(processor.isRunning(), "После команды exit процессор должен остановиться");
    }

    @Test
    public void testUnknownCommand() {
        TestInputProvider inputProvider = new TestInputProvider();
        TestOutputProvider outputProvider = new TestOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);

        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);
        processor.processCommand("unknown_command");

        String lastMessage = outputProvider.getLastMessage();
        assertTrue(lastMessage.contains("Неизвестная команда"),
                "Неизвестная команда должна обрабатываться корректно");
    }

    @Test
    public void testMenuCommand() {
        TestInputProvider inputProvider = new TestInputProvider();
        TestOutputProvider outputProvider = new TestOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);

        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);
        processor.processCommand("menu");

        // MenuManager должен был получить вызов showMenu()
        assertNotNull(outputProvider.getLastMenu(),
                "Команда menu должна вызывать отображение меню");
    }
}
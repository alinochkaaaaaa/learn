// Main.java
public class Main {
    private boolean isRunning = true;
    private ConsoleInputProvider inputProvider;
    private OutputProvider outputProvider;
    private MenuManager menuManager;
    private CommandProcessor commandProcessor;

    public static void main(String[] args) {
        new Main().start();
    }

    public void start() {
        // Инициализация зависимостей
        inputProvider = new ConsoleInputProvider();
        outputProvider = new ConsoleOutputProvider();
        menuManager = new MenuManager(inputProvider, outputProvider);
        commandProcessor = new CommandProcessor(inputProvider, outputProvider, menuManager);

        outputProvider.showMessage("Бот запущен!");
        outputProvider.showMessage("Введите help для просмотра команд");

        while (isRunning) {
            outputProvider.showMessage("\n> ");
            String input = inputProvider.getInput();

            if (!input.isEmpty()) {
                commandProcessor.processCommand(input);
                // Обновляем состояние isRunning из CommandProcessor
                isRunning = commandProcessor.isRunning();
            }
        }

        inputProvider.close();
        outputProvider.showMessage("Бот завершил работу");
    }
}
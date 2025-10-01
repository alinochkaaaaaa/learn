// CommandProcessor.java
public class CommandProcessor {
    private InputProvider inputProvider;
    private OutputProvider outputProvider;
    private MenuManager menuManager;
    private boolean isRunning;

    public CommandProcessor(InputProvider inputProvider, OutputProvider outputProvider,
                            MenuManager menuManager) {
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
        this.menuManager = menuManager;
        this.isRunning = true;
    }

    public void processCommand(String command) {
        switch (command) {
            case "start":
                handleStart();
                break;
            case "help":
                handleHelp();
                break;
            case "menu":
                handleMenu();
                break;
            case "exit":
                handleExit();
                break;
            default:
                handleUnknownCommand(command);
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    private void handleStart() {
        outputProvider.showMessage("Вы запустили консольного бота.");
        outputProvider.showMessage("Для просмотра команд введите help");
    }

    private void handleHelp() {
        outputProvider.showMessage("Доступные команды:");
        outputProvider.showMessage("start - Начать работу с ботом");
        outputProvider.showMessage("help - Показать справку по командам");
        outputProvider.showMessage("menu - Показать меню");
        outputProvider.showMessage("exit - Завершить работу");
    }

    private void handleMenu() {
        menuManager.showMenu();
    }

    private void handleExit() {
        isRunning = false;
        outputProvider.showMessage("Завершение работы...");
    }

    private void handleUnknownCommand(String command) {
        outputProvider.showMessage("Неизвестная команда: " + command);
        outputProvider.showMessage("Введите help для просмотра доступных команд");
    }
}
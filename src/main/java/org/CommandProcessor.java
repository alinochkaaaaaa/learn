package org;

public class CommandProcessor {
    private final OutputProvider outputProvider;
    private final MenuManager menuManager;
    private boolean isRunning = true;

    public CommandProcessor(InputProvider inputProvider, OutputProvider outputProvider, MenuManager menuManager) {
        this.outputProvider = outputProvider;
        this.menuManager = menuManager;
    }

    public void processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        switch (command.toLowerCase()) {
            case "/start":
            case "start":
                outputProvider.output("\uD83D\uDE80 Вы запустили консольного бота!");
                showMainMenu();
                break;
            case "/help":
            case "help":
                outputProvider.output("Информация о боте: Это телеграмм бот для демонстрации планирования задач.");
                showMainMenu();
                break;
            case "/menu":
            case "menu":
                menuManager.showMenu();
                break;
            case "/exit":
            case "exit":
                outputProvider.output("\uD83D\uDC4B Выход из программы...");
                isRunning = false;
                break;
            case "1":
                outputProvider.output("Информация о боте: Это телеграмм бот для планирования.");
                menuManager.showMenu();
                break;
            case "2":
                outputProvider.output("Текущее время: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                menuManager.showMenu();
                break;
            case "3":
                outputProvider.output("Текущая дата: " + java.time.LocalDate.now());
                menuManager.showMenu();
                break;
            case "4":
                showMainMenu();
                break;
            default:
                outputProvider.output("Неизвестная команда: " + command + ". Используйте кнопки меню.");
                showMainMenu();
                break;
        }
    }

    private void showMainMenu() {
        String mainMenu = "Главное меню - выберите действие:";
        outputProvider.showMainMenu(mainMenu);
    }

    public boolean isRunning() {
        return isRunning;
    }
}
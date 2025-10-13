package org;

public class CommandProcessor {
    private InputProvider inputProvider;
    private OutputProvider outputProvider;
    private MenuManager menuManager;
    private boolean isRunning = true;
    private boolean inMenuMode = false;

    public CommandProcessor(InputProvider inputProvider, OutputProvider outputProvider, MenuManager menuManager) {
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
        this.menuManager = menuManager;
    }

    public void processCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return;
        }

        // Если мы в режиме меню, обрабатываем выбор меню
        if (inMenuMode) {
            handleMenuCommand(command);
            return;
        }

        // Обработка основных команд
        switch (command.toLowerCase()) {
            case "start":
                outputProvider.output("Вы запустили консольного бота. Введите 'help' для списка команд.");
                break;
            case "help":
                outputProvider.output("""
                        Доступные команды:
                        - start: Запуск бота
                        - help: Справка
                        - menu: Показать меню
                        - exit: Выход""");
                break;
            case "exit":
                outputProvider.output("Выход из программы...");
                isRunning = false;
                break;
            case "menu":
                outputProvider.output("Открытие меню...");
                menuManager.showMenu(); // Только показываем меню
                inMenuMode = true;      // Переходим в режим меню для обработки следующего ввода
                break;
            default:
                outputProvider.output("Неизвестная команда: " + command + ". Введите 'help' для списка доступных команд.");
                break;
        }
    }

    private void handleMenuCommand(String command) {
        // Обрабатываем выбор в меню
        menuManager.processMenuChoice(command);

        // Выходим из режима меню только если выбрана опция 4 ("Вернуться назад")
        if (command.equals("4")) {
            inMenuMode = false;
            outputProvider.output("Возврат в главное меню. Введите 'help' для списка команд.");
        } else {
            // Если не вышли, продолжаем показывать меню
            menuManager.showMenu();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }

    public boolean isInMenuMode() {
        return inMenuMode;
    }
}
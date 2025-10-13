package org;

public class Main {
    public static void main(String[] args) {
        InputProvider inputProvider = new ConsoleInputProvider();
        OutputProvider outputProvider = new ConsoleOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);
        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);

        // ИСПРАВЛЕНИЕ: используем output() вместо showMessage()
        outputProvider.output("Добро пожаловать! Введите 'start' для начала или 'help' для помощи.");

        while (processor.isRunning()) {
            if (inputProvider.hasInput()) {
                String command = inputProvider.getInput();
                processor.processCommand(command);
            }

            // Небольшая пауза для предотвращения бесконечного цикла
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        outputProvider.output("Программа завершена.");
    }
}
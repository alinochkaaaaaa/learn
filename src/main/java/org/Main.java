package org;

public class Main {
    private static final String BOT_TOKEN = "8560887442:AAHsqgfhhqB3yE5W6WlFi2amt3ELMY7ICPM"; // ЗАМЕНИТЕ!

    public static void main(String[] args) {
        if (args.length > 0 && "telegram".equals(args[0])) {
            startTelegramBot();
        } else {
            startConsoleBot();
        }
    }

    private static void startConsoleBot() {
        InputProvider inputProvider = new ConsoleInputProvider();
        OutputProvider outputProvider = new ConsoleOutputProvider();
        MenuManager menuManager = new MenuManager(inputProvider, outputProvider);
        CommandProcessor processor = new CommandProcessor(inputProvider, outputProvider, menuManager);

        outputProvider.output("Добро пожаловать! Введите 'start' для начала или 'help' для помощи.");

        while (processor.isRunning()) {
            if (inputProvider.hasInput()) {
                String command = inputProvider.getInput();
                processor.processCommand(command);
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        outputProvider.output("Программа завершена.");
    }

    private static void startTelegramBot() {


        TelegramInputProvider telegramInput = new TelegramInputProvider();
        TelegramOutputProvider telegramOutput = new TelegramOutputProvider(BOT_TOKEN);

        MenuManager menuManager = new MenuManager(telegramInput, telegramOutput);
        CommandProcessor processor = new CommandProcessor(telegramInput, telegramOutput, menuManager);

        SimpleTelegramBot bot = new SimpleTelegramBot(BOT_TOKEN, telegramInput, telegramOutput);
        bot.setProcessor(processor);
        bot.start();

        System.out.println("✅ Telegram бот запущен! Нажмите Ctrl+C для остановки.");

        // Держим main поток alive
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            bot.stop();
        }
    }
}
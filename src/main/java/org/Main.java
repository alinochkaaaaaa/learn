package org;

public class Main {
    private static final String BOT_TOKEN = "8560887442:AAHsqgfhhqB3yE5W6WlFi2amt3ELMY7ICPM"; // ЗАМЕНИТЕ!

    public static void main(String[] args) {
        if (args.length > 0 && "telegram".equals(args[0])) {
            startTelegramBot();
        }
    }

    private static void startTelegramBot() {

        TelegramInputProvider telegramInput = new TelegramInputProvider();
        TelegramOutputProvider telegramOutput = new TelegramOutputProvider(BOT_TOKEN);

        MenuManager menuManager = new MenuManager(telegramInput, telegramOutput);
        CommandProcessor processor = new CommandProcessor(telegramInput, telegramOutput, menuManager);

        SimpleTelegramBot bot = new SimpleTelegramBot(BOT_TOKEN, telegramInput, telegramOutput);
        bot.setProcessor(processor);
        bot.start();

        System.out.println("Telegram бот запущен!");

        // Держим main поток alive
        try {
            Thread.currentThread().join(); //поток ждет завершения самого себя
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); //прерывание потока
            bot.stop();
        }
    }
}
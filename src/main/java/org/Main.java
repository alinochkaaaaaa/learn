package org;

public class Main {
    private static final String BOT_TOKEN = System.getenv("TELEGRAM_BOT_TOKEN");

    public static void main(String[] args) {
        if (args.length > 0 && "telegram".equals(args[0])) {
            startTelegramBot();
        }
    }

    private static void startTelegramBot() {
        TelegramInputProvider telegramInput = new TelegramInputProvider();
        TelegramOutputProvider telegramOutput = new TelegramOutputProvider(BOT_TOKEN);
        ReminderScheduler reminderScheduler = new ReminderScheduler();

        MenuManager menuManager = new MenuManager(telegramInput, telegramOutput, reminderScheduler);
        CommandProcessor processor = new CommandProcessor(telegramInput, telegramOutput, menuManager, reminderScheduler);

        SimpleTelegramBot bot = new SimpleTelegramBot(BOT_TOKEN, telegramInput, telegramOutput, reminderScheduler);
        bot.setProcessor(processor);
        bot.start();

        System.out.println("Telegram бот запущен!");

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            bot.stop();
        }
    }
}

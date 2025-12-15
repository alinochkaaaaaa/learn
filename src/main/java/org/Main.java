package org;

public class Main {
    private static final String BOT_TOKEN = System.getenv("TELEGRAM_BOT_TOKEN");
    private static final String MONGODB_CONNECTION_STRING =
            System.getenv("MONGODB_CONNECTION_STRING") != null ?
                    System.getenv("MONGODB_CONNECTION_STRING") : "mongodb://localhost:27017";

    public static void main(String[] args) {

        if (BOT_TOKEN == null || BOT_TOKEN.isEmpty()) {
            System.err.println(" Ошибка: TELEGRAM_BOT_TOKEN не установлен!");
            System.exit(1);
        }

        System.out.println(" MongoDB connection string: " + MONGODB_CONNECTION_STRING);

        try {
            ReminderStorage.initialize(MONGODB_CONNECTION_STRING);
            BirthdayStorage.initialize(MONGODB_CONNECTION_STRING);

            if (args.length > 0 && "telegram".equals(args[0])) {
                startTelegramBot();
            } else {
                System.out.println(" Для запуска Telegram бота используйте: java -jar app.jar telegram");
            }
        } catch (Exception e) {
            System.err.println(" Ошибка при инициализации: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void startTelegramBot() {
        try {
            TelegramInputProvider telegramInput = new TelegramInputProvider();
            TelegramOutputProvider telegramOutput = new TelegramOutputProvider(BOT_TOKEN);
            ReminderScheduler reminderScheduler = new ReminderScheduler(telegramOutput);
            BirthdayManager birthdayManager = new BirthdayManager(telegramOutput); // Создаем BirthdayManager

            reminderScheduler.scheduleAllActiveReminders();
            System.out.println(" Активные напоминания загружены и запланированы");

            MenuManager menuManager = new MenuManager(telegramInput, telegramOutput, reminderScheduler);
            // Передаем birthdayManager в CommandProcessor
            CommandProcessor processor = new CommandProcessor(telegramInput, telegramOutput, menuManager, reminderScheduler, birthdayManager);

            SimpleTelegramBot bot = new SimpleTelegramBot(BOT_TOKEN, telegramInput, telegramOutput, reminderScheduler, birthdayManager);
            bot.setProcessor(processor);
            bot.start();

            System.out.println("=".repeat(50));
            System.out.println(" Telegram бот запущен!");
            System.out.println(" Бот готов к работе...");
            System.out.println("=".repeat(50));

            try {
                Thread.currentThread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                bot.stop();
                System.out.println(" Бот остановлен");
            }
        } catch (Exception e) {
            System.err.println(" Ошибка при запуске бота: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
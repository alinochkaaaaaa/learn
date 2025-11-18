package org;

public class MenuManager {
    private final OutputProvider outputProvider;

    public MenuManager(InputProvider inputProvider, OutputProvider outputProvider) {
        this.outputProvider = outputProvider;
    }

    public void showMenu() {
        String menu = "\uD83D\uDCCB Меню:\n" +
                "1 - Информация о боте\n" +
                "2 - Текущее время\n" +
                "3 - Текущая дата\n" +
                "4 - Вернуться назад";
        outputProvider.outputMenu(menu);
    }

    public void processMenuChoice(String choice) {
        switch (choice) {
            case "1":
                outputProvider.output("\uD83D\uDE80 Информация о боте: Это телеграмм бот для демонстрации работы с меню.");
                break;
            case "2":
                outputProvider.output("⏰ Текущее время: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                break;
            case "3":
                outputProvider.output("\uD83D\uDCC5 Текущая дата: " + java.time.LocalDate.now());
                break;
            case "4":
                outputProvider.output("\uD83D\uDE80 Возврат в главное меню...");
                outputProvider.showMainMenu("\uD83D\uDCCB Главное меню");
                break;
            default:
                outputProvider.output("Неверный выбор: " + choice + ". Используйте кнопки меню.");
                showMenu();
                break;
        }
    }

    public void handleMenuSelection(String selection) {
        processMenuChoice(selection);
    }
}
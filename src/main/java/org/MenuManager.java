package org;

public class MenuManager {
    private InputProvider inputProvider;
    private OutputProvider outputProvider;

    public MenuManager(InputProvider inputProvider, OutputProvider outputProvider) {
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
    }

    public void showMenu() {
        String menu = "Меню:\n" +
                "1 - Информация о боте\n" +
                "2 - Текущее время\n" +
                "3 - Текущая дата\n" +
                "4 - Вернуться назад\n\n" +
                "help - Показать справку по командам";
        outputProvider.outputMenu(menu);
    }

    public void processMenuChoice(String choice) {
        switch (choice) {
            case "1":
                outputProvider.output("Информация о боте: Это консольный бот для демонстрации.");
                break;
            case "2":
                outputProvider.output("Текущее время: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")));
                break;
            case "3":
                outputProvider.output("Текущая дата: " + java.time.LocalDate.now());
                break;
            case "4":
                outputProvider.output("Выход из меню...");
                break;
            case "help":
                outputProvider.output("Справка по меню: выберите цифру от 1 до 4 для навигации.");
                break;
            default:
                outputProvider.output("Неверный выбор: " + choice + ". Введите 'help' для справки.");
                break;
        }
    }

    public void handleMenuSelection(String selection) {
        processMenuChoice(selection);
    }
}
//1 задача бот - консольный, но заложить архитекурно, что это будет тг бот
// /start, /menu, /help

import java.util.Scanner;

public class Main {
    private boolean isRunning = true;
    private Scanner scanner; //для чтения

    public static void main(String[] args) {
        new Main().start(); // создает объект, вызывает метод start()
    }

    public void start() {
        scanner = new Scanner(System.in);
        System.out.println("Бот запущен!");
        System.out.println("Введите help для просмотра команд");

        while (isRunning) {
            System.out.print("\n> ");
            String input = scanner.nextLine();

            if (!input.isEmpty()) {
                processCommand(input);
            }
        }

        scanner.close();
        System.out.println("Бот завершил работу");
    }

    private void processCommand(String command) {
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

    private void handleStart() {
        System.out.println("Вы запустили консольного бота.");
        System.out.println("Для просмотра команд введите help");
    }

    private void handleHelp() {
        System.out.println("Доступные команды:");
        System.out.println("start - Начать работу с ботом");
        System.out.println("help - Показать справку по командам");
        System.out.println("menu - Показать меню");
        System.out.println("exit - Завершить работу");
    }

    private void handleMenu() {
        System.out.println("Меню:");
        System.out.println("1 - Информация о боте");
        System.out.println("2 - Текущее время");
        System.out.println("3 - Текущая дата");
        System.out.println("4 - Вернуться назад");
        System.out.println();
        System.out.println("help - Показать справку по командам");

        String choice = scanner.nextLine();
        handleMenuChoice(choice);
    }

    private void handleMenuChoice(String choice) {
        switch (choice) {
            case "1":
                System.out.println("Это консольный бот на Java");
                System.out.println("Этот бот поможет вам распланировать свое время");
                break;
            case "2":
                System.out.println("Текущее время: " + java.time.LocalTime.now());
                break;
            case "3":
                // поменять на список куда дообавляются команды
                System.out.println("Текущая дата: " + java.time.LocalDate.now());
                break;
            case "4":
                System.out.println("Возврат в главное меню...");
                break;
            case "help":
                handleHelp();
                break;
            default:
                System.out.println("Неверный выбор. Пожалуйста, введите одну из доступных команд");
        }
    }

    private void handleExit() {

        isRunning = false;
    }

    private void handleUnknownCommand(String command) {
        System.out.println("Неизвестная команда: " + command);
        System.out.println("Введите help для просмотра доступных команд");
    }
}
// MenuManager.java
import java.time.LocalDate;
import java.time.LocalTime;

public class MenuManager {
    private InputProvider inputProvider;
    private OutputProvider outputProvider;

    public MenuManager(InputProvider inputProvider, OutputProvider outputProvider) {
        this.inputProvider = inputProvider;
        this.outputProvider = outputProvider;
    }

    public void showMenu() {
        String menu = buildMenu();
        outputProvider.showMenu(menu);

        String choice = inputProvider.getInput();
        processMenuChoice(choice);
    }

    private String buildMenu() {
        StringBuilder menu = new StringBuilder();
        menu.append("Меню:\n");
        menu.append("1 - Информация о боте\n");
        menu.append("2 - Текущее время\n");
        menu.append("3 - Текущая дата\n");
        menu.append("4 - Вернуться назад\n");
        menu.append("\nhelp - Показать справку по командам");
        return menu.toString();
    }

    private void processMenuChoice(String choice) {
        switch (choice) {
            case "1":
                outputProvider.showMessage("Это консольный бот на Java");
                outputProvider.showMessage("Этот бот поможет вам распланировать свое время");
                break;
            case "2":
                outputProvider.showMessage("Текущее время: " + LocalTime.now());
                break;
            case "3":
                outputProvider.showMessage("Текущая дата: " + LocalDate.now());
                break;
            case "4":
                outputProvider.showMessage("Возврат в главное меню...");
                break;
            case "help":
                outputProvider.showMessage("Доступные команды в меню: 1, 2, 3, 4, help");
                break;
            default:
                outputProvider.showMessage("Неверный выбор. Пожалуйста, введите одну из доступных команд");
        }
    }
}
package org;

public class ConsoleOutputProvider implements OutputProvider {

    @Override
    public void output(String message) {
        System.out.println(message);
    }

    @Override
    public void outputMenu(String menu) {
        // Выводим меню с чистым форматированием
        System.out.println("\n" + menu);
        System.out.print("Выберите опцию > ");
    }
}
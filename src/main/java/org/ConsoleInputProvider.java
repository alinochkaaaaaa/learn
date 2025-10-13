package org;

import java.util.Scanner;

public class ConsoleInputProvider implements InputProvider {
    private Scanner scanner;

    public ConsoleInputProvider() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getInput() {
        System.out.print("> ");
        return scanner.nextLine();
    }

    @Override
    public boolean hasInput() {
        // Для консольного ввода всегда предполагаем, что есть ввод
        // или можно проверить, открыт ли System.in
        return System.in != null && scanner.hasNextLine();
    }

    // Дополнительный метод для закрытия сканера (опционально)
    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
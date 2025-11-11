package org;

public class ConsoleOutputProvider implements OutputProvider {
    @Override
    public void output(String message) {
        System.out.println(message);
    }

    @Override
    public void outputMenu(String menu) {

    }

    @Override
    public void showMessage(String message) {
        System.out.println(message);
    }
}
// ConsoleOutputProvider.java
public class ConsoleOutputProvider implements OutputProvider {
    @Override
    public void showMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void showMenu(String menu) {
        System.out.println(menu);
    }
}
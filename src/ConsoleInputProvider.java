// ConsoleInputProvider.java
import java.util.Scanner;

public class ConsoleInputProvider implements InputProvider {
    private Scanner scanner;

    public ConsoleInputProvider() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public String getInput() {
        return scanner.nextLine();
    }

    public void close() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
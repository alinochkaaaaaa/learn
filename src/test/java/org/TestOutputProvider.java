package test.org;

import org.OutputProvider;
import java.util.ArrayList;
import java.util.List;

public class TestOutputProvider implements OutputProvider {
    private List<String> messages = new ArrayList<>();
    private String lastMenu;

    @Override
    public void output(String message) {
        messages.add(message);
        System.out.println("OUTPUT: " + message); // Для отладки
    }

    @Override
    public void outputMenu(String menu) {
        this.lastMenu = menu;
        messages.add("MENU: " + menu);
        System.out.println("MENU: " + menu); // Для отладки
    }

    public String getLastMessage() {
        if (messages.isEmpty()) {
            return "";
        }
        return messages.get(messages.size() - 1);
    }

    public List<String> getAllMessages() {
        return new ArrayList<>(messages);
    }

    public String getLastMenu() {
        return lastMenu;
    }

    public void clear() {
        messages.clear();
        lastMenu = null;
    }
}
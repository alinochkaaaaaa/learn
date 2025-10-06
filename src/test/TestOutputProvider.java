public class TestOutputProvider implements OutputProvider {
    private String lastMessage;
    private String lastMenu;

    @Override
    public void showMessage(String message) {
        this.lastMessage = message;
    }

    @Override
    public void showMenu(String menu) {
        this.lastMenu = menu;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMenu() {
        return lastMenu;
    }
}
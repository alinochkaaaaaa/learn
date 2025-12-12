package org;

public interface OutputProvider {
    void output(String message);
    void outputMenu(String menu);
    void showMessage(String message);
    void showMainMenu(String message);
    void setCurrentChatId(Long chatId);
}
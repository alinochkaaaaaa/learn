package org;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private static final Map<Long, UserState> sessions = new HashMap<>();

    public static void setState(long chatId, UserState state) {
        sessions.put(chatId, state);
        System.out.println(" Состояние пользователя " + chatId + " установлено: " + state);
    }

    public static UserState getState(long chatId) {
        UserState state = sessions.getOrDefault(chatId, UserState.MAIN_MENU);
        System.out.println(" Состояние пользователя " + chatId + ": " + state);
        return state;
    }

    public static void clearState(long chatId) {
        sessions.remove(chatId);
        System.out.println(" Состояние пользователя " + chatId + " очищено");
    }

    public static void printAllSessions() {
        System.out.println(" Активные сессии пользователей:");
        for (Map.Entry<Long, UserState> entry : sessions.entrySet()) {
            System.out.println("  • " + entry.getKey() + " -> " + entry.getValue());
        }
    }
}
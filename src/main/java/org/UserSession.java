package org;

import java.util.HashMap;
import java.util.Map;

public class UserSession {
    private static final Map<Long, UserState> sessions = new HashMap<>();

    public static void setState(long chatId, UserState state) {
        sessions.put(chatId, state);
    }

    public static UserState getState(long chatId) {
        return sessions.getOrDefault(chatId, UserState.MAIN_MENU);
    }

    public static void clearState(long chatId) {
        sessions.remove(chatId);
    }
}
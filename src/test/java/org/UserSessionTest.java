package org;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserSessionTest {

    @BeforeEach
    void setUp() {
        UserSession.clearState(123L);
        UserSession.clearState(456L);
    }

    @Test
    void testSetAndGetState() {
        UserSession.setState(123L, UserState.CREATING_REMINDER);
        assertEquals(UserState.CREATING_REMINDER, UserSession.getState(123L));
    }

    @Test
    void testGetDefaultState() {
        assertEquals(UserState.MAIN_MENU, UserSession.getState(999L)); // non-existent user
    }

    @Test
    void testMultipleUsersDifferentStates() {
        UserSession.setState(123L, UserState.IN_MENU);
        UserSession.setState(456L, UserState.CREATING_REMINDER);

        assertEquals(UserState.IN_MENU, UserSession.getState(123L));
        assertEquals(UserState.CREATING_REMINDER, UserSession.getState(456L));
    }

    @Test
    void testClearState() {
        UserSession.setState(123L, UserState.IN_MENU);
        assertEquals(UserState.IN_MENU, UserSession.getState(123L));

        UserSession.clearState(123L);
        assertEquals(UserState.MAIN_MENU, UserSession.getState(123L));
    }
}
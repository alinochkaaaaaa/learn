package org;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ReminderParserTest {

    @Test
    void testParseValidReminderWithTime() {
        String input = "напомни завтра в 15:30 позвонить маме";
        ReminderParser.Result result = ReminderParser.parse(input);

        assertNotNull(result);
        assertNotNull(result.getTriggerTime());
        assertEquals("позвонить маме", result.getText());

        LocalDateTime expectedTime = LocalDateTime.now().plusDays(1).withHour(15).withMinute(30);
        assertEquals(expectedTime.getDayOfYear(), result.getTriggerTime().getDayOfYear());
        assertEquals(15, result.getTriggerTime().getHour());
        assertEquals(30, result.getTriggerTime().getMinute());
    }

    @Test
    void testParseValidReminderToday() {
        String input = "напомни сегодня в 10:00 сделать зарядку";
        ReminderParser.Result result = ReminderParser.parse(input);

        assertNotNull(result);
        assertEquals("сделать зарядку", result.getText());
        assertEquals(LocalDateTime.now().getDayOfYear(), result.getTriggerTime().getDayOfYear());
    }

    @Test
    void testParseInvalidFormat() {
        String input = "просто текст без команды";
        ReminderParser.Result result = ReminderParser.parse(input);
        assertNull(result);
    }

    @Test
    void testParseEmptyString() {
        String input = "";
        ReminderParser.Result result = ReminderParser.parse(input);
        assertNull(result);
    }

    @Test
    void testParseReminderWithoutTime() {
        String input = "напомни завтра купить хлеб";
        ReminderParser.Result result = ReminderParser.parse(input);

        assertNotNull(result);
        assertEquals("купить хлеб", result.getText());
        assertEquals(9, result.getTriggerTime().getHour()); // default time
        assertEquals(0, result.getTriggerTime().getMinute());
    }
}
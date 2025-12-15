package org;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.jupiter.api.Assertions.*;

class ReminderParserTest {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Test
    @DisplayName("Парсинг напоминания с относительным временем (минуты)")
    void testParseRelativeTimeMinutes() {
        String input = "напомни через 5 минут позвонить маме";
        ReminderParser.ParseResult result = ReminderParser.parse(input);

        assertNotNull(result);
        assertEquals("позвонить маме", result.getText());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedTime = now.plusMinutes(5);

        // Проверяем, что время примерно соответствует ожидаемому (допуск 1 секунда)
        long diff = java.time.Duration.between(result.getTriggerTime(), expectedTime).getSeconds();
        assertTrue(Math.abs(diff) <= 1);
    }

    @Test
    @DisplayName("Парсинг напоминания с относительным временем (часы)")
    void testParseRelativeTimeHours() {
        String input = "напомни через 2 часа проверить почту";
        ReminderParser.ParseResult result = ReminderParser.parse(input);

        assertNotNull(result);
        assertEquals("проверить почту", result.getText());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedTime = now.plusHours(2);

        long diff = java.time.Duration.between(result.getTriggerTime(), expectedTime).getSeconds();
        assertTrue(Math.abs(diff) <= 1);
    }

    @Test
    @DisplayName("Парсинг напоминания с конкретной датой и временем")
    void testParseSpecificDateTime() {
        // Используем дату через месяц, чтобы она точно была в будущем
        LocalDateTime futureDate = LocalDateTime.now().plusMonths(1);
        String dateStr = futureDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        String input = "напомни " + dateStr + " в 15:30 встреча с клиентом";
        ReminderParser.ParseResult result = ReminderParser.parse(input);

        assertNotNull(result);
        assertEquals("встреча с клиентом", result.getText());

        LocalDateTime expectedTime = LocalDateTime.of(
                futureDate.getYear(),
                futureDate.getMonth(),
                futureDate.getDayOfMonth(),
                15, 30
        );

        assertEquals(expectedTime, result.getTriggerTime());
    }

    @Test
    @DisplayName("Парсинг напоминания с ключевым словом 'завтра'")
    void testParseTomorrow() {
        String input = "напомни завтра в 09:00 сделать зарядку";
        ReminderParser.ParseResult result = ReminderParser.parse(input);

        assertNotNull(result);
        assertEquals("сделать зарядку", result.getText());

        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime expectedTime = LocalDateTime.of(
                tomorrow.getYear(),
                tomorrow.getMonth(),
                tomorrow.getDayOfMonth(),
                9, 0
        );

        assertEquals(expectedTime, result.getTriggerTime());
    }

    @Test
    @DisplayName("Парсинг некорректного ввода возвращает null")
    void testParseInvalidInput() {
        // Тест 1: Пустая строка
        assertNull(ReminderParser.parse(""));

        // Тест 2: Некорректный формат
        assertNull(ReminderParser.parse("просто текст без команды"));

        // Тест 3: Команда без ключевого слова "напомни"
        assertNull(ReminderParser.parse("через 5 минут позвонить"));

        // Тест 4: Напоминание в прошлом (более 5 минут)
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(10);
        String pastDateStr = pastTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String input = "напомни " + pastDateStr + " в " +
                pastTime.format(DateTimeFormatter.ofPattern("HH:mm")) + " тест";
        assertNull(ReminderParser.parse(input));
    }
}
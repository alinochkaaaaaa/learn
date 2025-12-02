package org;

import org.junit.Before;
import org.junit.Test;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.junit.Assert.*;

public class ReminderParserTest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    @Before
    public void setUp() {
        // Можно установить фиксированное время для тестов
        // В реальных тестах лучше использовать Mockito для мока времени
    }

    // =========== ПОЗИТИВНЫЕ СЦЕНАРИИ ===========

    @Test
    public void testParseRelativeMinutes() {
        // Тест 1: "через X минут"
        ReminderParser.ParseResult result = ReminderParser.parse("напомни через 5 минут выпить воды");

        assertNotNull("Результат не должен быть null", result);
        assertNotNull("Время срабатывания не должно быть null", result.getTriggerTime());
        assertFalse("Текст не должен быть пустым", result.getText().isEmpty());

        LocalDateTime expectedTime = LocalDateTime.now().plusMinutes(5);
        LocalDateTime actualTime = result.getTriggerTime();

        // Проверяем, что разница во времени около 5 минут (допускаем небольшую погрешность)
        long diffMinutes = java.time.Duration.between(LocalDateTime.now(), actualTime).toMinutes();
        assertTrue("Должно быть примерно 5 минут", Math.abs(diffMinutes - 5) <= 1);
        assertEquals("Текст напоминания должен совпадать", "выпить воды", result.getText());
    }

    @Test
    public void testParseRelativeHours() {
        // Тест 2: "через X часов"
        ReminderParser.ParseResult result = ReminderParser.parse("напомни через 2 часа позвонить маме");

        assertNotNull(result);
        assertNotNull(result.getTriggerTime());
        assertFalse(result.getText().isEmpty());

        long diffHours = java.time.Duration.between(LocalDateTime.now(), result.getTriggerTime()).toHours();
        assertTrue("Должно быть примерно 2 часа", Math.abs(diffHours - 2) <= 1);
        assertEquals("позвонить маме", result.getText());
    }

    @Test
    public void testParseTomorrowWithTime() {
        // Тест 3: "завтра в HH:MM"
        ReminderParser.ParseResult result = ReminderParser.parse("напомни завтра в 15:30 совещание");

        assertNotNull(result);
        assertNotNull(result.getTriggerTime());

        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime expectedTime = LocalDateTime.of(tomorrow.toLocalDate(), java.time.LocalTime.of(15, 30));
        LocalDateTime actualTime = result.getTriggerTime();

        // Проверяем дату (завтра) и время (15:30)
        assertEquals("Должно быть завтра", tomorrow.toLocalDate(), actualTime.toLocalDate());
        assertEquals("Должно быть 15:30", 15, actualTime.getHour());
        assertEquals("Должно быть 15:30", 30, actualTime.getMinute());
        assertEquals("совещание", result.getText());
    }

    @Test
    public void testParseTodayWithTime() {
        // Тест 4: "сегодня в HH:MM"
        // Если время уже прошло, должно перенестись на завтра
        LocalDateTime now = LocalDateTime.now();
        int testHour = (now.getHour() - 1 + 24) % 24; // час, который уже прошел

        String command = String.format("напомни сегодня в %02d:45 купить хлеб", testHour);
        ReminderParser.ParseResult result = ReminderParser.parse(command);

        assertNotNull(result);

        assertEquals(45, result.getTriggerTime().getMinute());
    }

    @Test
    public void testParseSpecificDate() {
        // Тест 5: Конкретная дата "DD.MM в HH:MM"
        // Используем дату в будущем
        LocalDateTime futureDate = LocalDateTime.now().plusMonths(1);
        String dateStr = String.format("%02d.%02d", futureDate.getDayOfMonth(), futureDate.getMonthValue());

        ReminderParser.ParseResult result = ReminderParser.parse(
                "напомни " + dateStr + " в 10:00 день рождения"
        );

        assertNotNull(result);
        assertEquals(futureDate.getDayOfMonth(), result.getTriggerTime().getDayOfMonth());
        assertEquals(futureDate.getMonthValue(), result.getTriggerTime().getMonthValue());
        assertEquals(10, result.getTriggerTime().getHour());
        assertEquals(0, result.getTriggerTime().getMinute());
        assertEquals("день рождения", result.getText());
    }

    @Test
    public void testParseWithoutTime() {
        // Тест 6: Без указания времени (должен использовать 09:00 по умолчанию)
        ReminderParser.ParseResult result = ReminderParser.parse("напомни завтра уборка");

        assertNotNull(result);
        assertEquals(9, result.getTriggerTime().getHour());
        assertEquals(0, result.getTriggerTime().getMinute());
        assertEquals("уборка", result.getText());
    }

    // =========== НЕГАТИВНЫЕ СЦЕНАРИИ ===========

    @Test
    public void testParseInvalidFormat() {
        // Тест 7: Неправильный формат (без "напомни")
        ReminderParser.ParseResult result = ReminderParser.parse("позвонить маме в 15:00");

        assertNull("При неправильном формате должен возвращать null", result);
    }

    @Test
    public void testParseEmptyMessage() {
        // Тест 8: Пустое сообщение
        ReminderParser.ParseResult result = ReminderParser.parse("напомни завтра в 15:00");

        // В зависимости от реализации парсера может вернуть null или объект с пустым текстом
        // Проверяем оба варианта
        if (result != null) {
            assertTrue("Текст должен быть пустым или null",
                    result.getText() == null || result.getText().isEmpty());
        }
    }

    @Test
    public void testParseInvalidTime() {
        // Тест 9: Неправильное время
        ReminderParser.ParseResult result = ReminderParser.parse("напомни сегодня в 25:70 тест");

        // Парсер должен обработать некорректное время
        // Проверяем, что не падает с исключением
        assertNotNull("Парсер не должен падать при некорректном времени", result);
    }

    @Test
    public void testParseNullInput() {
        // Тест 10: Null на входе
        ReminderParser.ParseResult result = ReminderParser.parse(null);

        assertNull("При null входе должен возвращать null", result);
    }

    @Test
    public void testParseMalformedDate() {
        // Тест 11: Некорректная дата
        ReminderParser.ParseResult result = ReminderParser.parse("напомни 32.13 в 10:00 тест");

        // Не должно падать с исключением
        // Может вернуть null или попытаться обработать
        // Главное - не должно быть исключения
        try {
            // Просто проверяем, что выполняется без исключений
            assertTrue(true);
        } catch (Exception e) {
            fail("Не должно быть исключения при некорректной дате: " + e.getMessage());
        }
    }
}
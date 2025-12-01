package org;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReminderRepository {
    private static final String DATABASE_NAME = "reminder_bot";
    private static final String COLLECTION_NAME = "reminders";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public ReminderRepository(String connectionString) {
        try {
            System.out.println("🔗 Подключение к MongoDB: " + connectionString);
            this.mongoClient = MongoClients.create(connectionString);
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            this.collection = database.getCollection(COLLECTION_NAME);

            // Проверяем подключение
            database.runCommand(new Document("ping", 1));
            System.out.println("✅ Успешное подключение к MongoDB");

            // Создаем индексы для улучшения производительности
            createIndexes();

            // Проверим, есть ли уже документы
            long count = collection.countDocuments();
            System.out.println("📊 В коллекции " + COLLECTION_NAME + " документов: " + count);

        } catch (Exception e) {
            System.err.println("❌ Ошибка подключения к MongoDB: " + e.getMessage());
            throw new RuntimeException("Не удалось подключиться к MongoDB", e);
        }
    }

    private void createIndexes() {
        try {
            // Индекс для быстрого поиска по chatId и completed
            collection.createIndex(new Document("chatId", 1).append("completed", 1));
            // Индекс для быстрого поиска по triggerTime
            collection.createIndex(new Document("triggerTime", 1));
            System.out.println("✅ Индексы созданы");
        } catch (Exception e) {
            System.err.println("⚠️ Не удалось создать индексы: " + e.getMessage());
        }
    }

    public void save(Reminder reminder) {
        try {
            String triggerTimeStr = reminder.getTriggerTime().format(FORMATTER);
            System.out.println("💾 Сохранение напоминания в MongoDB:");
            System.out.println("   chatId: " + reminder.getChatId());
            System.out.println("   message: " + reminder.getMessage());
            System.out.println("   triggerTime: " + triggerTimeStr);

            Document doc = new Document()
                    .append("chatId", reminder.getChatId())
                    .append("message", reminder.getMessage())
                    .append("triggerTime", triggerTimeStr)
                    .append("completed", false)
                    .append("createdAt", LocalDateTime.now().format(FORMATTER));

            collection.insertOne(doc);
            System.out.println("✅ Документ сохранен в MongoDB, ID: " + doc.get("_id"));

            // Проверим, сколько теперь документов
            long totalCount = collection.countDocuments();
            long userCount = collection.countDocuments(Filters.eq("chatId", reminder.getChatId()));
            System.out.println("📊 Всего документов: " + totalCount);
            System.out.println("📊 Документов для пользователя " + reminder.getChatId() + ": " + userCount);

        } catch (Exception e) {
            System.err.println("❌ Ошибка сохранения в MongoDB: " + e.getMessage());
            throw e;
        }
    }

    public List<Reminder> findActiveByChatId(long chatId) {
        List<Reminder> reminders = new ArrayList<>();
        try {
            System.out.println("🔍 Поиск активных напоминаний для chatId: " + chatId);

            FindIterable<Document> documents = collection.find(
                    Filters.and(
                            Filters.eq("chatId", chatId),
                            Filters.eq("completed", false)
                    )
            ).sort(new Document("triggerTime", 1));

            int count = 0;
            for (Document doc : documents) {
                count++;
                reminders.add(documentToReminder(doc));
            }

            System.out.println("✅ Найдено напоминаний для chatId " + chatId + ": " + count);

        } catch (Exception e) {
            System.err.println("❌ Ошибка поиска в MongoDB: " + e.getMessage());
        }
        return reminders;
    }

    public List<Reminder> findAllActive() {
        List<Reminder> reminders = new ArrayList<>();
        try {
            System.out.println("🔍 Поиск всех активных напоминаний...");

            FindIterable<Document> documents = collection.find(Filters.eq("completed", false))
                    .sort(new Document("triggerTime", 1));

            int count = 0;
            for (Document doc : documents) {
                count++;
                reminders.add(documentToReminder(doc));
            }

            System.out.println("✅ Всего активных напоминаний: " + count);

        } catch (Exception e) {
            System.err.println("❌ Ошибка поиска всех активных напоминаний: " + e.getMessage());
        }
        return reminders;
    }

    public void markAsCompleted(Reminder reminder) {
        try {
            String triggerTimeStr = reminder.getTriggerTime().format(FORMATTER);
            System.out.println("✅ Отметка напоминания как выполненного:");
            System.out.println("   chatId: " + reminder.getChatId());
            System.out.println("   message: " + reminder.getMessage());
            System.out.println("   triggerTime: " + triggerTimeStr);

            long updatedCount = collection.updateOne(
                    Filters.and(
                            Filters.eq("chatId", reminder.getChatId()),
                            Filters.eq("message", reminder.getMessage()),
                            Filters.eq("triggerTime", triggerTimeStr)
                    ),
                    new Document("$set",
                            new Document("completed", true)
                                    .append("completedAt", LocalDateTime.now().format(FORMATTER))
                    )
            ).getModifiedCount();

            if (updatedCount > 0) {
                System.out.println("✅ Напоминание отмечено как выполненное в MongoDB");
            } else {
                System.err.println("⚠️ Не найдено напоминание для отметки как выполненное");
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка обновления в MongoDB: " + e.getMessage());
            throw e;
        }
    }

    private Reminder documentToReminder(Document doc) {
        long chatId = doc.getLong("chatId");
        String message = doc.getString("message");
        LocalDateTime triggerTime = LocalDateTime.parse(doc.getString("triggerTime"), FORMATTER);

        Reminder reminder = new Reminder(chatId, message, triggerTime);
        System.out.println("📄 Загружено напоминание: " + reminder);
        return reminder;
    }

    // Закрытие подключения при необходимости
    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
            System.out.println("🔌 Подключение к MongoDB закрыто");
        }
    }

    // Метод для отладки - показать все документы
    public void printAllDocuments() {
        try {
            System.out.println("📊 Все документы в коллекции:");
            FindIterable<Document> documents = collection.find();
            int count = 0;
            for (Document doc : documents) {
                count++;
                System.out.println("\nДокумент #" + count + ":");
                System.out.println("  ID: " + doc.getObjectId("_id"));
                System.out.println("  chatId: " + doc.getLong("chatId"));
                System.out.println("  message: " + doc.getString("message"));
                System.out.println("  triggerTime: " + doc.getString("triggerTime"));
                System.out.println("  completed: " + doc.getBoolean("completed"));
                System.out.println("  createdAt: " + doc.getString("createdAt"));
            }
            if (count == 0) {
                System.out.println("Коллекция пуста");
            }
        } catch (Exception e) {
            System.err.println("❌ Ошибка при выводе документов: " + e.getMessage());
        }
    }
}
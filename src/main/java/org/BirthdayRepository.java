package org;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BirthdayRepository {
    private static final String DATABASE_NAME = "reminder_bot";
    private static final String COLLECTION_NAME = "birthday_contacts";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final MongoClient mongoClient;
    private final MongoCollection<Document> collection;

    public BirthdayRepository(String connectionString) {
        this.mongoClient = MongoClients.create(connectionString);
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        this.collection = database.getCollection(COLLECTION_NAME);
        createIndexes();
    }

    private void createIndexes() {
        try {
            collection.createIndex(new Document("chatId", 1).append("contactId", 1));
        } catch (Exception e) {
        }
    }

    public void save(BirthdayContact contact) {
        Document doc = new Document()
                .append("chatId", contact.getChatId())
                .append("contactId", contact.getContactId())
                .append("contactName", contact.getContactName())
                .append("notificationsEnabled", contact.isNotificationsEnabled())
                .append("daysBefore", contact.getDaysBefore())
                .append("customMessage", contact.getCustomMessage());

        if (contact.getBirthday() != null) {
            doc.append("birthday", contact.getBirthday().format(FORMATTER));
        }

        collection.insertOne(doc);
        if (doc.get("_id") != null) {
            contact.setId(doc.getObjectId("_id").toString());
        }
    }

    public List<BirthdayContact> findByChatId(long chatId) {
        List<BirthdayContact> contacts = new ArrayList<>();
        FindIterable<Document> documents = collection.find(Filters.eq("chatId", chatId));

        for (Document doc : documents) {
            contacts.add(documentToContact(doc));
        }

        return contacts;
    }

    public BirthdayContact findById(String contactId, long chatId) {
        Document doc = collection.find(
                Filters.and(
                        Filters.eq("_id", new ObjectId(contactId)),
                        Filters.eq("chatId", chatId)
                )
        ).first();

        return doc != null ? documentToContact(doc) : null;
    }

    public void updateBirthday(String contactId, long chatId, LocalDate birthday) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(contactId)),
                        Filters.eq("chatId", chatId)
                ),
                new Document("$set", new Document("birthday", birthday.format(FORMATTER)))
        );
    }

    public void toggleNotifications(String contactId, long chatId, boolean enabled) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(contactId)),
                        Filters.eq("chatId", chatId)
                ),
                new Document("$set", new Document("notificationsEnabled", enabled))
        );
    }

    public void updateReminderSettings(String contactId, long chatId, int daysBefore, String customMessage) {
        collection.updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(contactId)),
                        Filters.eq("chatId", chatId)
                ),
                new Document("$set",
                        new Document("daysBefore", daysBefore)
                                .append("customMessage", customMessage))
        );
    }

    public void delete(String contactId, long chatId) {
        collection.deleteOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(contactId)),
                        Filters.eq("chatId", chatId)
                )
        );
    }

    public List<BirthdayContact> findAll() {
        List<BirthdayContact> contacts = new ArrayList<>();
        FindIterable<Document> documents = collection.find();

        for (Document doc : documents) {
            contacts.add(documentToContact(doc));
        }

        return contacts;
    }

    private BirthdayContact documentToContact(Document doc) {
        long chatId = doc.getLong("chatId");
        long contactId = doc.getLong("contactId");
        String contactName = doc.getString("contactName");

        BirthdayContact contact = new BirthdayContact(chatId, contactId, contactName);
        contact.setId(doc.getObjectId("_id").toString());

        if (doc.containsKey("birthday")) {
            LocalDate birthday = LocalDate.parse(doc.getString("birthday"), FORMATTER);
            contact.setBirthday(birthday);
        }

        contact.setNotificationsEnabled(doc.getBoolean("notificationsEnabled", true));
        contact.setDaysBefore(doc.getInteger("daysBefore", 1));

        String customMessage = doc.getString("customMessage");
        if (customMessage == null) {
            customMessage = "Напоминание о дне рождения";
        }
        contact.setCustomMessage(customMessage);

        return contact;
    }
}
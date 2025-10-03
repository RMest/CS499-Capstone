package com.example.rileymest.data.remote;

import android.util.Log;

import com.mongodb.client.*;
import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Super-minimal, not production-grade. For class demo only.
public final class MongoHelper {

    private static final String MONGO_URI = ""; // MongoDB connection URL
    private static final String DB_NAME = "InventoryManager";
    private static MongoHelper INSTANCE;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private MongoClient client;
    private MongoDatabase db;
    private MongoCollection<Document> currCollection;

    private MongoHelper() {}

    public static synchronized MongoHelper get() {
        if (INSTANCE == null) INSTANCE = new MongoHelper();
        return INSTANCE;
    }

    public void connect() { // function for connecting to MongoDB database
        if (client != null) return;
        io.execute(() -> {
            try {
                client = MongoClients.create(MONGO_URI);
                db = client.getDatabase(DB_NAME);

                for (String name : client.listDatabaseNames()) {
                    Log.d("Mongo", "DB: " + name);
                }
                Log.d("Mongo", "Using DB=" + db.getName());
            } catch (Exception e) {
                Log.e("Mongo", "Connect failed", e);
                client = null; db = null; currCollection = null;
            }
        });
    }

    public void connectBlocking() { // blocks user from entering app until fully connected to database
        if (client != null) return;
        try {
            client = MongoClients.create(MONGO_URI);
            db = client.getDatabase(DB_NAME);
            Log.d("Mongo", "Connected to DB blocking");
        } catch (Exception e) {
            client = null; db = null;
            Log.e("Mongo", "Blocking connect failed", e);
        }
    }

    public interface Callback<T> { void onResult(T result, Exception error); }

    /** -------------Item inventory database functions ------------- **/

    public void insertItem(String name, int qty, Callback<String> cb) { // inserts item into database
        currCollection = db.getCollection("items");
        io.execute(() -> {
            try {
                Document doc = new Document("name", name).append("qty", qty);
                currCollection.insertOne(doc);
                String id = doc.getObjectId("_id").toHexString();
                cb.onResult(id, null);
            } catch (Exception e) { cb.onResult(null, e); }
        });
    }

    public void listItems(Callback<List<Document>> cb) { // gets every item in database
        currCollection = db.getCollection("items");
        io.execute(() -> {
            try {
                List<Document> out = new ArrayList<>();
                try (MongoCursor<Document> cur = currCollection.find().iterator()) {
                    while (cur.hasNext()) out.add(cur.next());
                }
                cb.onResult(out, null);
            } catch (Exception e) { cb.onResult(null, e); }
        });
    }

    public void updateQty(String idHex, int qty, Callback<Boolean> cb) { // updates one item in database
        currCollection = db.getCollection("items");
        io.execute(() -> {
            try {
                var res = currCollection.updateOne(new Document("_id", new org.bson.types.ObjectId(idHex)),
                        new Document("$set", new Document("qty", qty)));
                cb.onResult(res.getModifiedCount() == 1, null);
            } catch (Exception e) { cb.onResult(false, e); }
        });
    }

    /** -------------User database functions ------------- **/

    public void insertUser(String username, String password, Callback<String> cb) { // inserts a new user into database
        currCollection = db.getCollection("users");
        String encryptedPass = encryptDecryptString(password);
        io.execute(() -> {
            try {
                Document doc = new Document("user", username).append("pass", encryptedPass);
                currCollection.insertOne(doc);
                String userId = doc.getObjectId("_id").toHexString();
                cb.onResult(userId, null);
            } catch (Exception e) { cb.onResult(null, e); }
        });
    }

    public void validateUser(String username, String password, Callback<Boolean> cb) { // checks if user is in database
        currCollection = db.getCollection("users");
        String encryptedPass = encryptDecryptString(password);
        io.execute(() -> {
            try {
                Document query = new Document("user", username).append("pass", encryptedPass);
                boolean exists = currCollection.find(query).iterator().hasNext();
                cb.onResult(exists, null);
            } catch (Exception e) { cb.onResult(null, e); }
        });
    }

    public void updatePassword(String idHex, String newPass, Callback<Boolean> cb) { // updates user password
        currCollection = db.getCollection("users");
        String encryptedPass = encryptDecryptString(newPass);
        io.execute(() -> {
            try {
                var res = currCollection.updateOne(new Document("_id", new org.bson.types.ObjectId(idHex)),
                        new Document("$set", new Document("pass", encryptedPass)));
                cb.onResult(res.getModifiedCount() == 1, null);
            } catch (Exception e) { cb.onResult(false, e); }
        });
    }

    public void getUserId(String username, Callback<String> cb) { // gets the currently logged in userId
        currCollection = db.getCollection("users");
        io.execute(() -> {
            try {
                Document query = new Document("user", username);
                Document userDoc = currCollection.find(query).first();
                if (userDoc != null) {
                    String userId = userDoc.getObjectId("_id").toHexString();
                    cb.onResult(userId, null);
                } else {
                    cb.onResult(null, null);
                }
            } catch (Exception e) { cb.onResult(null, e); }
        });
    }

    public void getUsernameFromId(String idHex, Callback<String> cb) { // gets the username of logged in user
        currCollection = db.getCollection("users");
        io.execute(() -> {
            try {
                org.bson.types.ObjectId oid = new org.bson.types.ObjectId(idHex);
                Document query = new Document("_id", oid);
                Document userDoc = currCollection.find(query).first();
                if (userDoc != null) {
                    String username = userDoc.getString("user");
                    cb.onResult(username, null);
                } else {
                    cb.onResult(null, null);
                }
            } catch (Exception e) { cb.onResult(null, e); }
        });
    }

    /** -------------Miscellaneous functions ------------- **/

    public void stringExists(String string, String COLLECTION, Callback<Boolean> cb) { // checks if something exists in given collection
        currCollection = db.getCollection(COLLECTION);
        io.execute(() -> {
            try {
                Document query = new Document("name", string);
                boolean exists = currCollection.find(query).iterator().hasNext();
                cb.onResult(exists, null);
            } catch (Exception e) { cb.onResult(false, e); }
        });
    }

    public void delete(String idHex, String COLLECTION, Callback<Boolean> cb) { // deletes id of something from database
        currCollection = db.getCollection(COLLECTION);
        io.execute(() -> {
            try {
                var res = currCollection.deleteOne(new Document("_id", new org.bson.types.ObjectId(idHex)));
                cb.onResult(res.getDeletedCount() == 1, null);
            } catch (Exception e) { cb.onResult(false, e); }
        });
    }

    private String encryptDecryptString(String string) { // simple XOR encryption method
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            String key = "azbxcwdveut";
            char stringChar = string.charAt(i);
            char keyChar = key.charAt(i % key.length());
            stringBuilder.append((char) (stringChar ^ keyChar));
        }
        return stringBuilder.toString();
    }

    public void close() { // closes connection to database. unused but here if needed
        io.execute(() -> {
            try { if (client != null) client.close(); } catch (Exception ignored) {}
        });
    }
}


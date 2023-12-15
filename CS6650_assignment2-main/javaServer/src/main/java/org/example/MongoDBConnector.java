package org.example;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class MongoDBConnector {
    private static MongoClient mongoClient;
    private static MongoDatabase database;

    static {
        mongoClient = new MongoClient("ip-172-31-17-155.us-west-2.compute.internal", 27017); // Connect to MongoDB server
        database = mongoClient.getDatabase("cs6650DB");
    }

    public static MongoCollection<Document> getAlbumCollection() {
        return database.getCollection("albums");
    }
}

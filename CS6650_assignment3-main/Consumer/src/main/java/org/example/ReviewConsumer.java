package org.example;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.rabbitmq.client.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReviewConsumer {

    private final static String QUEUE_NAME = "reviewQueue";
    private final static int MONGO_PORT = 27017;
    private final static int THREAD_POOL_SIZE = 800; // Adjust the size according to your system's capabilities

    private static final Logger logger = LoggerFactory.getLogger(ReviewConsumer.class);

    public static void main(String[] argv) throws Exception {
        MongoCollection<Document> collection = MongoDBConnector.getReviewCollection();

        ConnectionFactory factory = new ConnectionFactory();
        //factory.setHost("ip-172-31-27-112.us-west-2.compute.internal");
        factory.setHost("localhost");
        Connection connection = factory.newConnection();

        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executorService.submit(() -> {
                try {
                    Channel consumerChannel = connection.createChannel();
                    consumerChannel.queueDeclare(QUEUE_NAME, true, false, false, null);
                    consumerChannel.basicQos(1);

                    DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                        String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                        try {
                            processMessage(message, collection);
                            //consumerChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        } catch (Exception e) {
                            logger.error("Error processing message: " + message, e);
                        }
                    };
                    consumerChannel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> {});
                } catch (IOException e) {
                    logger.error("Error in consumer thread: ", e);
                }
            });
        }

        // Add shutdown hook to properly close resources on shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            executorService.shutdownNow();
            try {
                connection.close();
            } catch (IOException e) {
                logger.error("Failed to close RabbitMQ connection", e);
            }
        }));
    }

    private static void processMessage(String message, MongoCollection<Document> collection) {

        // Simulate some processing time
        /*
        try {
            Thread.sleep(1000); // Delays for 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            logger.error("Thread interrupted", e);
        }
        */
        Document doc = Document.parse(message);

        // Extract albumID and likeOrNot from the message
        String albumIDString = doc.getString("albumID");
        String likeOrNot = doc.getString("likeOrNot");

        if (albumIDString == null || likeOrNot == null || !ObjectId.isValid(albumIDString)) {
            logger.error("Invalid message received: {}", message);
            return;
        }

        ObjectId albumID = new ObjectId(albumIDString);

        Document update = new Document("$inc", new Document(likeOrNot, 1));
        collection.updateOne(new Document("albumId", albumID), update, new UpdateOptions().upsert(true));
        logger.info("Processed message: {}", message);
    }
}


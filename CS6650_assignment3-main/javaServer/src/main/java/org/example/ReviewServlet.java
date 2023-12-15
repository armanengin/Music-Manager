package org.example;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

@WebServlet("/review/*")
public class ReviewServlet extends HttpServlet {
    private static final String QUEUE_NAME = "reviewQueue";
    private static Connection connection;
    private GenericObjectPool<Channel> channelPool;
    private final Gson gson = new Gson();

    private static final Logger logger = LoggerFactory.getLogger(ReviewServlet.class);

    @Override
    public void init() throws ServletException {
        // Configure and initialize RabbitMQ connection and channel pool
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("ip-172-31-27-112.us-west-2.compute.internal"); // Set to your RabbitMQ server address
        try {
            connection = factory.newConnection();
            channelPool = new GenericObjectPool<>(new ChannelFactory(connection));
        } catch (IOException | TimeoutException e) {
            throw new ServletException("Could not initialize RabbitMQ connection or channel pool", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo(); // "/{likeornot}/{albumID}"
        String[] pathParts = pathInfo.split("/");

        // Assuming the URL pattern is /review/{likeornot}/{albumID}
        if (pathParts.length != 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("msg", "Invalid URL format");
            resp.getWriter().println(gson.toJson(error));
            return;
        }

        String likeOrNot = pathParts[1];
        String albumID = pathParts[2];
        logger.debug("Received review request with albumID: {} and likeOrNot: {}", albumID, likeOrNot);
        // Validation logic here...
        if (likeOrNot == null || albumID == null) {
            logger.error("Invalid request parameters: albumID: {}, likeOrNot: {}", albumID, likeOrNot);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            JsonObject error = new JsonObject();
            error.addProperty("msg", "Invalid request parameters.");
            resp.getWriter().println(gson.toJson(error));
            return;
        }
        Channel channel = null;
        try {
            channel = channelPool.borrowObject(); // Borrow a channel from the pool

            // Ensure the queue exists before publishing
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);

            JsonObject message = new JsonObject();
            message.addProperty("albumID", albumID);
            message.addProperty("likeOrNot", likeOrNot);
            // Publish to RabbitMQ
            channel.basicPublish("", QUEUE_NAME, null, message.toString().getBytes());

            resp.setStatus(HttpServletResponse.SC_OK);
            JsonObject response = new JsonObject();
            response.addProperty("msg", "Review received");
            resp.getWriter().println(gson.toJson(response));
        } catch (Exception e) {
            logger.error("Failed to publish review message: {}", e.getMessage(), e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            JsonObject error = new JsonObject();
            error.addProperty("msg", "Failed to publish review message");
            resp.getWriter().println(gson.toJson(error));
        } finally {
            if (channel != null) {
                channelPool.returnObject(channel); // Return the channel to the pool
            }
        }
    }

    @Override
    public void destroy() {
        try {
            channelPool.close(); // Close the channel pool
            connection.close(); // Close the connection
        } catch (IOException e) {
            // Log or handle the exception during destroy
        }
    }
}

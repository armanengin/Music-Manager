package io.swagger.client.api;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.helper.RequestCounterBarrier;
import io.swagger.client.utils.CSVUtil;
import io.swagger.client.utils.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewLogic {
    private static final Logger logger = LoggerFactory.getLogger(ReviewLogic.class);

    public static void postReview(String ipAddr, String albumID, String likeOrNot, RequestCounterBarrier counter) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(ipAddr);
        //System.out.println("******* -> " + albumID);
        LikeApi apiInstance = new LikeApi(apiClient);

        long startTime = System.currentTimeMillis(); // Start timestamp
        try {
            //System.out.println("logic gets: " + likeOrNot + " and, " + albumID);
            apiInstance.review(likeOrNot, albumID);
            long endTime = System.currentTimeMillis(); // End timestamp
            CSVUtil.addToQueue(new RequestData(startTime, "POST", endTime - startTime, 200));
            counter.inc();
        } catch (ApiException e) {
            long endTime = System.currentTimeMillis();
            CSVUtil.addToQueue(new RequestData(startTime, "POST", endTime - startTime, e.getCode()));
            logger.error("ApiException encountered while posting review: ", e);
            System.out.println("Error response body: " + e.getResponseBody());
            System.out.println(e.getMessage());

            // Handle retries for 4XX and 5XX errors
            int retryCount = 0;
            while (retryCount < 5 && (e.getCode() >= 400 && e.getCode() < 600)) {
                try {
                    apiInstance.review(likeOrNot, albumID);
                    counter.inc();
                    break; // Break if successful
                } catch (ApiException retryException) {
                    retryCount++;
                }
            }
            if (retryCount == 5) {
                logger.error("Failed review POST request after 5 retries.", e);
            }
        }
    }
}

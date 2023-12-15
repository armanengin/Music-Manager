package io.swagger.client.api;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.helper.RequestCounterBarrier;
import io.swagger.client.model.AlbumInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetLogic {
    private static final Logger logger = LoggerFactory.getLogger(GetLogic.class);
    public static void run(String ipAddr, RequestCounterBarrier counter) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(ipAddr);
        DefaultApi apiInstance = new DefaultApi(apiClient);
        String albumID = "12345";

        try {
            AlbumInfo result = apiInstance.getAlbumByKey(albumID);
            counter.inc();
        } catch (ApiException e) {
            // Handle retries for 4XX and 5XX errors
            int retryCount = 0;
            while(retryCount < 5 && (e.getCode() >= 400 && e.getCode() < 600)) {
                try {
                    AlbumInfo retryResult = apiInstance.getAlbumByKey(albumID);
                    counter.inc();
                    break; // Break if successful
                } catch (ApiException retryException) {
                    retryCount++;
                }
            }
            if(retryCount == 5) {
                logger.error("Failed to retrieve album after 5 retries. AlbumID: {}", albumID, e);
            }
        }
    }
}

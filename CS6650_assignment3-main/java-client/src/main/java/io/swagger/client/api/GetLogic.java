package io.swagger.client.api;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.helper.RequestCounterBarrier;
import io.swagger.client.model.AlbumInfo;
import io.swagger.client.utils.CSVUtil;
import io.swagger.client.utils.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.swagger.client.utils.CSVUtil.writeToCSV;

public class GetLogic {
    private static final Logger logger = LoggerFactory.getLogger(GetLogic.class);
    public static void run(String ipAddr, RequestCounterBarrier counter, String albumID) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(ipAddr);
        DefaultApi apiInstance = new DefaultApi(apiClient);
        //String albumID = "6542c6e2181b324b30694619";
        long startTime = System.currentTimeMillis();  // Start timestamp

        try {
            AlbumInfo result = apiInstance.getAlbumByKey(albumID);
            long endTime = System.currentTimeMillis();
            CSVUtil.addToQueue(new RequestData(startTime, "GET", endTime - startTime, 200));
            //writeToCSV(startTime, "GET", endTime - startTime, 200);  // Assuming 200 as the success code
            counter.inc();
        } catch (ApiException e) {
            long endTime = System.currentTimeMillis();
            CSVUtil.addToQueue(new RequestData(startTime, "GET", endTime - startTime, e.getCode()));
            //writeToCSV(startTime, "GET", endTime - startTime, e.getCode());
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

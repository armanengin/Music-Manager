package io.swagger.client.api;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.helper.RequestCounterBarrier;
import io.swagger.client.model.AlbumsProfile;
import io.swagger.client.model.ImageMetaData;
import io.swagger.client.utils.CSVUtil;
import io.swagger.client.utils.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import static io.swagger.client.utils.CSVUtil.writeToCSV;

public class PostLogic {
    private static final Logger logger = LoggerFactory.getLogger(PostLogic.class);
    public static String run(String ipAddr, RequestCounterBarrier counter) throws ApiException {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(ipAddr);
        DefaultApi apiInstance = new DefaultApi(apiClient);
        File image = new File("nmtb.png");
        AlbumsProfile profile = new AlbumsProfile();
        profile.setArtist("Sex Pistols");
        profile.setTitle("Never Mind The Bollocks!");
        profile.setYear("1977");
        long startTime = System.currentTimeMillis();  // Start timestamp
        try {
            ImageMetaData result = apiInstance.newAlbum(image, profile);
            long endTime = System.currentTimeMillis();  // End timestamp
            CSVUtil.addToQueue(new RequestData(startTime, "POST", endTime - startTime, 200));
            //writeToCSV(startTime, "POST", endTime - startTime, 200);
            counter.inc();
            return result.getAlbumID();
        } catch (ApiException e) {
            long endTime = System.currentTimeMillis();
            CSVUtil.addToQueue(new RequestData(startTime, "POST", endTime - startTime, e.getCode()));
            //writeToCSV(startTime, "POST", endTime - startTime, e.getCode());
            logger.error("ApiException encountered: ", e);
            System.out.println(e.getMessage());
            // Handle retries for 4XX and 5XX errors
            int retryCount = 0;
            while(retryCount < 5 && (e.getCode() >= 400 && e.getCode() < 600)) {
                try {
                    ImageMetaData retryResult = apiInstance.newAlbum(image, profile);
                    counter.inc();
                    break; // Break if successful
                } catch (ApiException retryException) {
                    retryCount++;
                }
            }
            if(retryCount == 5) {
                logger.error("Failed POST request after 5 retries.", e);
            }
            return null;
        }
    }
}
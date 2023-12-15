package io.swagger.client.api;

import io.swagger.client.helper.RequestCounterBarrier;
import io.swagger.client.utils.CSVUtil;
import io.swagger.client.utils.MetricsCalculator;
import io.swagger.client.utils.RequestData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class TestClient {
    private static final Logger logger = LoggerFactory.getLogger(TestClient.class);
    private static final int INITIAL_THREADS = 10;
    private static final int INITIAL_REQUESTS = 100;
    private static final int REQUESTS_PER_THREAD = 2000;

    private final int threadGroupSize, numThreadGroups, delay;
    private final String ipAddr;
    private final RequestCounterBarrier counter;

    public TestClient(int threadGroupSize, int numThreadGroups, int delay, String ipAddr) {
        this.threadGroupSize = threadGroupSize;
        this.numThreadGroups = numThreadGroups;
        this.delay = delay;
        this.ipAddr = ipAddr;
        this.counter = new RequestCounterBarrier();
    }

    public int getVal() {
        return counter.getVal();
    }

    private void runRequests(int requestCount, String ipAddr, boolean countRequests) {
        for (int i = 0; i < requestCount / 2; i++) { // We divide by 2 because for each iteration, we do 1 POST and 1 GET
            // POST request logic
            String albumID = null;
            try {
                albumID = PostLogic.run(ipAddr, counter);
                if (countRequests) {
                    counter.incrementSuccessfulRequests();
                }
            } catch (Exception e) {
                logger.error("Error executing PostLogic", e);
                if (countRequests) {
                    counter.incrementFailedRequests();
                }
            }

            // GET request logic
            if (albumID != null) {
                try {
                    GetLogic.run(ipAddr, counter, albumID);
                    if (countRequests) {
                        counter.incrementSuccessfulRequests();
                    }
                } catch (Exception e) {
                    logger.error("Error executing GetLogic", e);
                    if (countRequests) {
                        counter.incrementFailedRequests();
                    }
                }
            }
        }
    }


    public static void main(String[] args) throws InterruptedException {
        // Clear CSV content before starting client operations
        CSVUtil.clearCSV();

        if (args.length < 4) {
            System.err.println("Usage: <threadGroupSize> <numThreadGroups> <delay> <ipAddr>");
            return;
        }

        int threadGroupSize = Integer.parseInt(args[0]);
        int numThreadGroups = Integer.parseInt(args[1]);
        int delay = Integer.parseInt(args[2]);
        String ipAddr = args[3];

        TestClient client = new TestClient(threadGroupSize, numThreadGroups, delay, ipAddr);
        CountDownLatch initialLatch = new CountDownLatch(INITIAL_THREADS);


        System.out.println("Configuration Used:");
        System.out.println("Thread Group Size: " + threadGroupSize);
        System.out.println("Number of Thread Groups: " + numThreadGroups);
        System.out.println("Delay Between Thread Groups: " + delay + " seconds");
        System.out.println("IP Address: " + ipAddr);
        System.out.println();


        // Initial Threads
        for (int i = 0; i < INITIAL_THREADS; i++) {
            Runnable thread = () -> {
                client.runRequests(INITIAL_REQUESTS, client.ipAddr, false); // Using the ipAddr from the TestClient instance
                initialLatch.countDown();
            };
            new Thread(thread).start();
        }

        initialLatch.await();

        // Thread Groups
        CountDownLatch groupLatch = new CountDownLatch(client.threadGroupSize * client.numThreadGroups);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < client.numThreadGroups; i++) {
            Thread.sleep(client.delay * 1000L);
            for (int j = 0; j < client.threadGroupSize; j++) {
                Runnable thread = () -> {
                    client.runRequests(REQUESTS_PER_THREAD, client.ipAddr, true);
                    groupLatch.countDown();
                };
                new Thread(thread).start();
            }
        }

        groupLatch.await();
        CSVUtil.writeBufferedData();
        long endTime = System.currentTimeMillis();
        long wallTime = (endTime - startTime) / 1000;  // Convert from milliseconds to seconds


        int throughput = client.getVal() / (int) wallTime;
        System.out.println("Wall Time: " + wallTime + " seconds");
        System.out.println("Throughput: " + throughput + " requests/second");

        // Read data from CSV and display metrics
        List<RequestData> allData = CSVUtil.readCSV();
        MetricsCalculator.displayMetrics(allData);
        
        int successfulRequests = client.counter.getSuccessfulRequests();
        int failedRequests = client.counter.getFailedRequests();
        System.out.println();
        System.out.println("Number of successful requests: " + successfulRequests);
        System.out.println("Number of failed requests: " + failedRequests);
    }
}

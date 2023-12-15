package io.swagger.client.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MetricsCalculator {

    public static void displayMetrics(List<RequestData> data) {
        List<Long> postLatencies = new ArrayList<>();
        List<Long> getLatencies = new ArrayList<>();

        for (RequestData request : data) {
            if ("POST".equalsIgnoreCase(request.requestType)) {
                postLatencies.add(request.latency);
            } else if ("GET".equalsIgnoreCase(request.requestType)) {
                getLatencies.add(request.latency);
            }
        }

        System.out.println("POST Metrics:");
        printMetrics(postLatencies);

        System.out.println("\nGET Metrics:");
        printMetrics(getLatencies);
    }

    private static void printMetrics(List<Long> latencies) {
        if (latencies.isEmpty()) {
            System.out.println("No data available.");
            return;
        }

        Collections.sort(latencies);

        long sum = latencies.stream().mapToLong(Long::longValue).sum();
        double mean = sum / (double) latencies.size();
        long median = latencies.get(latencies.size() / 2);
        long p99 = latencies.get((int) (latencies.size() * 0.99));
        long min = latencies.get(0);
        long max = latencies.get(latencies.size() - 1);

        System.out.println("Mean Response Time: " + mean + " ms");
        System.out.println("Median Response Time: " + median + " ms");
        System.out.println("P99 Response Time: " + p99 + " ms");
        System.out.println("Min Response Time: " + min + " ms");
        System.out.println("Max Response Time: " + max + " ms");
    }
}

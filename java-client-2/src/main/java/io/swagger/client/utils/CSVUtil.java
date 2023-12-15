package io.swagger.client.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CSVUtil {
    private static final String CSV_FILE = "client_data.csv";
    private static ConcurrentLinkedQueue<RequestData> queue = new ConcurrentLinkedQueue<>();

    public static void addToQueue(RequestData data) {
        queue.add(data);
    }
    public static void writeBufferedData() {
        try (FileWriter writer = new FileWriter(CSV_FILE, true)) {
            while (!queue.isEmpty()) {
                RequestData data = queue.poll();
                writer.append(data.startTime + "," + data.requestType + "," + data.latency + "," + data.responseCode + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static synchronized void writeToCSV(long startTime, String requestType, long latency, int responseCode) {
        try (FileWriter writer = new FileWriter(CSV_FILE, true)) {
            writer.append(startTime + "," + requestType + "," + latency + "," + responseCode + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<RequestData> readCSV() {
        List<RequestData> requestDataList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(CSV_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                requestDataList.add(new RequestData(Long.parseLong(data[0]), data[1], Long.parseLong(data[2]), Integer.parseInt(data[3])));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return requestDataList;
    }

    public static void clearCSV() {
        try (FileWriter writer = new FileWriter(CSV_FILE)) {
            writer.write("");  // Write an empty string to clear content
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


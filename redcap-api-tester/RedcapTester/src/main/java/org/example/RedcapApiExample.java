package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class RedcapApiExample {

    public static void main(String[] args) {
        String apiToken = "F6A4D47F0DDE8AD8FB4A454C549304D4"; // Replace with your API token
        String redcapApiUrl = "https://your_redcap_instance.com/api/"; // Replace with your REDCap API URL

        try {
            // Prepare the URL and connection
            URL url = new URL(redcapApiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            // Prepare API parameters
            String data = "token=" + apiToken +
                    "&content=record" +
                    "&format=json" +
                    "&type=flat"; // Export records in JSON format

            // Send the request
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = data.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Read the response
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                // Output the response (exported data)
                System.out.println(response.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

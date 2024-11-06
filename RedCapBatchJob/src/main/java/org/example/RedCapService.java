package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class RedCapService {

    // Function to import records to RedCap with additional flags
    public static void importRecordToRedCap(String apiToken,  String projectUrl, List<Trainee> trainees) throws IOException, URISyntaxException {
        System.out.println("Starting RedCap import process...");

        // Create a URI and convert it to URL
        URL url = new URI(projectUrl).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        //Adding headers to the request
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        //connection.setRequestProperty("Authorization", "Bearer your_token_here"); // Example of an authorization header
        connection.setDoOutput(true); //Allows you to tack on data to body of request

        //Preparing Body (ie token, content, action ect)
        String traineesAsJsonArray = JSONUtility.toJsonArray(trainees); //Serialize the list of users to a JSON array
        System.out.println("Trainee JSON Array: " + traineesAsJsonArray);

        // Prepare form parameters
        Map<String, String> payload = new HashMap<>();
        payload.put("token", apiToken);
        payload.put("content", "record");
        payload.put("action", "import");
        payload.put("format", "json");
        payload.put("type", "flat");
        payload.put("overwriteBehavior", "normal");
        payload.put("forceAutoNumber", "false");
        payload.put("returnContent", "count");
        payload.put("returnFormat", "json");
        payload.put("data", traineesAsJsonArray);

        // Encode parameters into x-www-form-urlencoded format
        String formEncodedPayload = encodeFormParams(payload);
        System.out.println("Form-Encoded Payload: " + formEncodedPayload);

        //Write the encoded parameters to the request body
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = formEncodedPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Check the response code
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        String result = getResponse(connection,responseCode);
        System.out.println("Payload: " + result);
    }

    public static String getResponse(HttpURLConnection connection, int responseCode) throws IOException {
        // Check the response code
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (Scanner scanner = new Scanner(connection.getInputStream())) {
                StringBuilder response = new StringBuilder();
                while (scanner.hasNext()) {
                    response.append(scanner.nextLine());
                }
                System.out.println("Response: " + response);
                return response.toString();
            }
        } else {
            return "Unable to import records";
        }
    }
    // Encodes form parameters into "key1=value1&key2=value2" format for x-www-form-urlencoded content type
    public static String encodeFormParams(Map<String, String> params) {
        return params.entrySet().stream()
                .map(entry -> {
                    try {
                        return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name()) + "=" +
                                URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                    } catch (Exception e) {
                        throw new RuntimeException("Error encoding parameter: " + entry.getKey(), e);
                    }
                })
                .collect(Collectors.joining("&"));
    }

    // Method to write the encoded parameters to the request body
    static void writeRequestBody(HttpURLConnection connection, String encodedParams) throws IOException {
        // Ensure the connection is set for output
        connection.setDoOutput(true);

        // Write the encoded parameters to the output stream
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = encodedParams.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }
    }
}

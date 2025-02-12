package org.example;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;

public class RedCapService {


    /**
     * fetches the trainees from the Redcap API and formats the response as a set (researcher ids).
     * Also deals with errors if the API returned something other than an JSON Array of trainees
     *
     * @param projectUrl is the Redcap project URL
     * @param apiToken is the API token given by Redcap for a specific user
     * @return trainees a set of (researcher_id, first_name, last_name..ect) or throw an error if there is one
     */

    public static Set<Integer> fetchResearchIDFromRedcap(String apiToken, String projectUrl) throws URISyntaxException, IOException {

        //TODO: Ask Patrick –Should these error catching statments be somewhere else?
        JsonNode response = RedCapService.getResearchIDs(apiToken, projectUrl);
        if (response.isNull()) {
            System.err.println("API response is empty or null. Skipping processing.");
            //TODO: Ask Patrick should I Throw error here? Or how do you want to deal with API response being null?
        }

        if (response.isObject() && response.has("error")) {
            System.err.println("API returned an error: " + response.get("error").asText());
            //TODO: Ask Patrick should I throw error here? Or how do you want to deal with API returning an error message?
        }

        if (!response.isArray()) {
            System.err.println("Unexpected response format: " + response);
            //TODO: Ask Patrick should I throw error here? Or how do you want to deal the response not being returned as a JSON array as it should
        }

        //Converts the response into a set
        Set<Integer> researcherIdsSet = new HashSet<>();

        //No records yet created, return empty set
        if(response.isEmpty()) {
            researcherIdsSet = Collections.emptySet();
        }

        // Iterate over JSON array and extract researcher_id
        if (response.isArray()) {
            for (JsonNode researcher : response) {
                int researcherId = researcher.get("id_researcher").asInt();
                researcherIdsSet.add(researcherId);  // Ensures uniqueness
            }
        }
        //System.out.println(response.toPrettyString());
        return researcherIdsSet;
    }

    /**
     * Imports trainees into Redcap project
     *
     * @param projectUrl is the Redcap project URL
     * @param trainees is a JSON Array of trainees (researcher_id, first_name, last_name..ect)
     * @param apiToken is the API token given by Redcap for a specific user
     * @return returns a list of researcher_ids of participants in the Redcap project
     */

    // Function to import records to RedCap with additional flags
    public static JsonNode importRecordToRedCap(String projectUrl, JsonNode trainees, String apiToken) throws IOException, URISyntaxException {
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
        System.out.println("Trainee JSON Array: " + trainees);

        // Prepare form parameters
        Map<String, String> payload = new HashMap<>();
        payload.put("token", apiToken);
        payload.put("content", "record");
        payload.put("action", "import");
        payload.put("format", "json");
        payload.put("type", "flat");
        payload.put("overwriteBehavior", "normal");
        payload.put("forceAutoNumber", "true");
        payload.put("returnContent", "ids");
        payload.put("returnFormat", "json");

        // Convert JsonNode to a String for sending in the request
        ObjectMapper objectMapper = new ObjectMapper();
        String traineesJsonString = objectMapper.writeValueAsString(trainees);
        payload.put("data", traineesJsonString); //Required type String

        // Encode parameters into x-www-form-urlencoded format
        String formEncodedPayload = encodeFormParams(payload);
        System.out.println("Form-Encoded Payload: " + formEncodedPayload);

        //Write the encoded parameters to the request body
        System.out.println("writing api request to redcap...");
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = formEncodedPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        // Check the response
        JsonNode result = getResponse(connection);
        return result;
    }

    /**
     * Queries Redcap System for former trainees
     *
     * @param apiToken is the API token given by Redcap
     * @param projectUrl redcaps project URL
     * @return returns a list of researcher_ids of participants in the Redcap project
     */

    public static JsonNode getResearchIDs(String apiToken, String projectUrl) throws URISyntaxException, IOException {
        System.out.println("Getting id_researcherfrom RedCap ...");

        // Create a URI and convert it to URL
        URL url = new URI(projectUrl).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        //Adding headers to the request
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setRequestProperty("Accept", "application/json");
        //connection.setRequestProperty("Authorization", "Bearer your_token_here"); // Example of an authorization header
        connection.setDoOutput(true); //Allows you to tack on data to body of request

        // Prepare form parameters
        Map<String, String> payload = new HashMap<>();
        payload.put("token", apiToken);
        payload.put("content", "record");
        payload.put("action", "export");
        payload.put("format", "json");
        payload.put("type", "flat");
        payload.put("fields", "id_researcher");
        payload.put("forms", "trainee_contact");
        payload.put("events", "baseline_arm_1");
        payload.put("rawOrLabel", "raw");
        payload.put("exportSurveyFields", "false");
        payload.put("exportDataAccessGroups", "false");
        payload.put("returnFormat", "json");

        JsonNode response = NullNode.getInstance(); // Default value to ensure safe return

        //sends request
        try {
            String formEncodedPayload = encodeFormParams(payload);
            writeRequestBody(connection, formEncodedPayload);
        }
        catch (Exception e) {
            throw new IOException("Failed to send researcher ID request", e);
        }
        try{
            //
            response = getResponse(connection);
            //System.out.println(response.toPrettyString());
        }
        catch (final Exception e){
            throw new IOException("Error reading research ID response", e);
        }
        return response;
    }

    /**
     * Helper method for receiving responses from redcap
     *
     * @param connection represents the http connection
     * @return returns a response from the Redcap Server (A JSON Array with JSON Objects)
     */
    public static JsonNode getResponse(HttpURLConnection connection) throws IOException {
        System.out.println("receiving response from redcap...");
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);
        // Check the response code
        ObjectMapper objectMapper = new ObjectMapper();
        // 1️⃣ Handle HTTP errors gracefully
        if (responseCode >= 400) {
            //TODO: ask patrick how would you prefer I deal with exceptions?
            return readErrorResponse(connection);
        }

        if (responseCode == HttpURLConnection.HTTP_OK) {
            //if it's a normal response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return objectMapper.readTree(reader);
            } catch (Exception e){
                throw new IOException("Error reading JSON response", e);
            }
        } else {
            //if it is an error response
            try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
                return objectMapper.readTree(reader);
            } catch (Exception e){
                System.out.println("error reading the response error");
                e.printStackTrace();
            }
        }
        // Return NullNode if an error occurs (better than returning null)
        return NullNode.getInstance();
    }
    /**
     * Helper method for receiving error messages from Redap Servers
     *
     * @param connection represents the http connection
     * @return returns the error response from the Redcap Server
     */
    private static JsonNode readErrorResponse(HttpURLConnection connection) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()))) {
            return objectMapper.readTree(reader); // Parse error response as JSON
        } catch (Exception e) {
            throw new IOException("Error reading API error response", e);
        }
    }
    /**
     * Helper method which Encodes form parameters into "key1=value1&key2=value2" format for x-www-form-urlencoded content type
     *
     * @param params represents the key value pairs of parameters needed for the request
     * @return returns a serialized string version with the correct format
     */
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

    /**
     * Helper method for writing content over the network
     *
     * @param connection represents the http connection
     * @param encodedParams are the parameters in request that are to be written
     */
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
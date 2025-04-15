package app.ogs;


import app.ogs.model.Location;
import app.ogs.model.Sample;
import app.ogs.model.Statistics;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.Base64;
import java.util.List;
import java.util.logging.*;

public class BackendClient {

	private static final Logger logger = Logger.getLogger(BackendClient.class.getName());
	private static final String BASE_URL = "http://localhost:8080/api";
	// Accept this from UI
	private static final String CREDENTIALS = Base64.getEncoder().encodeToString("user:password".getBytes());
	
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper mapper = createObjectMapper();
    
    private static ObjectMapper createObjectMapper() {
    	 ObjectMapper mapper = new ObjectMapper();
    	 mapper.registerModule(new JavaTimeModule());
     	mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
     	return mapper;
    }
    
    private static HttpRequest.Builder requestBuilder(String url) {
    	return HttpRequest.newBuilder()
    			.header("Authorization", "Basic " + CREDENTIALS)
        .uri(URI.create(BASE_URL + url));
    }
    
    public static List<Sample> getAllSamples() {
        try {
            HttpRequest request = requestBuilder("/samples")
                    .GET()
                    .build();

            String response = send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response, new TypeReference<>() {
            });
        } catch (Exception ex) {
        	logger.log(Level.WARNING, "error fetching all samples", ex);
            return List.of();
        }
    }

    public static List<String> getLocations() {
        try {
            HttpRequest request =requestBuilder("/locations")
                    .GET()
                    .build();

            String response = send(request, HttpResponse.BodyHandlers.ofString());
            logger.log(Level.INFO, response);
            return mapper.readValue(response, new TypeReference<List<Location>>() {
            }).stream().map(l -> l.getName()).toList();
        } catch (Exception ex) {
        	logger.log(Level.WARNING, "error fetching all locations", ex);
            return List.of();
        }
    }

    public static void addSample(Sample sample) {
        try {
            String requestBody = mapper.writeValueAsString(sample);
            HttpRequest request = requestBuilder("/samples")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
        	logger.log(Level.WARNING, "error uploading sample", ex);
            throw new RuntimeException("Failed to upload sample: " + ex.getMessage());
        }
    }

    public static Statistics getStatistics() {
        try {
            HttpRequest request = requestBuilder("/samples/statistics")
                    .GET()
                    .build();

            String response = send(request, HttpResponse.BodyHandlers.ofString());
            return mapper.readValue(response, Statistics.class);
        } catch (Exception ex) {
        	logger.log(Level.WARNING, "error fetching statistics", ex);
            return new Statistics();
        }
    }

    public static void updateSample(Sample sample) {
        try {
            String json = mapper.writeValueAsString(sample);
        	logger.log(Level.INFO, "updating sample "+ sample.getId() + ": " + json );
            HttpRequest request = requestBuilder("/samples/" + sample.getId())
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
        	logger.log(Level.WARNING, "error updating sample " + sample.getId(), ex);
            throw new RuntimeException("Failed to update sample: " + ex.getMessage());
        }
    }

    public static void deleteSample(String sampleId) {
        try {
            HttpRequest request = requestBuilder("/samples/" + sampleId)
                    .DELETE()
                    .build();

            send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
        	logger.log(Level.WARNING, "error deleting sample", ex);
            throw new RuntimeException("Failed to delete sample: " + ex.getMessage());
        }
    }
    
    static String send(HttpRequest request, BodyHandler<String> bodyHandler) {
    	HttpResponse<String> response;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (IOException | InterruptedException e) {
			logger.log(Level.WARNING, "error fetching all samples");
        	throw new RuntimeException("Failed to request: " + request.uri());
		}
    	if (response.statusCode() >= 300) {
        	logger.log(Level.WARNING, "error fetching all samples:" + response.body());
        	throw new RuntimeException("Failed to request: " + request.uri());
        }
    	return response.body();
    }
   

}

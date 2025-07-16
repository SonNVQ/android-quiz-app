package app.quiz.data.remote;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.quiz.data.models.Flashcard;
import app.quiz.data.models.FlashcardGroup;
import app.quiz.data.models.PagedResponse;

/**
 * Service for handling flashcard-related API requests
 * Implements UC-06: View public flashcard list
 */
public class FlashcardService {
    private static final String TAG = "FlashcardService";
    private static final String BASE_URL = "https://learnlanguage-aggbd0h2h6grc6es.eastasia-01.azurewebsites.net";
    private static final String FLASHCARD_PAGED_ENDPOINT = "/api/Flashcard/paged";
    private static final String FLASHCARD_DETAIL_ENDPOINT = "/api/Flashcard";
    
    private static final int TIMEOUT_CONNECT = 10000; // 10 seconds
    private static final int TIMEOUT_READ = 15000; // 15 seconds
    
    private final ExecutorService executorService;
    
    // Singleton instance
    private static FlashcardService instance;
    
    private FlashcardService() {
        executorService = Executors.newCachedThreadPool();
    }
    
    public static synchronized FlashcardService getInstance() {
        if (instance == null) {
            instance = new FlashcardService();
        }
        return instance;
    }
    
    /**
     * Interface for API response callbacks
     */
    public interface FlashcardCallback<T> {
        void onSuccess(T result);
        void onError(String error, int statusCode);
    }
    
    /**
     * Get paged list of public flashcard groups
     * Implements UC-06 normal sequence step 3: retrieve all public flashcard sets
     * 
     * @param pageNumber Page number (default: 1)
     * @param pageSize Page size (default: 10, max: 100)
     * @param search Search term for filtering (optional)
     * @param callback Response callback
     */
    public void getPublicFlashcards(int pageNumber, int pageSize, String search, 
                                   FlashcardCallback<PagedResponse<FlashcardGroup>> callback) {
        executorService.execute(() -> {
            try {
                // Build query parameters
                StringBuilder queryParams = new StringBuilder();
                queryParams.append("?isPublic=true");
                queryParams.append("&pageNumber=").append(pageNumber);
                queryParams.append("&pageSize=").append(Math.min(pageSize, 100)); // Enforce max page size
                
                if (search != null && !search.trim().isEmpty()) {
                    queryParams.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8.toString()));
                }
                
                String endpoint = FLASHCARD_PAGED_ENDPOINT + queryParams.toString();
                String response = makeGetRequest(endpoint);
                
                PagedResponse<FlashcardGroup> pagedResponse = parsePagedFlashcardResponse(response);
                callback.onSuccess(pagedResponse);
                
            } catch (ApiException e) {
                Log.e(TAG, "Get public flashcards API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Get public flashcards unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }
    
    /**
     * Create a new flashcard group with flashcards
     * Implements UC-12 normal sequence: create flashcard
     * 
     * @param authToken User authentication token
     * @param name Flashcard group name
     * @param description Flashcard group description (optional)
     * @param isPublic Whether the flashcard group is public
     * @param flashcards List of flashcards to include
     * @param callback Response callback
     */
    public void createFlashcard(String authToken, String name, String description, boolean isPublic,
                               List<Flashcard> flashcards, FlashcardCallback<FlashcardGroup> callback) {
        executorService.execute(() -> {
            try {
                // Validate input
                if (authToken == null || authToken.trim().isEmpty()) {
                    callback.onError("Authentication required", 401);
                    return;
                }
                
                if (name == null || name.trim().isEmpty()) {
                    callback.onError("Flashcard title is required", 400);
                    return;
                }
                
                if (flashcards == null || flashcards.isEmpty()) {
                    callback.onError("At least one flashcard is required", 400);
                    return;
                }
                
                // Build request body
                JSONObject requestBody = new JSONObject();
                requestBody.put("name", name.trim());
                requestBody.put("description", description != null ? description.trim() : "");
                requestBody.put("isPublic", isPublic);
                
                JSONArray flashcardsArray = new JSONArray();
                for (Flashcard flashcard : flashcards) {
                    if (flashcard.getTerm() == null || flashcard.getTerm().trim().isEmpty() ||
                        flashcard.getDefinition() == null || flashcard.getDefinition().trim().isEmpty()) {
                        callback.onError("All flashcard terms and definitions are required", 400);
                        return;
                    }
                    
                    JSONObject flashcardJson = new JSONObject();
                    flashcardJson.put("term", flashcard.getTerm().trim());
                    flashcardJson.put("definition", flashcard.getDefinition().trim());
                    flashcardsArray.put(flashcardJson);
                }
                requestBody.put("flashcards", flashcardsArray);
                
                String response = makePostRequest(FLASHCARD_DETAIL_ENDPOINT, requestBody.toString(), authToken);
                FlashcardGroup createdGroup = parseFlashcardGroupResponse(response);
                callback.onSuccess(createdGroup);
                
            } catch (ApiException e) {
                Log.e(TAG, "Create flashcard API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Create flashcard unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }

    /**
     * Get paged list of user's private flashcard groups
     * Implements UC-08: View my flashcards - retrieve flashcards created by the user
     * 
     * @param authToken User authentication token
     * @param pageNumber Page number (default: 1)
     * @param pageSize Page size (default: 10, max: 100)
     * @param search Search term for filtering (optional)
     * @param callback Response callback
     */
    public void getUserFlashcards(String authToken, int pageNumber, int pageSize, String search, 
                                 FlashcardCallback<PagedResponse<FlashcardGroup>> callback) {
        executorService.execute(() -> {
            try {
                // Build query parameters
                StringBuilder queryParams = new StringBuilder();
                queryParams.append("?isPublic=false");
                queryParams.append("&pageNumber=").append(pageNumber);
                queryParams.append("&pageSize=").append(Math.min(pageSize, 100)); // Enforce max page size
                
                if (search != null && !search.trim().isEmpty()) {
                    queryParams.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8.toString()));
                }
                
                String endpoint = FLASHCARD_PAGED_ENDPOINT + queryParams.toString();
                String response = makeAuthenticatedGetRequest(endpoint, authToken);
                
                PagedResponse<FlashcardGroup> pagedResponse = parsePagedFlashcardResponse(response);
                callback.onSuccess(pagedResponse);
                
            } catch (ApiException e) {
                Log.e(TAG, "Get user flashcards API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Get user flashcards unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }
    
    /**
     * Get detailed flashcard group with all flashcards
     * 
     * @param groupId Flashcard group ID
     * @param callback Response callback
     */
    public void getFlashcardGroupDetails(String groupId, FlashcardCallback<FlashcardGroup> callback) {
        executorService.execute(() -> {
            try {
                String endpoint = FLASHCARD_DETAIL_ENDPOINT + "/" + groupId;
                String response = makeGetRequest(endpoint);
                
                FlashcardGroup flashcardGroup = parseFlashcardGroupDetails(response);
                callback.onSuccess(flashcardGroup);
                
            } catch (ApiException e) {
                Log.e(TAG, "Get flashcard details API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Get flashcard details unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }
    
    /**
     * Make HTTP GET request
     * 
     * @param endpoint API endpoint with query parameters
     * @return Response string
     * @throws ApiException If request fails
     */
    private String makeGetRequest(String endpoint) throws ApiException {
        return makeRequest(endpoint, "GET", null, null);
    }
    
    /**
     * Make authenticated HTTP GET request
     * 
     * @param endpoint API endpoint with query parameters
     * @param authToken Authentication token
     * @return Response string
     * @throws ApiException If request fails
     */
    private String makeAuthenticatedGetRequest(String endpoint, String authToken) throws ApiException {
        return makeRequest(endpoint, "GET", authToken, null);
    }
    
    /**
     * Make authenticated HTTP POST request
     * 
     * @param endpoint API endpoint
     * @param requestBody Request body JSON
     * @param authToken Authentication token
     * @return Response string
     * @throws ApiException If request fails
     */
    private String makePostRequest(String endpoint, String requestBody, String authToken) throws ApiException {
        return makeRequest(endpoint, "POST", authToken, requestBody);
    }
    
    /**
     * Make HTTP request with optional authentication
     * 
     * @param endpoint API endpoint with query parameters
     * @param method HTTP method
     * @param authToken Authentication token (optional)
     * @param requestBody Request body (optional)
     * @return Response string
     * @throws ApiException If request fails
     */
    private String makeRequest(String endpoint, String method, String authToken, String requestBody) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            
            // Configure connection
            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(TIMEOUT_CONNECT);
            connection.setReadTimeout(TIMEOUT_READ);
            
            // Add authentication header if token provided
            if (authToken != null && !authToken.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + authToken);
            }
            
            // Add request body if provided
            if (requestBody != null && !requestBody.trim().isEmpty()) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
            }
            
            int statusCode = connection.getResponseCode();
            
            // Read response
            BufferedReader reader;
            if (statusCode >= 200 && statusCode < 300) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8));
            }
            
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            if (statusCode >= 200 && statusCode < 300) {
                return response.toString();
            } else {
                String errorMessage = getErrorMessage(statusCode, response.toString());
                throw new ApiException(errorMessage, statusCode);
            }
            
        } catch (IOException e) {
            Log.e(TAG, "Network error: " + e.getMessage());
            throw new ApiException("Network connection failed", -1);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * Parse paged flashcard response from JSON
     * 
     * @param jsonResponse JSON response string
     * @return PagedResponse containing FlashcardGroup list
     * @throws JSONException If JSON parsing fails
     */
    private PagedResponse<FlashcardGroup> parsePagedFlashcardResponse(String jsonResponse) throws JSONException {
        JSONObject responseJson = new JSONObject(jsonResponse);
        
        PagedResponse<FlashcardGroup> pagedResponse = new PagedResponse<>();
        pagedResponse.setTotalCount(responseJson.getInt("totalCount"));
        pagedResponse.setPageNumber(responseJson.getInt("pageNumber"));
        pagedResponse.setPageSize(responseJson.getInt("pageSize"));
        pagedResponse.setTotalPages(responseJson.getInt("totalPages"));
        
        JSONArray itemsArray = responseJson.getJSONArray("items");
        List<FlashcardGroup> flashcardGroups = new ArrayList<>();
        
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject item = itemsArray.getJSONObject(i);
            FlashcardGroup group = new FlashcardGroup();
            group.setId(item.getString("id"));
            group.setName(item.getString("name"));
            group.setDescription(item.optString("description", ""));
            group.setPublic(item.getBoolean("isPublic"));
            
            flashcardGroups.add(group);
        }
        
        pagedResponse.setItems(flashcardGroups);
        return pagedResponse;
    }
    
    /**
     * Parse detailed flashcard group response from JSON
     * 
     * @param jsonResponse JSON response string
     * @return FlashcardGroup with all flashcards
     * @throws JSONException If JSON parsing fails
     */
    private FlashcardGroup parseFlashcardGroupDetails(String jsonResponse) throws JSONException {
        JSONObject responseJson = new JSONObject(jsonResponse);
        
        FlashcardGroup group = new FlashcardGroup();
        group.setId(responseJson.getString("id"));
        group.setName(responseJson.getString("name"));
        group.setDescription(responseJson.optString("description", ""));
        group.setPublic(responseJson.getBoolean("isPublic"));
        
        JSONArray flashcardsArray = responseJson.getJSONArray("flashcards");
        List<Flashcard> flashcards = new ArrayList<>();
        
        for (int i = 0; i < flashcardsArray.length(); i++) {
            JSONObject flashcardJson = flashcardsArray.getJSONObject(i);
            Flashcard flashcard = new Flashcard();
            flashcard.setTerm(flashcardJson.getString("term"));
            flashcard.setDefinition(flashcardJson.getString("definition"));
            
            flashcards.add(flashcard);
        }
        
        group.setFlashcards(flashcards);
        return group;
    }
    
    /**
     * Parse flashcard group response from JSON (for create operation)
     * 
     * @param jsonResponse JSON response string
     * @return FlashcardGroup object
     * @throws JSONException If JSON parsing fails
     */
    private FlashcardGroup parseFlashcardGroupResponse(String jsonResponse) throws JSONException {
        JSONObject responseJson = new JSONObject(jsonResponse);
        
        FlashcardGroup group = new FlashcardGroup();
        group.setId(responseJson.getString("id"));
        group.setName(responseJson.getString("name"));
        group.setDescription(responseJson.optString("description", ""));
        group.setPublic(responseJson.getBoolean("isPublic"));
        
        JSONArray flashcardsArray = responseJson.getJSONArray("flashcards");
        List<Flashcard> flashcards = new ArrayList<>();
        
        for (int i = 0; i < flashcardsArray.length(); i++) {
            JSONObject flashcardJson = flashcardsArray.getJSONObject(i);
            Flashcard flashcard = new Flashcard();
            flashcard.setTerm(flashcardJson.getString("term"));
            flashcard.setDefinition(flashcardJson.getString("definition"));
            
            flashcards.add(flashcard);
        }
        
        group.setFlashcards(flashcards);
        return group;
    }
    
    /**
     * Parse error message from API response
     * 
     * @param statusCode HTTP status code
     * @param responseBody Response body
     * @return User-friendly error message
     */
    private String getErrorMessage(int statusCode, String responseBody) {
        switch (statusCode) {
            case 400:
                return "Invalid request parameters";
            case 404:
                return "No flashcards found";
            case 500:
                return "Server error. Please try again later";
            default:
                return "An error occurred while loading flashcards";
        }
    }
    
    /**
     * Custom exception for API errors
     */
    public static class ApiException extends Exception {
        private final int statusCode;
        
        public ApiException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }
        
        public int getStatusCode() {
            return statusCode;
        }
    }
}
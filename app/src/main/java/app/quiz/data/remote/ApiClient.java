package app.quiz.data.remote;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import app.quiz.data.models.LoginRequest;
import app.quiz.data.models.LoginResponse;
import app.quiz.data.models.SignupRequest;
import app.quiz.data.models.User;

/**
 * API Client for handling HTTP requests to the LinguaRead backend
 * Base URL: https://learnlanguage-aggbd0h2h6grc6es.eastasia-01.azurewebsites.net/
 * Based on API documentation from api-docs/user.txt
 */
public class ApiClient {
    private static final String TAG = "ApiClient";
    private static final String BASE_URL = "https://learnlanguage-aggbd0h2h6grc6es.eastasia-01.azurewebsites.net";
    private static final String LOGIN_ENDPOINT = "/api/User/login";
    private static final String REGISTER_ENDPOINT = "/api/User/register";
    
    private static final int TIMEOUT_CONNECT = 10000; // 10 seconds
    private static final int TIMEOUT_READ = 15000; // 15 seconds
    
    private final ExecutorService executorService;
    
    // Singleton instance
    private static ApiClient instance;
    
    private ApiClient() {
        executorService = Executors.newCachedThreadPool();
    }
    
    public static synchronized ApiClient getInstance() {
        if (instance == null) {
            instance = new ApiClient();
        }
        return instance;
    }
    
    /**
     * Interface for API response callbacks
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error, int statusCode);
    }
    
    /**
     * Login user with email and password
     * @param loginRequest Login credentials
     * @param callback Response callback
     */
    public void login(LoginRequest loginRequest, ApiCallback<LoginResponse> callback) {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("Email", loginRequest.getEmail());
                requestBody.put("Password", loginRequest.getPassword());
                
                String response = makePostRequest(LOGIN_ENDPOINT, requestBody.toString());
                
                JSONObject responseJson = new JSONObject(response);
                LoginResponse loginResponse = new LoginResponse();
                loginResponse.setToken(responseJson.getString("token"));
                
                callback.onSuccess(loginResponse);
                
            } catch (ApiException e) {
                Log.e(TAG, "Login API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (JSONException e) {
                Log.e(TAG, "Login JSON parsing error: " + e.getMessage());
                callback.onError("Invalid response format", -1);
            } catch (Exception e) {
                Log.e(TAG, "Login unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }
    
    /**
     * Register new user
     * @param signupRequest User registration data
     * @param callback Response callback
     */
    public void register(SignupRequest signupRequest, ApiCallback<User> callback) {
        executorService.execute(() -> {
            try {
                JSONObject requestBody = new JSONObject();
                requestBody.put("Email", signupRequest.getEmail());
                requestBody.put("Password", signupRequest.getPassword());
                requestBody.put("UserName", signupRequest.getUserName());
                
                String response = makePostRequest(REGISTER_ENDPOINT, requestBody.toString());
                
                JSONObject responseJson = new JSONObject(response);
                User user = new User();
                user.setId(responseJson.getString("id"));
                user.setUserName(responseJson.getString("userName"));
                user.setEmail(responseJson.getString("email"));
                user.setActive(responseJson.getBoolean("isActive"));
                user.setRole(responseJson.getString("role"));
                
                callback.onSuccess(user);
                
            } catch (ApiException e) {
                Log.e(TAG, "Register API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (JSONException e) {
                Log.e(TAG, "Register JSON parsing error: " + e.getMessage());
                callback.onError("Invalid response format", -1);
            } catch (Exception e) {
                Log.e(TAG, "Register unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }
    
    /**
     * Make HTTP POST request
     * @param endpoint API endpoint
     * @param requestBody JSON request body
     * @return Response string
     * @throws ApiException If request fails
     */
    private String makePostRequest(String endpoint, String requestBody) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            
            // Configure connection
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setConnectTimeout(TIMEOUT_CONNECT);
            connection.setReadTimeout(TIMEOUT_READ);
            
            // Send request body
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
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
     * Parse error message from API response
     * @param statusCode HTTP status code
     * @param responseBody Response body
     * @return User-friendly error message
     */
    private String getErrorMessage(int statusCode, String responseBody) {
        switch (statusCode) {
            case 400:
                return "Invalid request. Please check your input.";
            case 401:
                return "Invalid credentials. Please try again.";
            case 404:
                return "Service not found. Please try again later.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return "An error occurred. Please try again.";
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
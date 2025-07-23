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

import app.quiz.data.models.PagedResponse;
import app.quiz.data.models.Reading;
import app.quiz.data.models.ReadingCreateDTO;
import app.quiz.data.models.ReadingQuestion;
import app.quiz.data.models.ReadingUpdateDto;

import java.io.OutputStream;

public class ReadingService {
    private static final String TAG = "ReadingService";
    private static final String BASE_URL = "https://learnlanguage-aggbd0h2h6grc6es.eastasia-01.azurewebsites.net";
    private static final String READING_PAGED_ENDPOINT = "/api/Reading/paged";
    private static final String READING_DETAIL_ENDPOINT = "/api/Reading";

    private static final int TIMEOUT_CONNECT = 10000; // 10 seconds
    private static final int TIMEOUT_READ = 15000; // 15 seconds

    private final ExecutorService executorService;

    // Singleton instance
    private static ReadingService instance;

    private ReadingService() {
        executorService = Executors.newCachedThreadPool();
    }

    public static synchronized ReadingService getInstance() {
        if (instance == null) {
            instance = new ReadingService();
        }
        return instance;
    }

    public interface ReadingCallback<T> {
        void onSuccess(T result);
        void onError(String error, int statusCode);
    }

    public void getPublicReadings(int pageNumber, int pageSize, String search,
                                  ReadingCallback<PagedResponse<Reading>> callback) {
        executorService.execute(() -> {
            try {
                StringBuilder queryParams = new StringBuilder();
                queryParams.append("?pageNumber=").append(pageNumber);
                queryParams.append("&pageSize=").append(Math.min(pageSize, 100));

                if (search != null && !search.trim().isEmpty()) {
                    queryParams.append("&search=").append(URLEncoder.encode(search.trim(), StandardCharsets.UTF_8.toString()));
                }

                String endpoint = READING_PAGED_ENDPOINT + queryParams.toString();
                String response = makeGetRequest(endpoint);

                PagedResponse<Reading> pagedResponse = parsePagedReadingResponse(response);
                callback.onSuccess(pagedResponse);

            } catch (ApiException e) {
                Log.e(TAG, "Get public readings API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Get public readings unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }

    public void getReadingById(String readingId, ReadingCallback<Reading> callback) {
        executorService.execute(() -> {
            try {
                String endpoint = READING_DETAIL_ENDPOINT + "/" + readingId;
                String response = makeGetRequest(endpoint);

                Reading reading = parseReadingResponse(response);
                callback.onSuccess(reading);

            } catch (ApiException e) {
                Log.e(TAG, "Get reading by id API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Get reading by id unexpected error: " + e.getMessage());
                callback.onError("Network error occurred", -1);
            }
        });
    }

    // Create reading (Admin only)
    public void createReading(String authToken, ReadingCreateDTO readingData, ReadingCallback<Reading> callback) {
        executorService.execute(() -> {
            try {
                // Validate input
                if (authToken == null || authToken.trim().isEmpty()) {
                    callback.onError("Authentication token is required", 401);
                    return;
                }
                
                if (readingData == null || !readingData.isValid()) {
                    callback.onError("Invalid reading data", 400);
                    return;
                }
                
                JSONObject requestBody = createReadingRequestBody(readingData);
                String response = makeRequest(READING_DETAIL_ENDPOINT, "POST", authToken, requestBody.toString());
                Reading reading = parseReadingResponse(response);
                callback.onSuccess(reading);
                
            } catch (ApiException e) {
                Log.e(TAG, "Create reading API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Create reading unexpected error: " + e.getMessage());
                callback.onError("Failed to create reading: " + e.getMessage(), -1);
            }
        });
    }
    
    // Update reading (Admin only)
    public void updateReading(String authToken, ReadingUpdateDto readingData, ReadingCallback<Reading> callback) {
        executorService.execute(() -> {
            try {
                // Validate input
                if (authToken == null || authToken.trim().isEmpty()) {
                    callback.onError("Authentication token is required", 401);
                    return;
                }
                
                if (readingData == null || !readingData.isValid()) {
                    callback.onError("Invalid reading data", 400);
                    return;
                }
                
                JSONObject requestBody = createUpdateReadingRequestBody(readingData);
                String response = makeRequest(READING_DETAIL_ENDPOINT, "PUT", authToken, requestBody.toString());
                Reading reading = parseReadingResponse(response);
                callback.onSuccess(reading);
                
            } catch (ApiException e) {
                Log.e(TAG, "Update reading API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Update reading unexpected error: " + e.getMessage());
                callback.onError("Failed to update reading: " + e.getMessage(), -1);
            }
        });
    }
    
    // Delete reading (Admin only)
    public void deleteReading(String authToken, String id, ReadingCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                // Validate input
                if (authToken == null || authToken.trim().isEmpty()) {
                    callback.onError("Authentication token is required", 401);
                    return;
                }
                
                if (id == null || id.trim().isEmpty()) {
                    callback.onError("Reading ID is required", 400);
                    return;
                }
                
                String endpoint = READING_DETAIL_ENDPOINT + "/" + id;
                makeRequest(endpoint, "DELETE", authToken, null);
                callback.onSuccess(null);
                
            } catch (ApiException e) {
                Log.e(TAG, "Delete reading API error: " + e.getMessage());
                callback.onError(e.getMessage(), e.getStatusCode());
            } catch (Exception e) {
                Log.e(TAG, "Delete reading unexpected error: " + e.getMessage());
                callback.onError("Failed to delete reading: " + e.getMessage(), -1);
            }
        });
    }

    private String makeGetRequest(String endpoint) throws ApiException {
        return makeRequest(endpoint, "GET", null, null);
    }

    private String makeRequest(String endpoint, String method, String authToken, String requestBody) throws ApiException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(BASE_URL + endpoint);
            connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("Accept", "application/json");
            connection.setConnectTimeout(TIMEOUT_CONNECT);
            connection.setReadTimeout(TIMEOUT_READ);

            if (authToken != null && !authToken.trim().isEmpty()) {
                connection.setRequestProperty("Authorization", "Bearer " + authToken);
            }

            if (requestBody != null && !requestBody.trim().isEmpty()) {
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.getOutputStream().write(requestBody.getBytes(StandardCharsets.UTF_8));
            }

            int statusCode = connection.getResponseCode();

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

    private String getErrorMessage(int statusCode, String response) {
        try {
            JSONObject errorJson = new JSONObject(response);
            return errorJson.optString("message", "An error occurred while loading readings");
        } catch (JSONException e) {
            return "An error occurred while loading readings";
        }
    }

    private PagedResponse<Reading> parsePagedReadingResponse(String jsonResponse) throws JSONException {
        JSONObject responseJson = new JSONObject(jsonResponse);

        PagedResponse<Reading> pagedResponse = new PagedResponse<>();
        pagedResponse.setTotalCount(responseJson.getInt("totalCount"));
        pagedResponse.setPageNumber(responseJson.getInt("pageNumber"));
        pagedResponse.setPageSize(responseJson.getInt("pageSize"));
        pagedResponse.setTotalPages(responseJson.getInt("totalPages"));

        JSONArray itemsArray = responseJson.getJSONArray("items");
        List<Reading> readings = new ArrayList<>();
        for (int i = 0; i < itemsArray.length(); i++) {
            JSONObject itemJson = itemsArray.getJSONObject(i);
            Reading reading = new Reading();
            reading.setId(itemJson.optString("id"));
            reading.setTitle(itemJson.optString("title"));
            reading.setDescription(itemJson.optString("description"));
            reading.setContent(itemJson.optString("content"));
            reading.setImageUrl(itemJson.optString("imageUrl"));
            reading.setUserId(itemJson.optString("userId"));
            
            // Parse questions
            JSONArray questionsArray = itemJson.optJSONArray("questions");
            if (questionsArray != null) {
                List<ReadingQuestion> questions = new ArrayList<>();
                for (int j = 0; j < questionsArray.length(); j++) {
                    JSONObject questionJson = questionsArray.getJSONObject(j);
                    ReadingQuestion question = parseReadingQuestion(questionJson);
                    questions.add(question);
                }
                reading.setQuestions(questions);
            }
            
            readings.add(reading);
        }
        pagedResponse.setItems(readings);
        return pagedResponse;
    }

    private Reading parseReadingResponse(String jsonResponse) throws JSONException {
        JSONObject itemJson = new JSONObject(jsonResponse);
        Reading reading = new Reading();
        reading.setId(itemJson.optString("id"));
        reading.setTitle(itemJson.optString("title"));
        reading.setDescription(itemJson.optString("description"));
        reading.setContent(itemJson.optString("content"));
        reading.setImageUrl(itemJson.optString("imageUrl"));
        reading.setUserId(itemJson.optString("userId"));
        
        // Parse questions
        JSONArray questionsArray = itemJson.optJSONArray("questions");
        if (questionsArray != null) {
            List<ReadingQuestion> questions = new ArrayList<>();
            for (int i = 0; i < questionsArray.length(); i++) {
                JSONObject questionJson = questionsArray.getJSONObject(i);
                ReadingQuestion question = parseReadingQuestion(questionJson);
                questions.add(question);
            }
            reading.setQuestions(questions);
        }
        
        return reading;
    }
    
    private ReadingQuestion parseReadingQuestion(JSONObject questionJson) throws JSONException {
        ReadingQuestion question = new ReadingQuestion();
        question.setId(questionJson.optString("id"));
        question.setQuestionText(questionJson.optString("questionText"));
        question.setQuestionType(questionJson.optInt("questionType"));
        
        // Parse single choice options
        if (question.getQuestionType() == ReadingQuestion.TYPE_SINGLE_CHOICE) {
            question.setOptionA(questionJson.optString("optionA"));
            question.setOptionB(questionJson.optString("optionB"));
            question.setOptionC(questionJson.optString("optionC"));
            question.setOptionD(questionJson.optString("optionD"));
            question.setCorrectOption(questionJson.optString("correctOption"));
        }
        
        // Parse fill in the blank answer
        if (question.getQuestionType() == ReadingQuestion.TYPE_FILL_IN_BLANK) {
            question.setAnswer(questionJson.optString("answer"));
        }
        
        return question;
    }

    // JSON request body creation methods
    private JSONObject createReadingRequestBody(ReadingCreateDTO readingData) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("title", readingData.getTitle());
        json.put("content", readingData.getContent());
        
        if (readingData.getImageUrl() != null && !readingData.getImageUrl().trim().isEmpty()) {
            json.put("imageUrl", readingData.getImageUrl());
        }
        
        if (readingData.getDescription() != null && !readingData.getDescription().trim().isEmpty()) {
            json.put("description", readingData.getDescription());
        }
        
        if (readingData.getQuestions() != null && !readingData.getQuestions().isEmpty()) {
            JSONArray questionsArray = new JSONArray();
            for (ReadingQuestion question : readingData.getQuestions()) {
                questionsArray.put(createQuestionJson(question));
            }
            json.put("questions", questionsArray);
        }
        
        return json;
    }
    
    private JSONObject createUpdateReadingRequestBody(ReadingUpdateDto readingData) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", readingData.getId());
        json.put("title", readingData.getTitle());
        json.put("content", readingData.getContent());
        
        if (readingData.getImageUrl() != null && !readingData.getImageUrl().trim().isEmpty()) {
            json.put("imageUrl", readingData.getImageUrl());
        }
        
        if (readingData.getDescription() != null && !readingData.getDescription().trim().isEmpty()) {
            json.put("description", readingData.getDescription());
        }
        
        if (readingData.getQuestions() != null && !readingData.getQuestions().isEmpty()) {
            JSONArray questionsArray = new JSONArray();
            for (ReadingQuestion question : readingData.getQuestions()) {
                questionsArray.put(createQuestionJson(question));
            }
            json.put("questions", questionsArray);
        }
        
        return json;
    }
    
    private JSONObject createQuestionJson(ReadingQuestion question) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("questionText", question.getQuestionText());
        json.put("questionType", question.getQuestionType());
        
        if (question.isSingleChoice()) {
            if (question.getOptionA() != null && !question.getOptionA().trim().isEmpty()) {
                json.put("optionA", question.getOptionA());
            }
            if (question.getOptionB() != null && !question.getOptionB().trim().isEmpty()) {
                json.put("optionB", question.getOptionB());
            }
            if (question.getOptionC() != null && !question.getOptionC().trim().isEmpty()) {
                json.put("optionC", question.getOptionC());
            }
            if (question.getOptionD() != null && !question.getOptionD().trim().isEmpty()) {
                json.put("optionD", question.getOptionD());
            }
            if (question.getCorrectOption() != null && !question.getCorrectOption().trim().isEmpty()) {
                json.put("correctOption", question.getCorrectOption());
            }
        } else if (question.isFillInBlank()) {
            if (question.getAnswer() != null && !question.getAnswer().trim().isEmpty()) {
                json.put("answer", question.getAnswer());
            }
        }
        
        return json;
    }

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
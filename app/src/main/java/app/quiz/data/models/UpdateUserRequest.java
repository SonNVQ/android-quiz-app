package app.quiz.data.models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Request model for updating user profile
 * Based on UpdateAppUserDto from API documentation
 * All fields are required according to the API spec
 */
public class UpdateUserRequest {
    private String userName;
    private String email;
    private String password;
    private boolean isActive;
    private String role;

    public UpdateUserRequest() {}

    public UpdateUserRequest(String userName, String email, String password, boolean isActive, String role) {
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.isActive = isActive;
        this.role = role;
    }

    // Getters
    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Converts the request to JSON format for API call
     * @return JSON string representation
     * @throws JSONException if JSON creation fails
     */
    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("userName", userName);
        json.put("email", email);
        // Always include password field
        // Use null to indicate no password change
        json.put("password", password);
        json.put("isActive", isActive);
        json.put("role", role);
        return json.toString();
    }

    /**
     * Validates the request according to API requirements
     * @return true if valid, false otherwise
     */
    public boolean isValid() {
        return userName != null && !userName.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               (password != null && password.length() >= 6 && password.length() <= 100) &&
               role != null && ("Admin".equals(role) || "User".equals(role) || "Student".equals(role));
    }
}
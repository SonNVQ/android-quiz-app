package app.quiz.data.models;

/**
 * Login response model for API authentication
 * Based on POST /api/User/login endpoint response from api-docs/user.txt
 */
public class LoginResponse {
    private String token;

    // Default constructor
    public LoginResponse() {}

    // Constructor with token
    public LoginResponse(String token) {
        this.token = token;
    }

    // Getter
    public String getToken() {
        return token;
    }

    // Setter
    public void setToken(String token) {
        this.token = token;
    }

    // Validation method
    public boolean isValid() {
        return token != null && !token.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "token='[PROTECTED]'" +
                '}';
    }
}
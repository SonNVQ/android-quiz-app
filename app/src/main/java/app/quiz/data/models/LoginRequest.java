package app.quiz.data.models;

/**
 * Login request model for API authentication
 * Based on POST /api/User/login endpoint from api-docs/user.txt
 */
public class LoginRequest {
    private String email;
    private String password;

    // Default constructor
    public LoginRequest() {}

    // Constructor with parameters
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // Validation methods
    public boolean isValid() {
        return isValidEmail() && isValidPassword();
    }

    public boolean isValidEmail() {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPassword() {
        return password != null && !password.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "LoginRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                '}';
    }
}
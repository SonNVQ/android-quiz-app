package app.quiz.data.models;

import java.util.regex.Pattern;

/**
 * Signup request model for user registration
 * Based on POST /api/User/register endpoint from api-docs/user.txt
 */
public class SignupRequest {
    private String email;
    private String password;
    private String userName;

    // Password validation pattern - at least 8 chars, 1 uppercase, 1 number, 1 special char
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
    );

    // Default constructor
    public SignupRequest() {}

    // Constructor with parameters
    public SignupRequest(String email, String password, String userName) {
        this.email = email;
        this.password = password;
        this.userName = userName;
    }

    // Getters
    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    // Setters
    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    // Validation methods
    public boolean isValid() {
        return isValidEmail() && isValidPassword() && isValidUserName();
    }

    public boolean isValidEmail() {
        return email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public boolean isValidPassword() {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public boolean isValidUserName() {
        return userName != null && !userName.trim().isEmpty() && userName.length() >= 3;
    }

    // Validation error messages
    public String getEmailError() {
        if (email == null || email.trim().isEmpty()) {
            return "Email is required";
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return "Please enter a valid email address";
        }
        return null;
    }

    public String getPasswordError() {
        if (password == null || password.trim().isEmpty()) {
            return "Password is required";
        }
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must be at least 8 characters with uppercase, number, and special character";
        }
        return null;
    }

    public String getUserNameError() {
        if (userName == null || userName.trim().isEmpty()) {
            return "Username is required";
        }
        if (userName.length() < 3) {
            return "Username must be at least 3 characters";
        }
        return null;
    }

    @Override
    public String toString() {
        return "SignupRequest{" +
                "email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", userName='" + userName + '\'' +
                '}';
    }
}
package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.quiz.MainActivity;
import app.quiz.R;
import app.quiz.data.models.SignupRequest;
import app.quiz.data.models.User;
import app.quiz.data.remote.ApiClient;
import app.quiz.utils.SessionManager;

/**
 * SignupActivity implements UC-03 Signup use case
 * Allows users to create a personal account on the LinguaRead platform
 */
public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    
    // UI Components
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignup;
    private TextView tvLoginLink, tvError;
    private ProgressBar progressBar;
    
    // Dependencies
    private ApiClient apiClient;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        initializeComponents();
        setupClickListeners();
        checkExistingSession();
    }
    
    /**
     * Initialize UI components and dependencies
     */
    private void initializeComponents() {
        // Initialize UI components
        tilUsername = findViewById(R.id.til_username);
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        tilConfirmPassword = findViewById(R.id.til_confirm_password);
        etUsername = findViewById(R.id.et_username);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSignup = findViewById(R.id.btn_signup);
        tvLoginLink = findViewById(R.id.tv_login_link);
        tvError = findViewById(R.id.tv_error);
        progressBar = findViewById(R.id.progress_bar);
        
        // Initialize dependencies
        apiClient = ApiClient.getInstance();
        sessionManager = SessionManager.getInstance(this);
        
        Log.d(TAG, "Components initialized");
    }
    
    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        btnSignup.setOnClickListener(v -> attemptSignup());
        
        tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
    
    /**
     * Check if user is already logged in (PRE-01: User has not yet created an account)
     */
    private void checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to main activity");
            navigateToMainActivity();
        }
    }
    
    /**
     * Attempt user signup with validation
     * Implements the normal sequence from UC-03
     */
    private void attemptSignup() {
        // Clear previous errors
        clearErrors();
        
        // Get input values
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Validate inputs
        if (!validateInputs(username, email, password, confirmPassword)) {
            return;
        }
        
        // Create signup request
        SignupRequest signupRequest = new SignupRequest(email, password, username);
        
        // Show loading state
        setLoadingState(true);
        
        // Make API call
        apiClient.register(signupRequest, new ApiClient.ApiCallback<User>() {
            @Override
            public void onSuccess(User result) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    handleSignupSuccess(result);
                });
            }
            
            @Override
            public void onError(String error, int statusCode) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    handleSignupError(error, statusCode);
                });
            }
        });
    }
    
    /**
     * Validate user inputs
     * Implements BR-01: Unique Email Requirement and BR-02: Password Policy
     * @param username User username
     * @param email User email
     * @param password User password
     * @param confirmPassword Password confirmation
     * @return true if inputs are valid
     */
    private boolean validateInputs(String username, String email, String password, String confirmPassword) {
        boolean isValid = true;
        
        // Validate username
        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters long");
            isValid = false;
        } else if (!username.matches("^[a-zA-Z0-9_]+$")) {
            tilUsername.setError("Username can only contain letters, numbers, and underscores");
            isValid = false;
        }
        
        // Validate email (BR-01: Unique Email Requirement)
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        // Validate password (BR-02: Password Policy)
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!isValidPassword(password)) {
            tilPassword.setError(getString(R.string.error_weak_password));
            isValid = false;
        }
        
        // Validate password confirmation
        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.error_password_mismatch));
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Validate password according to BR-02: Password Policy
     * Password must be at least 8 characters long and include at least one uppercase letter, 
     * one number, and one special character
     * @param password Password to validate
     * @return true if password meets policy requirements
     */
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        
        boolean hasUppercase = password.matches(".*[A-Z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':,.<>?].*");
        
        return hasUppercase && hasNumber && hasSpecialChar;
    }
    
    /**
     * Handle successful signup response
     * Implements POST-01 and POST-02 from UC-03
     * @param user Created user object
     */
    private void handleSignupSuccess(User user) {
        Log.d(TAG, "Signup successful for user: " + user.getEmail());
        
        // Show success message
        Toast.makeText(this, getString(R.string.success_signup), Toast.LENGTH_LONG).show();
        
        // Navigate to login screen for user to login
        // Note: According to UC-03, user should be logged in automatically,
        // but since the API doesn't return a token, we redirect to login
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        intent.putExtra("email", user.getEmail());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Handle signup error
     * Implements AS-01: Invalid Input and AS-02: Existing Account
     * @param error Error message
     * @param statusCode HTTP status code
     */
    private void handleSignupError(String error, int statusCode) {
        Log.e(TAG, "Signup failed: " + error + " (Status: " + statusCode + ")");
        
        // Show appropriate error message
        if (statusCode == 400) {
            if (error.toLowerCase().contains("email")) {
                showError(getString(R.string.error_email_exists));
            } else if (error.toLowerCase().contains("username")) {
                showError("Username already exists. Please choose a different username.");
            } else {
                showError(getString(R.string.error_invalid_input));
            }
        } else if (statusCode == -1) {
            showError(getString(R.string.error_network));
        } else {
            showError(getString(R.string.error_server));
        }
    }
    
    /**
     * Navigate to main activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    /**
     * Set loading state for UI
     * @param isLoading true to show loading state
     */
    private void setLoadingState(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSignup.setEnabled(!isLoading);
        btnSignup.setText(isLoading ? getString(R.string.loading_signup) : getString(R.string.signup_button));
        
        // Disable input fields during loading
        etUsername.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }
    
    /**
     * Show error message
     * @param message Error message to display
     */
    private void showError(String message) {
        tvError.setText(message);
        tvError.setVisibility(View.VISIBLE);
    }
    
    /**
     * Clear all error messages and field errors
     */
    private void clearErrors() {
        tvError.setVisibility(View.GONE);
        tilUsername.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}
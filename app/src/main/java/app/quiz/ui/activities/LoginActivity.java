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
import app.quiz.data.models.LoginRequest;
import app.quiz.data.models.User;
import app.quiz.data.models.LoginResponse;
import app.quiz.data.remote.ApiClient;
import app.quiz.utils.SessionManager;

/**
 * LoginActivity implements UC-01 Login use case
 * Allows users and administrators to securely access the LinguaRead platform
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    
    // UI Components
    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView tvSignupLink, tvForgotPassword, tvError;
    private ProgressBar progressBar;
    
    // Dependencies
    private ApiClient apiClient;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        initializeComponents();
        setupClickListeners();
        checkExistingSession();
    }
    
    /**
     * Initialize UI components and dependencies
     */
    private void initializeComponents() {
        // Initialize UI components
        tilEmail = findViewById(R.id.til_email);
        tilPassword = findViewById(R.id.til_password);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignupLink = findViewById(R.id.tv_signup_link);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
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
        btnLogin.setOnClickListener(v -> attemptLogin());
        
        tvSignupLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });
        
        tvForgotPassword.setOnClickListener(v -> {
            // TODO: Implement forgot password functionality
            Toast.makeText(this, "Forgot password feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * Check if user is already logged in (PRE-02: Application must be running)
     */
    private void checkExistingSession() {
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "User already logged in, redirecting to main activity");
            navigateToMainActivity();
        }
    }
    
    /**
     * Attempt user login with validation
     * Implements the normal sequence from UC-01
     */
    private void attemptLogin() {
        // Check if account is locked (BR-02: Login Attempt Limit)
        if (sessionManager.isAccountLocked()) {
            long remainingTime = sessionManager.getRemainingLockoutTime();
            int remainingMinutes = (int) (remainingTime / (60 * 1000)) + 1;
            showError(getString(R.string.error_account_locked) + " Try again in " + remainingMinutes + " minutes.");
            return;
        }
        
        // Clear previous errors
        clearErrors();
        
        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        
        // Validate inputs
        if (!validateInputs(email, password)) {
            return;
        }
        
        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);
        
        // Show loading state
        setLoadingState(true);
        
        // Make API call
        apiClient.login(loginRequest, new ApiClient.ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse result) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    handleLoginSuccess(result);
                });
            }
            
            @Override
            public void onError(String error, int statusCode) {
                runOnUiThread(() -> {
                    setLoadingState(false);
                    handleLoginError(error, statusCode);
                });
            }
        });
    }
    
    /**
     * Validate user inputs
     * @param email User email
     * @param password User password
     * @return true if inputs are valid
     */
    private boolean validateInputs(String email, String password) {
        boolean isValid = true;
        
        // Validate email
        if (TextUtils.isEmpty(email)) {
            tilEmail.setError(getString(R.string.error_empty_fields));
            isValid = false;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError(getString(R.string.error_invalid_email));
            isValid = false;
        }
        
        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_empty_fields));
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Handle successful login response
     * Implements POST-01 and POST-02 from UC-01
     * @param loginResponse Login response with token
     */
    private void handleLoginSuccess(LoginResponse loginResponse) {
        Log.d(TAG, "Login successful");
        
        // Get the email from the input field to create user object
        String email = etEmail.getText().toString().trim();
        
        // Create user object with available information
        User user = new User();
        user.setEmail(email);
        user.setUserName(email.split("@")[0]); // Use email prefix as username
        user.setRole("Student"); // Default role
        user.setActive(true);
        
        // Create user session with both token and user data (POST-02: System maintains active session)
        sessionManager.createSession(loginResponse.getToken(), user);
        
        // Show success message
        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
        
        // Navigate to main activity (POST-01: Redirect to dashboard)
        navigateToMainActivity();
    }
    
    /**
     * Handle login error
     * Implements AS-01: Invalid Credentials and exception handling
     * @param error Error message
     * @param statusCode HTTP status code
     */
    private void handleLoginError(String error, int statusCode) {
        Log.e(TAG, "Login failed: " + error + " (Status: " + statusCode + ")");
        
        // Record failed attempt for BR-02: Login Attempt Limit
        sessionManager.recordFailedLoginAttempt();
        
        // Show appropriate error message
        if (statusCode == 401) {
            showError(getString(R.string.error_login_failed));
        } else if (statusCode == -1) {
            showError(getString(R.string.error_network));
        } else {
            showError(getString(R.string.error_server));
        }
        
        // Show remaining attempts warning
        int remainingAttempts = sessionManager.getRemainingLoginAttempts();
        if (remainingAttempts <= 2 && remainingAttempts > 0) {
            Toast.makeText(this, "Warning: " + remainingAttempts + " attempts remaining", 
                    Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Navigate to main activity
     */
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
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
        btnLogin.setEnabled(!isLoading);
        btnLogin.setText(isLoading ? getString(R.string.loading_login) : getString(R.string.login_button));
        
        // Disable input fields during loading
        etEmail.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
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
        tilEmail.setError(null);
        tilPassword.setError(null);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update activity timestamp for session management
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateActivity();
        }
    }
}
package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import app.quiz.R;
import app.quiz.data.models.LoginRequest;
import app.quiz.data.models.LoginResponse;
import app.quiz.data.models.UpdateUserRequest;
import app.quiz.data.models.User;
import app.quiz.data.remote.ApiClient;
import app.quiz.ui.activities.LoginActivity;
import app.quiz.utils.SessionManager;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";
    
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnChangePassword;
    private Button btnCancel;
    
    private ApiClient apiClient;
    private SessionManager sessionManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        
        initializeComponents();
        setupToolbar();
        setupClickListeners();
    }
    
    private void initializeComponents() {
        // Initialize UI components
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnChangePassword = findViewById(R.id.btn_change_password);
        btnCancel = findViewById(R.id.btn_cancel);
        
        // Initialize API client and session manager
        apiClient = ApiClient.getInstance();
        sessionManager = SessionManager.getInstance(this);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Change Password");
        }
    }
    
    private void setupClickListeners() {
        btnChangePassword.setOnClickListener(v -> changePassword());
        btnCancel.setOnClickListener(v -> finish());
    }
    
    private void changePassword() {
        if (!validatePasswordInput()) {
            return;
        }
        
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        
        // Disable button and show loading state
        btnChangePassword.setEnabled(false);
        btnChangePassword.setText("Changing Password...");
        
        // First verify current password by attempting login
        String userEmail = sessionManager.getUserEmail();
        LoginRequest loginRequest = new LoginRequest(userEmail, currentPassword);
        
        apiClient.login(loginRequest, new ApiClient.ApiCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                Log.d(TAG, "Current password verified, proceeding with password change");
                
                // Current password is correct, now update with new password
                // Get current user data to populate all required fields
                User currentUser = sessionManager.getCurrentUser();
                if (currentUser == null) {
                    runOnUiThread(() -> {
                        btnChangePassword.setEnabled(true);
                        btnChangePassword.setText("Change Password");
                        Toast.makeText(ChangePasswordActivity.this,
                                "Unable to get user data. Please try again.",
                                Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Create update request with all required fields
                UpdateUserRequest updateRequest = new UpdateUserRequest(
                    currentUser.getUserName(),
                    currentUser.getEmail(),
                    newPassword,
                    currentUser.isActive(),
                    currentUser.getRole()
                );
                
                Log.d(TAG, "Updating user with data: userName=" + currentUser.getUserName() + 
                          ", email=" + currentUser.getEmail() + ", role=" + currentUser.getRole() + 
                          ", isActive=" + currentUser.isActive());
                
                String authToken = sessionManager.getAuthToken();
                apiClient.updateUser(updateRequest, authToken, new ApiClient.ApiCallback<Void>() {
                    @Override
                    public void onSuccess(Void response) {
                        runOnUiThread(() -> {
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Password changed successfully. Please log in again.",
                                    Toast.LENGTH_LONG).show();
                            
                            // Clear session and redirect to login
                            sessionManager.clearSession();
                            Intent intent = new Intent(ChangePasswordActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        });
                    }
                    
                    @Override
                    public void onError(String error, int statusCode) {
                        runOnUiThread(() -> {
                            btnChangePassword.setEnabled(true);
                            btnChangePassword.setText("Change Password");
                            Toast.makeText(ChangePasswordActivity.this,
                                    "Failed to change password: " + error,
                                    Toast.LENGTH_LONG).show();
                        });
                    }
                });
            }
            
            @Override
            public void onError(String error, int statusCode) {
                runOnUiThread(() -> {
                    btnChangePassword.setEnabled(true);
                    btnChangePassword.setText("Change Password");
                    Toast.makeText(ChangePasswordActivity.this,
                            "Current password is incorrect",
                            Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
    
    private boolean validatePasswordInput() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();
        
        // Check if current password is provided
        if (TextUtils.isEmpty(currentPassword)) {
            etCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return false;
        }
        
        // Check if new password is provided
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return false;
        }
        
        // Check new password length
        if (newPassword.length() < 8) {
            etNewPassword.setError("Password must be at least 8 characters long");
            etNewPassword.requestFocus();
            return false;
        }
        
        // Check if confirm password is provided
        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Please confirm your new password");
            etConfirmPassword.requestFocus();
            return false;
        }
        
        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            etConfirmPassword.requestFocus();
            return false;
        }
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
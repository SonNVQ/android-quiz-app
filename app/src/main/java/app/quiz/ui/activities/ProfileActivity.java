package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;

import app.quiz.MainActivity;
import app.quiz.R;
import app.quiz.data.models.User;
import app.quiz.data.remote.ApiClient;
import app.quiz.ui.activities.LoginActivity;
import app.quiz.utils.SessionManager;

/**
 * ProfileActivity handles viewing and updating user profile information
 * Implements UC-04 (View Profile) and UC-05 (Update Profile)
 */
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    
    // UI Components
    private TextView tvUserName;
    private TextView tvEmail;
    private Chip tvRole;
    private Chip tvStatus;
    private TextView tvLearningStats;
    private Button btnEditProfile;
    private Button btnLogout;
    private ImageView ivProfilePicture;
    
    // Edit mode components
    private EditText etEditUserName;
    private EditText etEditEmail;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private Button btnSaveChanges;
    private Button btnCancelEdit;
    
    // Data
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private User currentUser;
    private boolean isEditMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        initializeComponents();
        setupClickListeners();
        setupBackPressHandler();
        loadUserProfile();
    }
    
    private void initializeComponents() {
        sessionManager = SessionManager.getInstance(this);
        apiClient = ApiClient.getInstance();
        
        // View mode components
        tvUserName = findViewById(R.id.tv_user_name);
        tvEmail = findViewById(R.id.tv_email);
        tvRole = findViewById(R.id.chip_role);
        tvStatus = findViewById(R.id.chip_status);
        tvLearningStats = findViewById(R.id.tv_learning_stats);
        btnEditProfile = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
        
        // Edit mode components
        etEditUserName = findViewById(R.id.et_edit_user_name);
        etEditEmail = findViewById(R.id.et_edit_email);
        etCurrentPassword = findViewById(R.id.et_current_password);
        etNewPassword = findViewById(R.id.et_new_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        btnSaveChanges = findViewById(R.id.btn_save_changes);
        btnCancelEdit = findViewById(R.id.btn_cancel_edit);
    }
    
    private void setupClickListeners() {
        btnEditProfile.setOnClickListener(v -> toggleEditMode());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        btnSaveChanges.setOnClickListener(v -> saveProfileChanges());
        btnCancelEdit.setOnClickListener(v -> cancelEdit());
    }
    
    private void loadUserProfile() {
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }
        
        // Try to get user from session first
        currentUser = sessionManager.getCurrentUser();
        
        if (currentUser != null && !TextUtils.isEmpty(currentUser.getEmail())) {
            // We have user data from session
            displayUserProfile();
        } else {
            // Fallback: create basic user profile from session email
            String userEmail = sessionManager.getUserEmail();
            if (!TextUtils.isEmpty(userEmail)) {
                createBasicUserProfile(userEmail);
            } else {
                showNoDataMessage();
            }
        }
    }
    
    private void createBasicUserProfile(String email) {
        // Create a basic user profile when session data is incomplete
        currentUser = new User();
        currentUser.setEmail(email);
        currentUser.setUserName(email.split("@")[0]); // Use email prefix as username
        currentUser.setRole("Student");
        currentUser.setActive(true);
        
        displayUserProfile();
    }
    
    private void displayUserProfile() {
        if (currentUser == null) {
            showNoDataMessage();
            return;
        }
        
        tvUserName.setText(currentUser.getUserName());
        tvEmail.setText(currentUser.getEmail());
        tvRole.setText(currentUser.getRole());
        tvStatus.setText(currentUser.isActive() ? "Active" : "Inactive");
        
        // Mock learning statistics
        tvLearningStats.setText("Articles Read: 0\nTime Spent: 0 minutes\nVocabulary Mastered: 0 words");
        
        // Set default profile picture
        ivProfilePicture.setImageResource(R.drawable.ic_profile_default);
    }
    
    private void showNoDataMessage() {
        tvUserName.setText("Guest User");
        tvEmail.setText("No email available");
        tvRole.setText("Guest");
        tvStatus.setText("Not logged in");
        tvLearningStats.setText("No learning data available yet. Start reading to build your progress!");
        
        btnEditProfile.setEnabled(false);
    }
    
    private void toggleEditMode() {
        isEditMode = !isEditMode;
        
        if (isEditMode) {
            showEditMode();
        } else {
            showViewMode();
        }
    }
    
    private void showEditMode() {
        // Hide view mode components
        tvUserName.setVisibility(View.GONE);
        tvEmail.setVisibility(View.GONE);
        btnEditProfile.setVisibility(View.GONE);
        
        // Show edit mode components
        findViewById(R.id.til_edit_user_name).setVisibility(View.VISIBLE);
        findViewById(R.id.til_edit_email).setVisibility(View.VISIBLE);
        findViewById(R.id.card_password_section).setVisibility(View.VISIBLE);
        btnSaveChanges.setVisibility(View.VISIBLE);
        btnCancelEdit.setVisibility(View.VISIBLE);
        
        // Populate edit fields with current data
        if (currentUser != null) {
            etEditUserName.setText(currentUser.getUserName());
            etEditEmail.setText(currentUser.getEmail());
        }
        
        // Clear password fields
        etCurrentPassword.setText("");
        etNewPassword.setText("");
        etConfirmPassword.setText("");
    }
    
    private void showViewMode() {
        // Show view mode components
        tvUserName.setVisibility(View.VISIBLE);
        tvEmail.setVisibility(View.VISIBLE);
        btnEditProfile.setVisibility(View.VISIBLE);
        
        // Hide edit mode components
        findViewById(R.id.til_edit_user_name).setVisibility(View.GONE);
        findViewById(R.id.til_edit_email).setVisibility(View.GONE);
        findViewById(R.id.card_password_section).setVisibility(View.GONE);
        btnSaveChanges.setVisibility(View.GONE);
        btnCancelEdit.setVisibility(View.GONE);
    }
    
    private void saveProfileChanges() {
        String newUserName = etEditUserName.getText().toString().trim();
        String newEmail = etEditEmail.getText().toString().trim();
        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();
        
        // Validate input
        if (!validateProfileInput(newUserName, newEmail, newPassword, confirmPassword)) {
            return;
        }
        
        // Update user object
        if (currentUser != null) {
            currentUser.setUserName(newUserName);
            currentUser.setEmail(newEmail);
            
            // Update session with new user data
            String currentToken = sessionManager.getAuthToken();
            if (currentToken != null) {
                sessionManager.createSession(currentToken, currentUser);
            }
        }
        
        // In a real implementation, this would call the API to update the profile
        // For now, we'll just update the display and show success message
        displayUserProfile();
        showViewMode();
        isEditMode = false;
        
        Toast.makeText(this, getString(R.string.profile_update_success), Toast.LENGTH_SHORT).show();
    }
    
    private boolean validateProfileInput(String userName, String email, String newPassword, String confirmPassword) {
        // Validate username
        if (TextUtils.isEmpty(userName) || userName.length() < 3) {
            etEditUserName.setError(getString(R.string.error_invalid_username));
            etEditUserName.requestFocus();
            return false;
        }
        
        // Validate email
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEditEmail.setError(getString(R.string.error_invalid_email));
            etEditEmail.requestFocus();
            return false;
        }
        
        // Validate password if provided
        if (!TextUtils.isEmpty(newPassword)) {
            if (newPassword.length() < 6) {
                etNewPassword.setError(getString(R.string.error_weak_password));
                etNewPassword.requestFocus();
                return false;
            }
            
            if (!newPassword.equals(confirmPassword)) {
                etConfirmPassword.setError(getString(R.string.error_password_mismatch));
                etConfirmPassword.requestFocus();
                return false;
            }
        }
        
        return true;
    }
    
    private void cancelEdit() {
        showViewMode();
        isEditMode = false;
    }
    
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_logout))
                .setMessage(getString(R.string.logout_confirmation_message))
                .setPositiveButton(getString(R.string.logout), (dialog, which) -> performLogout())
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }
    
    private void performLogout() {
        sessionManager.clearSession();
        redirectToLogin();
    }
    
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isEditMode) {
                    cancelEdit();
                } else {
                    // Go back to MainActivity
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
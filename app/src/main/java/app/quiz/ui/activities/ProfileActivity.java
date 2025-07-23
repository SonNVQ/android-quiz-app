package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

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
    private Toolbar toolbar;
    private TextView tvUserName;
    private TextView tvEmail;
    private Chip tvRole;
    private Chip tvStatus;

    private Button btnChangePassword;
    private Button btnLogout;
    private ImageView ivProfilePicture;
    
    // Data
    private SessionManager sessionManager;
    private ApiClient apiClient;
    private User currentUser;
    
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
        
        // Setup toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // View mode components
        tvUserName = findViewById(R.id.tv_user_name);
        tvEmail = findViewById(R.id.tv_email);
        tvRole = findViewById(R.id.chip_role);
        tvStatus = findViewById(R.id.chip_status);

        btnChangePassword = findViewById(R.id.btn_edit_profile);
        btnLogout = findViewById(R.id.btn_logout);
        ivProfilePicture = findViewById(R.id.iv_profile_picture);
    }
    
    private void setupClickListeners() {
        btnChangePassword.setOnClickListener(v -> openChangePasswordActivity());
        btnLogout.setOnClickListener(v -> showLogoutConfirmation());
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
        

        
        // Set default profile picture
        ivProfilePicture.setImageResource(R.drawable.ic_profile_default);
    }
    
    private void showNoDataMessage() {
        tvUserName.setText("Guest User");
        tvEmail.setText("No email available");
        tvRole.setText("Guest");
        tvStatus.setText("Not logged in");

        
        btnChangePassword.setEnabled(false);
    }
    
    private void openChangePasswordActivity() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
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
                // Go back to MainActivity
                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
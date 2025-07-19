package app.quiz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import app.quiz.R;
import app.quiz.ui.activities.LoginActivity;
import app.quiz.ui.activities.FlashcardListActivity;
import app.quiz.ui.activities.MyFlashcardsActivity;
import app.quiz.ui.activities.ProfileActivity;

import app.quiz.utils.SessionManager;

/**
 * MainActivity - Main dashboard for authenticated users
 * Implements logout functionality from UC-02
 */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    
    private SessionManager sessionManager;
    private TextView tvWelcome;
    private CardView cardVocabularyTools;
    private CardView cardMyFlashcards;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        
        initializeComponents();
        checkAuthentication();
        setupUI();
    }
    
    /**
     * Initialize components and dependencies
     */
    private void initializeComponents() {
        sessionManager = SessionManager.getInstance(this);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }
        
        tvWelcome = findViewById(R.id.tv_welcome);
        cardVocabularyTools = findViewById(R.id.card_vocabulary_tools);
        cardMyFlashcards = findViewById(R.id.card_my_flashcards);
        
        // Setup click listeners
        setupClickListeners();
        
        Log.d(TAG, "Components initialized");
    }
    
    /**
     * Setup click listeners for UI components
     */
    private void setupClickListeners() {
        // Vocabulary Tools card click - Navigate to FlashcardListActivity
        cardVocabularyTools.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FlashcardListActivity.class);
            startActivity(intent);
        });
        
        // My Flashcards card click - Navigate to MyFlashcardsActivity
        cardMyFlashcards.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyFlashcardsActivity.class);
            startActivity(intent);
        });
    }
    
    /**
     * Check user authentication (PRE-01: Actor must be logged in)
     */
    private void checkAuthentication() {
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "User not authenticated, redirecting to login");
            redirectToLogin();
            return;
        }
        
        // Check session timeout (BR-03: Session Timeout)
        // Note: isLoggedIn() already handles session timeout internally
        if (!sessionManager.isLoggedIn()) {
            Log.d(TAG, "Session expired, logging out user");
            Toast.makeText(this, "Your session has expired. Please log in again.", Toast.LENGTH_LONG).show();
            redirectToLogin();
            return;
        }
        
        // Update activity timestamp
        sessionManager.updateActivity();
    }
    
    /**
     * Setup UI components
     */
    private void setupUI() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        // Display welcome message
        String userEmail = sessionManager.getUserEmail();
        if (userEmail != null && tvWelcome != null) {
            tvWelcome.setText("Welcome, " + userEmail + "!");
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_profile) {
            openProfile();
            return true;
        } else if (id == R.id.action_logout) {
            showLogoutConfirmation();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Open profile activity
     * Implements navigation to UC-04 (View Profile) and UC-05 (Update Profile)
     */
    private void openProfile() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }
    
    /**
     * Show logout confirmation dialog
     * Implements step 2 from UC-02 normal sequence
     */
    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage(getString(R.string.confirm_logout))
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }
    
    /**
     * Perform logout operation
     * Implements UC-02 normal sequence steps 3-5
     */
    private void performLogout() {
        try {
            Log.d(TAG, "Performing logout");
            
            // Clear session (POST-02: Session is terminated)
            sessionManager.clearSession();
            
            // Show success message
            Toast.makeText(this, getString(R.string.success_logout), Toast.LENGTH_SHORT).show();
            
            // Redirect to login (POST-03: Return to guest mode)
            redirectToLogin();
            
        } catch (Exception e) {
            Log.e(TAG, "Logout failed", e);
            // Handle AS-01: Logout failure
            Toast.makeText(this, "Logout failed. Please check your connection and try again.", 
                    Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * Redirect to login activity
     * Implements POST-01: User is logged out and POST-03: Return to guest mode
     */
    private void redirectToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Update activity timestamp for session management (BR-03: Session Timeout)
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateActivity();
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // Update activity timestamp
        if (sessionManager.isLoggedIn()) {
            sessionManager.updateActivity();
        }
    }
}
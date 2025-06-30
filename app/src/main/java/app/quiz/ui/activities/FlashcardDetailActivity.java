package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import app.quiz.R;
import app.quiz.data.models.FlashcardGroup;
import app.quiz.data.models.Flashcard;
import app.quiz.data.remote.FlashcardService;
import app.quiz.ui.activities.FlashcardListActivity;
import app.quiz.ui.adapters.FlashcardSliderAdapter;
import app.quiz.ui.adapters.FlashcardListAdapter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * FlashcardDetailActivity - Display detailed view of a flashcard group
 * This is a placeholder activity for future implementation
 */
public class FlashcardDetailActivity extends AppCompatActivity {
    private static final String TAG = "FlashcardDetailActivity";
    public static final String EXTRA_FLASHCARD_GROUP = "extra_flashcard_group";
    
    private FlashcardGroup flashcardGroup;
    private TextView tvTitle;
    private TextView tvDescription;


    private MaterialButton btnStartQuiz;
    private MaterialButton btnBackToList;
    
    // New UI components for flashcard content
    private ViewPager2 viewPagerCards;
    private RecyclerView recyclerFlashcards;

    private MaterialButton btnPreviousCard;
    private MaterialButton btnNextCard;
    
    // Adapters
    private FlashcardSliderAdapter sliderAdapter;
    private FlashcardListAdapter listAdapter;
    
    // Data
    private List<Flashcard> flashcards;
    private int currentCardPosition = 0;
    private FlashcardService flashcardService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_detail);
        
        initializeComponents();
        getIntentData();
        setupUI();
    }
    
    /**
     * Initialize UI components
     */
    private void initializeComponents() {
        // Initialize service
        flashcardService = FlashcardService.getInstance();
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }
        
        // Initialize TextViews
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);


        
        // Initialize Buttons
        btnStartQuiz = findViewById(R.id.btn_start_quiz);
        btnBackToList = findViewById(R.id.btn_back_to_list);
        btnPreviousCard = findViewById(R.id.btn_previous);
        btnNextCard = findViewById(R.id.btn_next);
        
        // Initialize new UI components
        viewPagerCards = findViewById(R.id.viewPager);
        recyclerFlashcards = findViewById(R.id.recyclerView);
        
        // Set button click listeners
        setupButtonListeners();
        
        // Initialize adapters and setup components
        initializeViews();
        
        Log.d(TAG, "Components initialized");
    }
    
    /**
     * Get flashcard group data from intent
     */
    private void getIntentData() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(EXTRA_FLASHCARD_GROUP)) {
            flashcardGroup = intent.getParcelableExtra(EXTRA_FLASHCARD_GROUP);
            
            if (flashcardGroup != null) {
                Log.d(TAG, "Received flashcard group: " + flashcardGroup.getName());
                
                // Validate flashcard group data (UC-07 Exception handling)
                if (!isFlashcardGroupValid(flashcardGroup)) {
                    showErrorAndFinish("This flashcard is no longer available.");
                    return;
                }
                
                // Check access permissions (UC-07 Exception handling)
                if (!hasAccessPermission(flashcardGroup)) {
                    showAccessDeniedDialog();
                    return;
                }
            } else {
                Log.e(TAG, "Flashcard group is null");
                showErrorAndFinish("This flashcard is no longer available.");
            }
        } else {
            Log.e(TAG, "No flashcard group data received");
            showErrorAndFinish("Invalid flashcard data.");
        }
    }
    
    private boolean isFlashcardGroupValid(FlashcardGroup group) {
        // Validate that the flashcard group has required data
        return group != null && 
               group.getId() != null && 
               !group.getId().trim().isEmpty() &&
               group.getName() != null && 
               !group.getName().trim().isEmpty();
    }
    
    private boolean hasAccessPermission(FlashcardGroup group) {
        // For now, assume all public flashcards are accessible
        // This can be enhanced with user authentication and premium content checks
        return group.isPublic() || true; // TODO: Add actual permission logic
    }
    
    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }
    
    private void showAccessDeniedDialog() {
         // TODO: Implement access denied dialog with upgrade options
         Toast.makeText(this, "Access denied. Premium content requires subscription.", 
                       Toast.LENGTH_LONG).show();
         finish();
     }
    
    /**
     * Setup UI with flashcard group data
     */
    private void setupUI() {
        if (flashcardGroup == null) {
            Log.e(TAG, "FlashcardGroup is null");
            return;
        }
        
        // Set basic information
        tvTitle.setText(flashcardGroup.getName());
        tvDescription.setText(flashcardGroup.getDescription().isEmpty() ? 
            "No description available" : flashcardGroup.getDescription());
        

        
        // Update toolbar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(flashcardGroup.getName());
        }
        
        Log.d(TAG, "UI setup completed for: " + flashcardGroup.getName());
        
        // Setup flashcard content
        setupFlashcardContent();
    }
    
    /**
     * Initialize ViewPager2 and RecyclerView with adapters
     */
    private void initializeViews() {
        // Initialize with empty list - data will be loaded via API in setupFlashcardContent
        flashcards = new ArrayList<>();
        
        // Setup ViewPager2 adapter
        sliderAdapter = new FlashcardSliderAdapter(flashcards);
        viewPagerCards.setAdapter(sliderAdapter);
        
        // Setup RecyclerView adapter
        listAdapter = new FlashcardListAdapter(flashcards);
        recyclerFlashcards.setLayoutManager(new LinearLayoutManager(this));
        recyclerFlashcards.setAdapter(listAdapter);
        
        // Set click listener for list items to navigate to corresponding card in slider
        listAdapter.setOnFlashcardClickListener(position -> {
            viewPagerCards.setCurrentItem(position, true);

        });
        
        // Setup ViewPager2 page change callback
        viewPagerCards.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                currentCardPosition = position;

                updateNavigationButtons();
            }
        });
    }
    
    /**
     * Setup flashcard content for ViewPager2 and RecyclerView
     */
    private void setupFlashcardContent() {
        if (flashcardGroup == null || flashcardGroup.getId() == null) {
            Log.e(TAG, "Cannot load flashcards: flashcard group is null or has no ID");
            showErrorAndFinish("Invalid flashcard group");
            return;
        }
        
        // Show loading state
        btnStartQuiz.setEnabled(false);
        btnStartQuiz.setText("Loading...");
        
        // Load flashcards from API
        flashcardService.getFlashcardGroupDetails(flashcardGroup.getId(), 
            new FlashcardService.FlashcardCallback<FlashcardGroup>() {
                @Override
                public void onSuccess(FlashcardGroup detailedGroup) {
                    runOnUiThread(() -> {
                        // Update the flashcard group with detailed data
                        flashcardGroup = detailedGroup;
                        flashcards = detailedGroup.getFlashcards();
                        
                        if (flashcards == null || flashcards.isEmpty()) {
                            flashcards = new ArrayList<>();
                            Log.w(TAG, "No flashcards found in group");
                        }
                        
                        // Setup adapters with flashcard data
                         sliderAdapter = new FlashcardSliderAdapter(flashcards);
                         listAdapter = new FlashcardListAdapter(flashcards);
                         listAdapter.setOnFlashcardClickListener(position -> {
                             // Sync ViewPager2 with RecyclerView selection
                             viewPagerCards.setCurrentItem(position, true);
                         });
                        
                        viewPagerCards.setAdapter(sliderAdapter);
                        recyclerFlashcards.setAdapter(listAdapter);
                        
                        // Update card counter and navigation
                
                        updateNavigationButtons();
                        
                        // Re-enable start quiz button
                        btnStartQuiz.setEnabled(true);
                        btnStartQuiz.setText("Start Quiz");
                        
                        Log.d(TAG, "Loaded " + flashcards.size() + " flashcards from API");
                    });
                }
                
                @Override
                public void onError(String error, int statusCode) {
                    runOnUiThread(() -> {
                        Log.e(TAG, "Failed to load flashcard details: " + error);
                        
                        // Fallback to existing flashcards if available
                        if (flashcardGroup.getFlashcards() != null && !flashcardGroup.getFlashcards().isEmpty()) {
                            flashcards = flashcardGroup.getFlashcards();
                            setupAdaptersWithData();
                        } else {
                            // Show error and use empty list
                            flashcards = new ArrayList<>();
                            setupAdaptersWithData();
                            Toast.makeText(FlashcardDetailActivity.this, 
                                "Failed to load flashcards: " + error, Toast.LENGTH_LONG).show();
                        }
                        
                        // Re-enable start quiz button
                        btnStartQuiz.setEnabled(true);
                        btnStartQuiz.setText("Start Quiz");
                    });
                }
            });
    }
    
    /**
      * Setup adapters with current flashcard data
      */
     private void setupAdaptersWithData() {
         sliderAdapter = new FlashcardSliderAdapter(flashcards);
         listAdapter = new FlashcardListAdapter(flashcards);
         listAdapter.setOnFlashcardClickListener(position -> {
             viewPagerCards.setCurrentItem(position, true);
         });
        
        viewPagerCards.setAdapter(sliderAdapter);
        recyclerFlashcards.setAdapter(listAdapter);
        
        updateNavigationButtons();
    }
    

    

    
    /**
     * Update navigation button states
     */
    private void updateNavigationButtons() {
        if (sliderAdapter != null) {
            int total = sliderAdapter.getItemCount();
            btnPreviousCard.setEnabled(currentCardPosition > 0);
            btnNextCard.setEnabled(currentCardPosition < total - 1);
        }
    }
    
    private void setupButtonListeners() {
        btnStartQuiz.setOnClickListener(v -> {
            // TODO: Implement quiz start functionality
            Toast.makeText(this, "Quiz functionality coming soon!", Toast.LENGTH_SHORT).show();
        });
        
        btnBackToList.setOnClickListener(v -> {
            // Navigate back to flashcard list
            Intent intent = new Intent(this, FlashcardListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
        
        // Navigation button listeners
        btnPreviousCard.setOnClickListener(v -> {
            if (currentCardPosition > 0) {
                currentCardPosition--;
                viewPagerCards.setCurrentItem(currentCardPosition, true);
            }
        });
        
        btnNextCard.setOnClickListener(v -> {
            if (sliderAdapter != null && currentCardPosition < sliderAdapter.getItemCount() - 1) {
                currentCardPosition++;
                viewPagerCards.setCurrentItem(currentCardPosition, true);
            }
        });
    }
    

    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
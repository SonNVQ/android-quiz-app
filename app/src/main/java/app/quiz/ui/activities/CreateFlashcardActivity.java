package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Flashcard;
import app.quiz.data.models.FlashcardGroup;
import app.quiz.data.remote.FlashcardService;
import app.quiz.ui.adapters.CreateFlashcardAdapter;
import app.quiz.utils.SessionManager;

/**
 * Activity for creating new flashcard groups
 * Implements UC-12: Create flashcard
 */
public class CreateFlashcardActivity extends AppCompatActivity {
    private static final String TAG = "CreateFlashcardActivity";
    
    // UI Components
    private Toolbar toolbar;
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private SwitchMaterial switchPublic;
    private MaterialButton btnAddFlashcard;
    private LinearLayout llFlashcardsContainer;
    private LinearLayout llEmptyFlashcards;
    private ExtendedFloatingActionButton fabSave;
    private FrameLayout loadingOverlay;
    
    // Data
    private List<FlashcardItemView> flashcardViews;
    private FlashcardService flashcardService;
    private SessionManager sessionManager;
    private boolean isLoading = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_flashcard);
        
        initializeServices();
        initializeViews();
        setupToolbar();
        setupListeners();
        
        // Add initial flashcard
        addFlashcardView();
    }
    
    private void initializeServices() {
        flashcardService = FlashcardService.getInstance();
        sessionManager = SessionManager.getInstance(this);
        flashcardViews = new ArrayList<>();
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        switchPublic = findViewById(R.id.switch_public);
        btnAddFlashcard = findViewById(R.id.btn_add_flashcard);
        llFlashcardsContainer = findViewById(R.id.ll_flashcards_container);
        llEmptyFlashcards = findViewById(R.id.ll_empty_flashcards);
        fabSave = findViewById(R.id.fab_save);
        loadingOverlay = findViewById(R.id.loading_overlay);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }
    
    private void setupListeners() {
        btnAddFlashcard.setOnClickListener(v -> addFlashcardView());
        fabSave.setOnClickListener(v -> saveFlashcard());
        
        // Add text watcher to title for validation
        etTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateForm();
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void addFlashcardView() {
        View flashcardView = LayoutInflater.from(this)
                .inflate(R.layout.item_create_flashcard, llFlashcardsContainer, false);
        
        FlashcardItemView itemView = new FlashcardItemView(flashcardView, flashcardViews.size() + 1);
        flashcardViews.add(itemView);
        llFlashcardsContainer.addView(flashcardView);
        
        updateEmptyState();
        updateCardNumbers();
        validateForm();
    }
    
    private void removeFlashcardView(FlashcardItemView itemView) {
        if (flashcardViews.size() <= 1) {
            Toast.makeText(this, "At least one flashcard is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        flashcardViews.remove(itemView);
        llFlashcardsContainer.removeView(itemView.getView());
        
        updateEmptyState();
        updateCardNumbers();
        validateForm();
    }
    
    private void updateEmptyState() {
        if (flashcardViews.isEmpty()) {
            llEmptyFlashcards.setVisibility(View.VISIBLE);
            llFlashcardsContainer.setVisibility(View.GONE);
        } else {
            llEmptyFlashcards.setVisibility(View.GONE);
            llFlashcardsContainer.setVisibility(View.VISIBLE);
        }
    }
    
    private void updateCardNumbers() {
        for (int i = 0; i < flashcardViews.size(); i++) {
            flashcardViews.get(i).updateCardNumber(i + 1);
        }
    }
    
    private void validateForm() {
        boolean isValid = true;
        
        // Check title
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            isValid = false;
        }
        
        // Check flashcards
        if (flashcardViews.isEmpty()) {
            isValid = false;
        } else {
            for (FlashcardItemView itemView : flashcardViews) {
                if (!itemView.isValid()) {
                    isValid = false;
                    break;
                }
            }
        }
        
        fabSave.setEnabled(isValid && !isLoading);
    }
    
    private void saveFlashcard() {
        if (isLoading) return;
        
        // Validate form
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (flashcardViews.isEmpty()) {
            Toast.makeText(this, "At least one flashcard is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Collect flashcard data
        List<Flashcard> flashcards = new ArrayList<>();
        for (FlashcardItemView itemView : flashcardViews) {
            if (!itemView.isValid()) {
                Toast.makeText(this, "Please fill in all flashcard fields", Toast.LENGTH_SHORT).show();
                return;
            }
            flashcards.add(itemView.getFlashcard());
        }
        
        // Get other form data
        String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
        boolean isPublic = switchPublic.isChecked();
        String authToken = sessionManager.getAuthToken();
        
        if (authToken == null || authToken.isEmpty()) {
            Toast.makeText(this, "Please log in to create flashcards", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Show loading
        setLoading(true);
        
        // Create flashcard
        flashcardService.createFlashcard(authToken, title, description, isPublic, flashcards,
                new FlashcardService.FlashcardCallback<FlashcardGroup>() {
                    @Override
                    public void onSuccess(FlashcardGroup result) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            Toast.makeText(CreateFlashcardActivity.this, 
                                    "Flashcard created successfully!", Toast.LENGTH_SHORT).show();
                            
                            // Return to previous activity with result
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("created_flashcard", result);
                            setResult(RESULT_OK, resultIntent);
                            finish();
                        });
                    }
                    
                    @Override
                    public void onError(String error, int statusCode) {
                        runOnUiThread(() -> {
                            setLoading(false);
                            String errorMessage = getErrorMessage(error, statusCode);
                            Toast.makeText(CreateFlashcardActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
    
    private void setLoading(boolean loading) {
        isLoading = loading;
        loadingOverlay.setVisibility(loading ? View.VISIBLE : View.GONE);
        fabSave.setEnabled(!loading && isFormValid());
    }
    
    private boolean isFormValid() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        if (title.isEmpty()) return false;
        
        if (flashcardViews.isEmpty()) return false;
        
        for (FlashcardItemView itemView : flashcardViews) {
            if (!itemView.isValid()) return false;
        }
        
        return true;
    }
    
    private String getErrorMessage(String error, int statusCode) {
        switch (statusCode) {
            case 400:
                return "Invalid flashcard data. Please check your input.";
            case 401:
                return "Please log in to create flashcards.";
            case 500:
                return "Server error. Please try again later.";
            default:
                return error != null ? error : "An error occurred while creating the flashcard.";
        }
    }
    
    /**
     * Helper class to manage individual flashcard item views
     */
    private class FlashcardItemView {
        private final View view;
        private final TextInputEditText etTerm;
        private final TextInputEditText etDefinition;
        private final MaterialButton btnDelete;
        private final android.widget.TextView tvCardNumber;
        
        public FlashcardItemView(View view, int cardNumber) {
            this.view = view;
            this.etTerm = view.findViewById(R.id.et_term);
            this.etDefinition = view.findViewById(R.id.et_definition);
            this.btnDelete = view.findViewById(R.id.btn_delete_card);
            this.tvCardNumber = view.findViewById(R.id.tv_card_number);
            
            updateCardNumber(cardNumber);
            setupListeners();
        }
        
        private void setupListeners() {
            btnDelete.setOnClickListener(v -> removeFlashcardView(this));
            
            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    validateForm();
                }
                
                @Override
                public void afterTextChanged(Editable s) {}
            };
            
            etTerm.addTextChangedListener(textWatcher);
            etDefinition.addTextChangedListener(textWatcher);
        }
        
        public void updateCardNumber(int number) {
            tvCardNumber.setText("Card " + number);
        }
        
        public boolean isValid() {
            String term = etTerm.getText() != null ? etTerm.getText().toString().trim() : "";
            String definition = etDefinition.getText() != null ? etDefinition.getText().toString().trim() : "";
            return !term.isEmpty() && !definition.isEmpty();
        }
        
        public Flashcard getFlashcard() {
            String term = etTerm.getText() != null ? etTerm.getText().toString().trim() : "";
            String definition = etDefinition.getText() != null ? etDefinition.getText().toString().trim() : "";
            return new Flashcard(term, definition);
        }
        
        public View getView() {
            return view;
        }
    }
}
package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Flashcard;
import app.quiz.data.models.FlashcardGroup;
import app.quiz.ui.adapters.SelectableFlashcardGroupAdapter;
import app.quiz.utils.TestGenerator;

/**
 * FlashcardTestSetupActivity - Setup screen for configuring flashcard tests
 * Allows users to select flashcard groups, test modes, and question count
 */
public class FlashcardTestSetupActivity extends AppCompatActivity {
    
    public static final String EXTRA_FLASHCARD_GROUP_ID = "extra_flashcard_group_id";
    
    // UI Components
    private Toolbar toolbar;
    private RecyclerView rvFlashcardGroups;
    private MaterialCardView cardTestSettings;
    private ChipGroup chipGroupTestMode;
    private Chip chipMixed;
    private Chip chipMultipleChoice;
    private Chip chipTrueFalse;
    private Chip chipFillBlank;
    private TextView tvQuestionCount;
    private SeekBar seekBarQuestionCount;
    private TextView tvQuestionCountLabel;
    private MaterialButton btnStartTest;
    
    // Data
    private SelectableFlashcardGroupAdapter adapter;
    private List<FlashcardGroup> flashcardGroups;
    private FlashcardGroup selectedGroup;
    private String selectedTestMode = FlashcardTestActivity.TEST_MODE_MIXED;
    private int selectedQuestionCount = 10;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_test_setup);
        
        initializeUI();
        setupToolbar();
        loadFlashcardGroups();
        setupTestModeSelection();
        setupQuestionCountSelection();
        setupClickListeners();
        
        // Check if a specific group was requested
        String requestedGroupId = getIntent().getStringExtra(EXTRA_FLASHCARD_GROUP_ID);
        if (requestedGroupId != null) {
            selectFlashcardGroupById(requestedGroupId);
        }
    }
    
    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        rvFlashcardGroups = findViewById(R.id.rv_flashcard_groups);
        cardTestSettings = findViewById(R.id.card_test_settings);
        chipGroupTestMode = findViewById(R.id.chip_group_test_mode);
        chipMixed = findViewById(R.id.chip_mixed);
        chipMultipleChoice = findViewById(R.id.chip_multiple_choice);
        chipTrueFalse = findViewById(R.id.chip_true_false);
        chipFillBlank = findViewById(R.id.chip_fill_blank);
        tvQuestionCount = findViewById(R.id.tv_question_count);
        seekBarQuestionCount = findViewById(R.id.seekbar_question_count);
        tvQuestionCountLabel = findViewById(R.id.tv_question_count_label);
        btnStartTest = findViewById(R.id.btn_start_test);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Setup Flashcard Test");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadFlashcardGroups() {
        // TODO: Load from database or service
        // For now, create sample data
        flashcardGroups = createSampleFlashcardGroups();
        
        adapter = new SelectableFlashcardGroupAdapter(flashcardGroups, this::onFlashcardGroupSelected);
        rvFlashcardGroups.setLayoutManager(new LinearLayoutManager(this));
        rvFlashcardGroups.setAdapter(adapter);
    }
    
    private List<FlashcardGroup> createSampleFlashcardGroups() {
        // This should be replaced with actual data loading
        List<FlashcardGroup> groups = new ArrayList<>();
        
        FlashcardGroup group1 = new FlashcardGroup("1", "Basic Vocabulary", "Common English words", true);
        group1.addFlashcard(new Flashcard("hello", "a greeting"));
        group1.addFlashcard(new Flashcard("goodbye", "a farewell"));
        group1.addFlashcard(new Flashcard("thank you", "expression of gratitude"));
        groups.add(group1);
        
        FlashcardGroup group2 = new FlashcardGroup("2", "Advanced Vocabulary", "Complex English words", true);
        group2.addFlashcard(new Flashcard("serendipity", "pleasant surprise"));
        group2.addFlashcard(new Flashcard("ephemeral", "lasting for a short time"));
        group2.addFlashcard(new Flashcard("ubiquitous", "present everywhere"));
        groups.add(group2);
        
        return groups;
    }
    
    private void setupTestModeSelection() {
        chipMixed.setChecked(true); // Default selection
        
        chipGroupTestMode.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) {
                return; // Prevent deselecting all chips
            }
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_mixed) {
                selectedTestMode = FlashcardTestActivity.TEST_MODE_MIXED;
            } else if (checkedId == R.id.chip_multiple_choice) {
                selectedTestMode = FlashcardTestActivity.TEST_MODE_MULTIPLE_CHOICE;
            } else if (checkedId == R.id.chip_true_false) {
                selectedTestMode = FlashcardTestActivity.TEST_MODE_TRUE_FALSE;
            } else if (checkedId == R.id.chip_fill_blank) {
                selectedTestMode = FlashcardTestActivity.TEST_MODE_FILL_BLANK;
            }
            
            updateQuestionCountLimits();
        });
    }
    
    private void setupQuestionCountSelection() {
        seekBarQuestionCount.setMax(20);
        seekBarQuestionCount.setProgress(selectedQuestionCount);
        
        updateQuestionCountDisplay();
        
        seekBarQuestionCount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Ensure minimum value of 5
                selectedQuestionCount = Math.max(5, progress);
                if (selectedQuestionCount != progress) {
                    seekBar.setProgress(selectedQuestionCount);
                }
                updateQuestionCountDisplay();
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
    
    private void updateQuestionCountDisplay() {
        tvQuestionCount.setText(String.valueOf(selectedQuestionCount));
        
        if (selectedGroup != null) {
            int maxQuestions = getMaxQuestionsForMode(selectedGroup, selectedTestMode);
            if (selectedQuestionCount > maxQuestions) {
                selectedQuestionCount = maxQuestions;
                seekBarQuestionCount.setProgress(selectedQuestionCount);
                tvQuestionCount.setText(String.valueOf(selectedQuestionCount));
            }
            
            tvQuestionCountLabel.setText(String.format("Questions (max %d available)", maxQuestions));
        } else {
            tvQuestionCountLabel.setText("Questions");
        }
    }
    
    private void updateQuestionCountLimits() {
        if (selectedGroup != null) {
            int maxQuestions = getMaxQuestionsForMode(selectedGroup, selectedTestMode);
            seekBarQuestionCount.setMax(Math.max(5, maxQuestions));
            updateQuestionCountDisplay();
        }
    }
    
    private int getMaxQuestionsForMode(FlashcardGroup group, String testMode) {
        int flashcardCount = group.getFlashcards().size();
        
        switch (testMode) {
            case FlashcardTestActivity.TEST_MODE_MULTIPLE_CHOICE:
                // Need at least 4 flashcards for each multiple choice question
                return Math.min(flashcardCount, flashcardCount >= 4 ? flashcardCount : 0);
            case FlashcardTestActivity.TEST_MODE_TRUE_FALSE:
            case FlashcardTestActivity.TEST_MODE_FILL_BLANK:
                return flashcardCount;
            case FlashcardTestActivity.TEST_MODE_MIXED:
            default:
                return TestGenerator.getRecommendedQuestionCount(flashcardCount);
        }
    }
    
    private void setupClickListeners() {
        btnStartTest.setOnClickListener(v -> startTest());
    }
    
    private void onFlashcardGroupSelected(FlashcardGroup group) {
        selectedGroup = group;
        adapter.setSelectedGroup(group);
        
        // Show test settings
        cardTestSettings.setVisibility(View.VISIBLE);
        
        // Update question count limits
        updateQuestionCountLimits();
        
        updateStartButtonState();
    }
    
    private void updateStartButtonState() {
        btnStartTest.setEnabled(selectedGroup != null);
    }
    

    
    private void selectFlashcardGroupById(String groupId) {
        for (FlashcardGroup group : flashcardGroups) {
            if (group.getId().equals(groupId)) {
                onFlashcardGroupSelected(group);
                break;
            }
        }
    }
    
    private void startTest() {
        if (selectedGroup == null) {
            Toast.makeText(this, "Please select a flashcard group", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (selectedGroup.getFlashcards().isEmpty()) {
            Toast.makeText(this, "Selected group has no flashcards", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Validate question count for selected mode
        int maxQuestions = getMaxQuestionsForMode(selectedGroup, selectedTestMode);
        if (maxQuestions == 0) {
            Toast.makeText(this, "Not enough flashcards for selected test mode", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Start the test
        Intent intent = new Intent(this, FlashcardTestActivity.class);
        intent.putExtra(FlashcardTestActivity.EXTRA_FLASHCARD_GROUP, selectedGroup);
        intent.putExtra(FlashcardTestActivity.EXTRA_QUESTION_COUNT, selectedQuestionCount);
        intent.putExtra(FlashcardTestActivity.EXTRA_TEST_MODE, selectedTestMode);
        startActivity(intent);
    }
}
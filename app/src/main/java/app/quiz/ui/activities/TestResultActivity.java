package app.quiz.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.quiz.MainActivity;
import app.quiz.R;
import app.quiz.data.models.Flashcard;
import app.quiz.data.models.TestResult;
import app.quiz.ui.adapters.TestResultAdapter;

/**
 * TestResultActivity - Displays test results and performance feedback
 * Shows score, time taken, incorrect answers, and review options
 */
public class TestResultActivity extends AppCompatActivity {
    
    public static final String EXTRA_TEST_RESULT = "extra_test_result";
    
    // UI Components
    private Toolbar toolbar;
    private TextView tvScore;
    private TextView tvPerformanceLevel;
    private TextView tvCorrectAnswers;
    private TextView tvIncorrectAnswers;
    private TextView tvSkippedQuestions;
    private TextView tvTimeTaken;
    private TextView tvTestDate;
    
    // Cards
    private MaterialCardView cardIncorrectAnswers;
    private MaterialCardView cardSkippedQuestions;
    
    // RecyclerViews
    private RecyclerView rvIncorrectAnswers;
    private RecyclerView rvSkippedQuestions;
    
    // Adapters
    private TestResultAdapter incorrectAnswersAdapter;
    private TestResultAdapter skippedQuestionsAdapter;
    
    // Action buttons
    private MaterialButton btnRetakeTest;
    private MaterialButton btnReviewFlashcards;
    private MaterialButton btnBackToHome;
    
    // Data
    private TestResult testResult;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);
        
        initializeUI();
        setupToolbar();
        loadTestResult();
        displayResults();
        setupClickListeners();
    }
    
    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        tvScore = findViewById(R.id.tv_score);
        tvPerformanceLevel = findViewById(R.id.tv_performance_level);
        tvCorrectAnswers = findViewById(R.id.tv_correct_answers);
        tvIncorrectAnswers = findViewById(R.id.tv_incorrect_answers);
        tvSkippedQuestions = findViewById(R.id.tv_skipped_questions);
        tvTimeTaken = findViewById(R.id.tv_time_taken);
        tvTestDate = findViewById(R.id.tv_test_date);
        
        cardIncorrectAnswers = findViewById(R.id.card_incorrect_answers);
        cardSkippedQuestions = findViewById(R.id.card_skipped_questions);
        
        rvIncorrectAnswers = findViewById(R.id.rv_incorrect_answers);
        rvSkippedQuestions = findViewById(R.id.rv_skipped_questions);
        
        btnRetakeTest = findViewById(R.id.btn_retake_test);
        btnReviewFlashcards = findViewById(R.id.btn_review_flashcards);
        btnBackToHome = findViewById(R.id.btn_back_to_home);
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Test Results");
        }
        
        toolbar.setNavigationOnClickListener(v -> finish());
    }
    
    private void loadTestResult() {
        testResult = getIntent().getParcelableExtra(EXTRA_TEST_RESULT);
        if (testResult == null) {
            finish();
            return;
        }
    }
    
    private void displayResults() {
        // Score and performance
        int scorePercentage = (int) Math.round(testResult.getScorePercentage());
        tvScore.setText(String.format("%d%%", scorePercentage));
        
        String performanceLevel = testResult.getPerformanceLevel();
        tvPerformanceLevel.setText(performanceLevel);
        
        // Set performance level color
        int performanceColor;
        switch (performanceLevel) {
            case "Excellent":
                performanceColor = getResources().getColor(android.R.color.holo_green_dark, null);
                break;
            case "Good":
                performanceColor = getResources().getColor(android.R.color.holo_blue_dark, null);
                break;
            case "Fair":
                performanceColor = getResources().getColor(android.R.color.holo_orange_dark, null);
                break;
            default: // Poor
                performanceColor = getResources().getColor(android.R.color.holo_red_dark, null);
                break;
        }
        tvPerformanceLevel.setTextColor(performanceColor);
        
        // Statistics
        tvCorrectAnswers.setText(String.valueOf(testResult.getCorrectAnswers()));
        tvIncorrectAnswers.setText(String.valueOf(testResult.getIncorrectAnswers()));
        tvSkippedQuestions.setText(String.valueOf(testResult.getSkippedQuestions()));
        
        // Time taken
        long durationMs = testResult.getTestDurationMs();
        String formattedTime = formatDuration(durationMs);
        tvTimeTaken.setText(formattedTime);
        
        // Test date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' HH:mm", Locale.getDefault());
        tvTestDate.setText(dateFormat.format(testResult.getTestDate()));
        
        // Setup incorrect answers section
        setupIncorrectAnswersSection();
        
        // Setup skipped questions section
        setupSkippedQuestionsSection();
    }
    
    private void setupIncorrectAnswersSection() {
        List<TestResult.QuestionResult> incorrectResults = getIncorrectQuestionResults(testResult);
        
        if (incorrectResults.isEmpty()) {
            cardIncorrectAnswers.setVisibility(View.GONE);
        } else {
            cardIncorrectAnswers.setVisibility(View.VISIBLE);
            
            incorrectAnswersAdapter = new TestResultAdapter(incorrectResults, TestResultAdapter.TYPE_INCORRECT);
            rvIncorrectAnswers.setLayoutManager(new LinearLayoutManager(this));
            rvIncorrectAnswers.setAdapter(incorrectAnswersAdapter);
            rvIncorrectAnswers.setNestedScrollingEnabled(false);
        }
    }
    
    private void setupSkippedQuestionsSection() {
        List<TestResult.QuestionResult> skippedResults = getSkippedQuestionResults(testResult);
        
        if (skippedResults.isEmpty()) {
            cardSkippedQuestions.setVisibility(View.GONE);
        } else {
            cardSkippedQuestions.setVisibility(View.VISIBLE);
            
            skippedQuestionsAdapter = new TestResultAdapter(skippedResults, TestResultAdapter.TYPE_SKIPPED);
            rvSkippedQuestions.setLayoutManager(new LinearLayoutManager(this));
            rvSkippedQuestions.setAdapter(skippedQuestionsAdapter);
            rvSkippedQuestions.setNestedScrollingEnabled(false);
        }
    }
    
    private List<TestResult.QuestionResult> getIncorrectQuestionResults(TestResult testResult) {
        List<TestResult.QuestionResult> incorrectResults = new ArrayList<>();
        for (TestResult.QuestionResult result : testResult.getQuestionResults()) {
            if (!result.isCorrect() && !result.wasSkipped()) {
                incorrectResults.add(result);
            }
        }
        return incorrectResults;
    }
    
    private List<TestResult.QuestionResult> getSkippedQuestionResults(TestResult testResult) {
        List<TestResult.QuestionResult> skippedResults = new ArrayList<>();
        for (TestResult.QuestionResult result : testResult.getQuestionResults()) {
            if (result.wasSkipped()) {
                skippedResults.add(result);
            }
        }
        return skippedResults;
    }
    
    private void setupClickListeners() {
        btnRetakeTest.setOnClickListener(v -> retakeTest());
        btnReviewFlashcards.setOnClickListener(v -> reviewFlashcards());
        btnBackToHome.setOnClickListener(v -> backToHome());
    }
    
    private void retakeTest() {
        // Navigate back to test setup or directly start a new test
        Intent intent = new Intent(this, FlashcardTestSetupActivity.class);
        intent.putExtra(FlashcardTestSetupActivity.EXTRA_FLASHCARD_GROUP_ID, testResult.getFlashcardGroupId());
        startActivity(intent);
        finish();
    }
    
    private void reviewFlashcards() {
        // Navigate back to main activity to review flashcards
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("review_mode", "test_review");
        intent.putExtra("flashcard_group_id", testResult.getFlashcardGroupId());
        startActivity(intent);
    }
    
    private void backToHome() {
        // Navigate back to main activity
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
    
    private String formatDuration(long durationMs) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMs) - 
                      TimeUnit.MINUTES.toSeconds(minutes);
        
        if (minutes > 0) {
            return String.format(Locale.getDefault(), "%dm %ds", minutes, seconds);
        } else {
            return String.format(Locale.getDefault(), "%ds", seconds);
        }
    }
    
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        backToHome();
    }
}
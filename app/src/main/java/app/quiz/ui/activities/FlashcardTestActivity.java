package app.quiz.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import app.quiz.R;
import app.quiz.data.models.Flashcard;
import app.quiz.data.models.FlashcardGroup;
import app.quiz.data.models.TestQuestion;
import app.quiz.data.models.TestResult;
import app.quiz.utils.TestGenerator;

/**
 * FlashcardTestActivity - Main activity for taking flashcard tests
 * Supports multiple question types: multiple choice, true/false, fill-in-the-blank
 */
public class FlashcardTestActivity extends AppCompatActivity {
    
    // Constants
    public static final String EXTRA_FLASHCARD_GROUP = "extra_flashcard_group";
    public static final String EXTRA_QUESTION_COUNT = "extra_question_count";
    public static final String EXTRA_TEST_MODE = "extra_test_mode";
    
    // Test modes
    public static final String TEST_MODE_MIXED = "mixed";
    public static final String TEST_MODE_MULTIPLE_CHOICE = "multiple_choice";
    public static final String TEST_MODE_TRUE_FALSE = "true_false";
    public static final String TEST_MODE_FILL_BLANK = "fill_blank";
    
    // UI Components
    private Toolbar toolbar;
    private TextView tvQuestionCounter;
    private TextView tvQuestionText;
    private ProgressBar progressBar;
    
    // Question type specific UI
    private MaterialCardView cardMultipleChoice;
    private RadioGroup radioGroupOptions;
    private MaterialCardView cardTrueFalse;
    private MaterialButton btnTrue;
    private MaterialButton btnFalse;
    private MaterialCardView cardFillBlank;
    private EditText etFillBlank;
    
    // Common UI
    private TextView tvHint;
    private MaterialButton btnShowHint;
    private MaterialButton btnSubmit;
    private MaterialButton btnSkip;
    private MaterialButton btnNext;
    private TextView tvFeedback;
    private LinearLayout layoutFeedback;
    
    // Data
    private FlashcardGroup flashcardGroup;
    private List<TestQuestion> questions;
    private int currentQuestionIndex = 0;
    private TestResult testResult;
    private long testStartTime;
    private long questionStartTime;
    private boolean isAnswered = false;
    private boolean hintShown = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_test);
        
        initializeUI();
        setupToolbar();
        loadTestData();
        generateQuestions();
        startTest();
    }
    
    private void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        tvQuestionCounter = findViewById(R.id.tv_question_counter);
        tvQuestionText = findViewById(R.id.tv_question_text);
        progressBar = findViewById(R.id.progress_bar);
        
        // Question type specific UI
        cardMultipleChoice = findViewById(R.id.card_multiple_choice);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        cardTrueFalse = findViewById(R.id.card_true_false);
        btnTrue = findViewById(R.id.btn_true);
        btnFalse = findViewById(R.id.btn_false);
        cardFillBlank = findViewById(R.id.card_fill_blank);
        etFillBlank = findViewById(R.id.et_fill_blank);
        
        // Common UI
        tvHint = findViewById(R.id.tv_hint);
        btnShowHint = findViewById(R.id.btn_show_hint);
        btnSubmit = findViewById(R.id.btn_submit);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);
        tvFeedback = findViewById(R.id.tv_feedback);
        layoutFeedback = findViewById(R.id.layout_feedback);
        
        setupClickListeners();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Flashcard Test");
        }
        
        toolbar.setNavigationOnClickListener(v -> showExitConfirmation());
    }
    
    private void setupClickListeners() {
        btnShowHint.setOnClickListener(v -> showHint());
        btnSubmit.setOnClickListener(v -> submitAnswer());
        btnSkip.setOnClickListener(v -> skipQuestion());
        btnNext.setOnClickListener(v -> nextQuestion());
        
        btnTrue.setOnClickListener(v -> selectTrueFalse(true));
        btnFalse.setOnClickListener(v -> selectTrueFalse(false));
        
        etFillBlank.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                updateSubmitButton();
            }
        });
        
        radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> updateSubmitButton());
    }
    
    private void loadTestData() {
        flashcardGroup = getIntent().getParcelableExtra(EXTRA_FLASHCARD_GROUP);
        if (flashcardGroup == null || flashcardGroup.getFlashcards().isEmpty()) {
            Toast.makeText(this, "No flashcards available for testing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        // Initialize test result
        testResult = new TestResult(
            UUID.randomUUID().toString(),
            flashcardGroup.getId(),
            flashcardGroup.getName()
        );
    }
    
    private void generateQuestions() {
        int questionCount = getIntent().getIntExtra(EXTRA_QUESTION_COUNT, 
            TestGenerator.getRecommendedQuestionCount(flashcardGroup.getFlashcards().size()));
        String testMode = getIntent().getStringExtra(EXTRA_TEST_MODE);
        
        List<Flashcard> flashcards = flashcardGroup.getFlashcards();
        
        switch (testMode != null ? testMode : TEST_MODE_MIXED) {
            case TEST_MODE_MULTIPLE_CHOICE:
                questions = TestGenerator.generateMultipleChoiceQuestions(flashcards, questionCount);
                break;
            case TEST_MODE_TRUE_FALSE:
                questions = TestGenerator.generateTrueFalseQuestions(flashcards, questionCount);
                break;
            case TEST_MODE_FILL_BLANK:
                questions = TestGenerator.generateFillInBlankQuestions(flashcards, questionCount);
                break;
            default:
                questions = TestGenerator.generateMixedTest(flashcards, questionCount);
                break;
        }
        
        if (questions.isEmpty()) {
            Toast.makeText(this, "Unable to generate test questions", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        testResult.setTotalQuestions(questions.size());
    }
    
    private void startTest() {
        testStartTime = SystemClock.elapsedRealtime();
        currentQuestionIndex = 0;
        loadCurrentQuestion();
    }
    
    private void loadCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            finishTest();
            return;
        }
        
        TestQuestion question = questions.get(currentQuestionIndex);
        questionStartTime = SystemClock.elapsedRealtime();
        isAnswered = false;
        hintShown = false;
        
        // Update UI
        tvQuestionCounter.setText(String.format("%d / %d", currentQuestionIndex + 1, questions.size()));
        progressBar.setProgress((int) ((currentQuestionIndex / (float) questions.size()) * 100));
        tvQuestionText.setText(question.getQuestionText());
        
        // Hide all question type cards
        cardMultipleChoice.setVisibility(View.GONE);
        cardTrueFalse.setVisibility(View.GONE);
        cardFillBlank.setVisibility(View.GONE);
        
        // Show appropriate question type UI
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                setupMultipleChoiceQuestion(question);
                break;
            case TRUE_FALSE:
                setupTrueFalseQuestion(question);
                break;
            case FILL_IN_BLANK:
                setupFillBlankQuestion(question);
                break;
        }
        
        // Reset UI state
        layoutFeedback.setVisibility(View.GONE);
        btnShowHint.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        btnSkip.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        
        updateSubmitButton();
    }
    
    private void setupMultipleChoiceQuestion(TestQuestion question) {
        cardMultipleChoice.setVisibility(View.VISIBLE);
        radioGroupOptions.removeAllViews();
        radioGroupOptions.clearCheck();
        
        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setId(View.generateViewId());
            radioButton.setText(options.get(i));
            radioButton.setTag(i);
            radioGroupOptions.addView(radioButton);
        }
    }
    
    private void setupTrueFalseQuestion(TestQuestion question) {
        cardTrueFalse.setVisibility(View.VISIBLE);
        btnTrue.setSelected(false);
        btnFalse.setSelected(false);
    }
    
    private void setupFillBlankQuestion(TestQuestion question) {
        cardFillBlank.setVisibility(View.VISIBLE);
        etFillBlank.setText("");
        etFillBlank.requestFocus();
    }
    
    private void selectTrueFalse(boolean isTrue) {
        btnTrue.setSelected(isTrue);
        btnFalse.setSelected(!isTrue);
        updateSubmitButton();
    }
    
    private void updateSubmitButton() {
        boolean hasAnswer = false;
        TestQuestion question = questions.get(currentQuestionIndex);
        
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                hasAnswer = radioGroupOptions.getCheckedRadioButtonId() != -1;
                break;
            case TRUE_FALSE:
                hasAnswer = btnTrue.isSelected() || btnFalse.isSelected();
                break;
            case FILL_IN_BLANK:
                hasAnswer = !etFillBlank.getText().toString().trim().isEmpty();
                break;
        }
        
        btnSubmit.setEnabled(hasAnswer && !isAnswered);
    }
    
    private void showHint() {
        TestQuestion question = questions.get(currentQuestionIndex);
        tvHint.setText(question.getHint());
        tvHint.setVisibility(View.VISIBLE);
        btnShowHint.setVisibility(View.GONE);
        hintShown = true;
    }
    
    private void submitAnswer() {
        if (isAnswered) return;
        
        TestQuestion question = questions.get(currentQuestionIndex);
        String userAnswer = getUserAnswer(question);
        boolean isCorrect = checkAnswer(question, userAnswer);
        long timeSpent = SystemClock.elapsedRealtime() - questionStartTime;
        
        // Record result
        TestResult.QuestionResult questionResult = new TestResult.QuestionResult(
            question.getId(),
            question.getType(),
            userAnswer,
            getCorrectAnswerText(question),
            isCorrect,
            false, // not skipped
            timeSpent,
            question.getSourceFlashcard()
        );
        
        testResult.addQuestionResult(questionResult);
        
        // Show feedback
        showFeedback(isCorrect, question);
        isAnswered = true;
        
        btnSubmit.setVisibility(View.GONE);
        btnSkip.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }
    
    private String getUserAnswer(TestQuestion question) {
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                int checkedId = radioGroupOptions.getCheckedRadioButtonId();
                if (checkedId != -1) {
                    RadioButton selected = findViewById(checkedId);
                    return String.valueOf((Integer) selected.getTag());
                }
                break;
            case TRUE_FALSE:
                if (btnTrue.isSelected()) return "0"; // True
                if (btnFalse.isSelected()) return "1"; // False
                break;
            case FILL_IN_BLANK:
                return etFillBlank.getText().toString().trim();
        }
        return "";
    }
    
    private boolean checkAnswer(TestQuestion question, String userAnswer) {
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
            case TRUE_FALSE:
                try {
                    int userIndex = Integer.parseInt(userAnswer);
                    return question.isCorrectAnswer(userIndex);
                } catch (NumberFormatException e) {
                    return false;
                }
            case FILL_IN_BLANK:
                return question.isCorrectAnswer(userAnswer);
        }
        return false;
    }
    
    private String getCorrectAnswerText(TestQuestion question) {
        switch (question.getType()) {
            case MULTIPLE_CHOICE:
                List<String> options = question.getOptions();
                if (question.getCorrectAnswerIndex() < options.size()) {
                    return options.get(question.getCorrectAnswerIndex());
                }
                break;
            case TRUE_FALSE:
                return question.getCorrectAnswerIndex() == 0 ? "True" : "False";
            case FILL_IN_BLANK:
                return question.getCorrectAnswer();
        }
        return "";
    }
    
    private void showFeedback(boolean isCorrect, TestQuestion question) {
        layoutFeedback.setVisibility(View.VISIBLE);
        
        if (isCorrect) {
            tvFeedback.setText("Correct! ✓");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
        } else {
            String correctAnswer = getCorrectAnswerText(question);
            tvFeedback.setText("Incorrect. The correct answer is: " + correctAnswer);
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
        }
    }
    
    private void skipQuestion() {
        TestQuestion question = questions.get(currentQuestionIndex);
        long timeSpent = SystemClock.elapsedRealtime() - questionStartTime;
        
        // Record skipped result
        TestResult.QuestionResult questionResult = new TestResult.QuestionResult(
            question.getId(),
            question.getType(),
            "", // no answer
            getCorrectAnswerText(question),
            false, // not correct
            true, // skipped
            timeSpent,
            question.getSourceFlashcard()
        );
        
        testResult.addQuestionResult(questionResult);
        
        nextQuestion();
    }
    
    private void nextQuestion() {
        currentQuestionIndex++;
        loadCurrentQuestion();
    }
    
    private void finishTest() {
        long testDuration = SystemClock.elapsedRealtime() - testStartTime;
        testResult.setTestDurationMs(testDuration);
        
        // Navigate to test results
        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putExtra(TestResultActivity.EXTRA_TEST_RESULT, testResult);
        startActivity(intent);
        finish();
    }
    
    private void showExitConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Exit Test")
            .setMessage("Are you sure you want to exit the test? Your progress will be lost.")
            .setPositiveButton("Exit", (dialog, which) -> finish())
            .setNegativeButton("Continue", null)
            .show();
    }
    
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        showExitConfirmation();
        // Note: super.onBackPressed() is intentionally not called here
        // because we want to handle the back press with our custom confirmation dialog
    }
}
package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.ReadingQuestion;
import app.quiz.data.models.TestResult;
import app.quiz.ui.activities.TestResultActivity;

public class ReadingTestActivity extends AppCompatActivity {
    public static final String EXTRA_READING_ID = "reading_id";
    public static final String EXTRA_READING_TITLE = "reading_title";
    public static final String EXTRA_QUESTIONS = "questions";
    
    // UI Components
    private TextView tvQuestionCounter;
    private TextView tvQuestion;
    private LinearLayout layoutOptions;
    private EditText etFillInAnswer;
    private Button btnSubmit;
    private Button btnNext;
    private ProgressBar progressBar;
    
    // Data
    private List<ReadingQuestion> questions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private String readingTitle;
    private String readingId;
    private List<String> userAnswers;
    private boolean hasAnswered = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_test);
        
        initializeViews();
        setupToolbar();
        getIntentData();
        initializeTest();
        loadCurrentQuestion();
    }
    
    private void initializeViews() {
        tvQuestionCounter = findViewById(R.id.tv_question_counter);
        tvQuestion = findViewById(R.id.tv_question);
        layoutOptions = findViewById(R.id.layout_options);
        etFillInAnswer = findViewById(R.id.et_fill_in_answer);
        btnSubmit = findViewById(R.id.btn_submit);
        btnNext = findViewById(R.id.btn_next);
        progressBar = findViewById(R.id.progress_bar);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.reading_test));
        }
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        readingId = intent.getStringExtra(EXTRA_READING_ID);
        readingTitle = intent.getStringExtra(EXTRA_READING_TITLE);
        questions = intent.getParcelableArrayListExtra(EXTRA_QUESTIONS);
        
        if (questions == null || questions.isEmpty()) {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    
    private void initializeTest() {
        userAnswers = new ArrayList<>();
        for (int i = 0; i < questions.size(); i++) {
            userAnswers.add("");
        }
        
        btnSubmit.setOnClickListener(v -> submitAnswer());
        btnNext.setOnClickListener(v -> nextQuestion());
        
        progressBar.setMax(questions.size());
    }
    
    private void loadCurrentQuestion() {
        if (currentQuestionIndex >= questions.size()) {
            showTestResults();
            return;
        }
        
        ReadingQuestion question = questions.get(currentQuestionIndex);
        
        // Update UI
        tvQuestionCounter.setText(getString(R.string.question_counter, 
                currentQuestionIndex + 1, questions.size()));
        tvQuestion.setText(question.getQuestionText());
        progressBar.setProgress(currentQuestionIndex + 1);
        
        // Reset state
        hasAnswered = false;
        btnSubmit.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
        
        // Setup question type specific UI
        if (question.isSingleChoice()) {
            setupMultipleChoiceQuestion(question);
        } else {
            setupFillInBlankQuestion(question);
        }
    }
    
    private void setupMultipleChoiceQuestion(ReadingQuestion question) {
        layoutOptions.setVisibility(View.VISIBLE);
        etFillInAnswer.setVisibility(View.GONE);
        layoutOptions.removeAllViews();
        
        String[] options = {question.getOptionA(), question.getOptionB(), 
                           question.getOptionC(), question.getOptionD()};
        String[] optionLabels = {"A", "B", "C", "D"};
        
        for (int i = 0; i < options.length; i++) {
            if (options[i] != null && !options[i].trim().isEmpty()) {
                Button optionButton = new Button(this);
                optionButton.setText(optionLabels[i] + ". " + options[i]);
                optionButton.setTag(optionLabels[i]);
                
                // Style the button
                optionButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.background_secondary));
                optionButton.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
                
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(0, 8, 0, 8);
                optionButton.setLayoutParams(params);
                
                optionButton.setOnClickListener(v -> selectOption((String) v.getTag()));
                layoutOptions.addView(optionButton);
            }
        }
    }
    
    private void setupFillInBlankQuestion(ReadingQuestion question) {
        layoutOptions.setVisibility(View.GONE);
        etFillInAnswer.setVisibility(View.VISIBLE);
        etFillInAnswer.setText("");
        etFillInAnswer.setHint(getString(R.string.fill_in_answer_hint));
    }
    
    private void selectOption(String option) {
        // Reset all button colors
        for (int i = 0; i < layoutOptions.getChildCount(); i++) {
            Button btn = (Button) layoutOptions.getChildAt(i);
            btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.background_secondary));
        }
        
        // Highlight selected option
        for (int i = 0; i < layoutOptions.getChildCount(); i++) {
            Button btn = (Button) layoutOptions.getChildAt(i);
            if (option.equals(btn.getTag())) {
                btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.primary));
                break;
            }
        }
        
        userAnswers.set(currentQuestionIndex, option);
    }
    
    private void submitAnswer() {
        ReadingQuestion question = questions.get(currentQuestionIndex);
        String userAnswer;
        
        if (question.isSingleChoice()) {
            userAnswer = userAnswers.get(currentQuestionIndex);
            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            userAnswer = etFillInAnswer.getText().toString().trim();
            if (userAnswer.isEmpty()) {
                Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            userAnswers.set(currentQuestionIndex, userAnswer);
        }
        
        hasAnswered = true;
        boolean isCorrect = checkAnswer(question, userAnswer);
        
        if (isCorrect) {
            correctAnswers++;
            showFeedback(getString(R.string.correct_answer), true);
        } else {
            String correctAnswer;
            if (question.isSingleChoice()) {
                correctAnswer = question.getCorrectOption();
                // Handle database format "optionA" -> "A" for display
                if (correctAnswer != null && correctAnswer.toLowerCase().startsWith("option")) {
                    correctAnswer = correctAnswer.substring(6).toUpperCase();
                }
            } else {
                correctAnswer = question.getAnswer();
            }
            showFeedback(getString(R.string.incorrect_answer, correctAnswer), false);
        }
        
        btnSubmit.setVisibility(View.GONE);
        
        if (currentQuestionIndex < questions.size() - 1) {
            btnNext.setText(getString(R.string.next_question));
            btnNext.setVisibility(View.VISIBLE);
        } else {
            btnNext.setText(getString(R.string.finish_test));
            btnNext.setVisibility(View.VISIBLE);
        }
    }
    
    private boolean checkAnswer(ReadingQuestion question, String userAnswer) {
        if (question.isSingleChoice()) {
            String correctOption = question.getCorrectOption();
            // Handle database format "optionA" -> "A"
            if (correctOption != null && correctOption.toLowerCase().startsWith("option")) {
                correctOption = correctOption.substring(6).toUpperCase(); // Remove "option" and convert to uppercase
            }
            return userAnswer.equalsIgnoreCase(correctOption);
        } else {
            // For fill-in-the-blank, do case-insensitive comparison
            return userAnswer.equalsIgnoreCase(question.getAnswer().trim());
        }
    }
    
    private void showFeedback(String message, boolean isCorrect) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        
        if (questions.get(currentQuestionIndex).isSingleChoice()) {
            // Get the correct option and convert from database format if needed
            String correctOption = questions.get(currentQuestionIndex).getCorrectOption();
            if (correctOption != null && correctOption.toLowerCase().startsWith("option")) {
                correctOption = correctOption.substring(6).toUpperCase();
            }
            
            // Highlight correct answer in green and wrong answer in red
            for (int i = 0; i < layoutOptions.getChildCount(); i++) {
                Button btn = (Button) layoutOptions.getChildAt(i);
                String option = (String) btn.getTag();
                
                if (option.equals(correctOption)) {
                    btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.success));
                } else if (option.equals(userAnswers.get(currentQuestionIndex)) && !isCorrect) {
                    btn.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.error));
                }
                
                btn.setEnabled(false);
            }
        }
    }
    
    private void nextQuestion() {
        currentQuestionIndex++;
        loadCurrentQuestion();
    }
    
    private void showTestResults() {
        // Create a TestResult object for the reading test
        TestResult testResult = new TestResult();
        testResult.setFlashcardGroupName(readingTitle);
        testResult.setTotalQuestions(questions.size());
        testResult.setCorrectAnswers(correctAnswers);
        testResult.setSkippedQuestions(0); // Reading tests don't have skipped questions
        testResult.setTestDurationMs(0); // Could be implemented if needed
        
        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putExtra(TestResultActivity.EXTRA_TEST_RESULT, testResult);
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        // Show confirmation dialog if test is in progress
        if (currentQuestionIndex > 0 || hasAnswered) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Exit Test")
                    .setMessage("Are you sure you want to exit? Your progress will be lost.")
                    .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
                    .setNegativeButton("Continue", null)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}
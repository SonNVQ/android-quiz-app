package app.quiz.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import app.quiz.R;
import app.quiz.data.models.Flashcard;

public class MultipleChoiceQuizActivity extends AppCompatActivity {

    // UI Components
    private Toolbar toolbar;
    private ProgressBar progressBar;
    private TextView tvQuestionCounter;
    private TextView tvQuestionText;
    private TextView tvDefinitionTerm;
    private RadioGroup radioGroupOptions;
    private RadioButton radioOptionA, radioOptionB, radioOptionC, radioOptionD;
    private MaterialCardView cardOptionA, cardOptionB, cardOptionC, cardOptionD;
    private TextView tvFeedback;
    private MaterialButton btnSkip, btnSubmit, btnNext;

    // Data
    private List<Flashcard> flashcards;
    private List<Flashcard> skippedQuestions;
private List<Flashcard> retryQuestions;
private Set<Flashcard> retried;
    private int currentQuestionIndex = 0;
    private Flashcard currentFlashcard;
    private int correctAnswerIndex;
    private boolean hasAnswered = false;
    private boolean isCorrect = false;

    // Statistics
    private int correctAnswers = 0;
    private int totalAttempts = 0;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_choice_quiz);

        // Initialize UI components
        initializeViews();

        // Setup toolbar
        setupToolbar();

        // Get flashcard data from intent
        getFlashcardData();

        // Initialize quiz data
        initializeQuizData();

        // Setup button listeners
        setupButtonListeners();

        // Load first question
        loadNextQuestion();
    }

    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progress_bar);
        tvQuestionCounter = findViewById(R.id.tv_question_counter);
        tvQuestionText = findViewById(R.id.tv_question_text);
        tvDefinitionTerm = findViewById(R.id.tv_definition_term);
        radioGroupOptions = findViewById(R.id.radio_group_options);
        radioOptionA = findViewById(R.id.radio_option_a);
        radioOptionB = findViewById(R.id.radio_option_b);
        radioOptionC = findViewById(R.id.radio_option_c);
        radioOptionD = findViewById(R.id.radio_option_d);
        cardOptionA = findViewById(R.id.card_option_a);
        cardOptionB = findViewById(R.id.card_option_b);
        cardOptionC = findViewById(R.id.card_option_c);
        cardOptionD = findViewById(R.id.card_option_d);
        tvFeedback = findViewById(R.id.tv_feedback);
        btnSkip = findViewById(R.id.btn_skip);
        btnSubmit = findViewById(R.id.btn_submit);
        btnNext = findViewById(R.id.btn_next);
        
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Multiple Choice Quiz");
        }
    }

    private void getFlashcardData() {
        flashcards = getIntent().getParcelableArrayListExtra("flashcards");
        if (flashcards == null || flashcards.isEmpty()) {
            Toast.makeText(this, "No flashcards available for quiz", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Shuffle flashcards for random order
        Collections.shuffle(flashcards);
    }

    private void initializeQuizData() {
        skippedQuestions = new ArrayList<>();
retryQuestions = new ArrayList<>();
retried = new HashSet<>();
        startTime = System.currentTimeMillis();
        progressBar.setMax(flashcards.size());
    }

    private void setupButtonListeners() {
        // Card click listeners for better UX
        cardOptionA.setOnClickListener(v -> {
            if (!hasAnswered) {
                radioOptionA.setChecked(true);
            }
        });
    
        cardOptionB.setOnClickListener(v -> {
            if (!hasAnswered) {
                radioOptionB.setChecked(true);
            }
        });
    
        cardOptionC.setOnClickListener(v -> {
            if (!hasAnswered) {
                radioOptionC.setChecked(true);
            }
        });
    
        cardOptionD.setOnClickListener(v -> {
            if (!hasAnswered) {
                radioOptionD.setChecked(true);
            }
        });
    
        // Individual radio listeners to enforce single selection
        radioOptionA.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                radioOptionB.setChecked(false);
                radioOptionC.setChecked(false);
                radioOptionD.setChecked(false);
                button.setChecked(true);
                if (!hasAnswered) btnSubmit.setEnabled(true);
            }
        });
    
        radioOptionB.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                radioOptionA.setChecked(false);
                radioOptionC.setChecked(false);
                radioOptionD.setChecked(false);
                button.setChecked(true);
                if (!hasAnswered) btnSubmit.setEnabled(true);
            }
        });
    
        radioOptionC.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                radioOptionA.setChecked(false);
                radioOptionB.setChecked(false);
                radioOptionD.setChecked(false);
                button.setChecked(true);
                if (!hasAnswered) btnSubmit.setEnabled(true);
            }
        });
    
        radioOptionD.setOnCheckedChangeListener((button, isChecked) -> {
            if (isChecked) {
                radioOptionA.setChecked(false);
                radioOptionB.setChecked(false);
                radioOptionC.setChecked(false);
                button.setChecked(true);
                if (!hasAnswered) btnSubmit.setEnabled(true);
            }
        });
    
        // Submit button
        btnSubmit.setOnClickListener(v -> {
            if (!hasAnswered) {
                checkAnswer();
            }
        });
    
        // Skip button
        btnSkip.setOnClickListener(v -> {
            if (!hasAnswered) {
                skipQuestion();
            }
        });

        // Next button
        btnNext.setOnClickListener(v -> {
            proceedToNext();
        });
        
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= flashcards.size()) {
    if (!skippedQuestions.isEmpty()) {
        flashcards = new ArrayList<>(skippedQuestions);
        skippedQuestions.clear();
        Collections.shuffle(flashcards);
        currentQuestionIndex = 0;
    } else if (!retryQuestions.isEmpty()) {
        flashcards = new ArrayList<>(retryQuestions);
        retryQuestions.clear();
        Collections.shuffle(flashcards);
        currentQuestionIndex = 0;
    } else {
        showQuizResults();
        return;
    }
}

        currentFlashcard = flashcards.get(currentQuestionIndex);
        hasAnswered = false;
        isCorrect = false;

        // Update UI
        updateQuestionCounter();
        updateProgressBar();
        setupQuestion();
        resetAnswerOptions();
        hideFeedback();
        
        btnSubmit.setEnabled(false);
        btnSubmit.setText("Submit");
        btnSubmit.setVisibility(MaterialButton.VISIBLE);
        btnSkip.setText("Skip");
        btnSkip.setVisibility(MaterialButton.VISIBLE);
        btnNext.setVisibility(MaterialButton.GONE);
        
    }

    private void updateQuestionCounter() {
        int totalQuestions = flashcards.size();
        tvQuestionCounter.setText(String.format("Question %d of %d", 
            currentQuestionIndex + 1, totalQuestions));
    }

    private void updateProgressBar() {
        progressBar.setProgress(currentQuestionIndex + 1);
    }

    private void setupQuestion() {
        // Display the definition and ask for the term
        tvQuestionText.setText("What term matches this definition?");
        tvDefinitionTerm.setText(currentFlashcard.getDefinition());

        // Generate answer options
        generateAnswerOptions();
    }

    private void generateAnswerOptions() {
        List<String> options = new ArrayList<>();
        
        // Add correct answer
        options.add(currentFlashcard.getTerm());
        
        // Generate 3 distractors from other flashcards
        List<Flashcard> otherFlashcards = new ArrayList<>(flashcards);
        otherFlashcards.remove(currentFlashcard);
        Collections.shuffle(otherFlashcards);
        
        int distractorsAdded = 0;
        for (Flashcard flashcard : otherFlashcards) {
            if (distractorsAdded >= 3) break;
            if (!flashcard.getTerm().equals(currentFlashcard.getTerm())) {
                options.add(flashcard.getTerm());
                distractorsAdded++;
            }
        }
        
        // If we don't have enough distractors, generate some generic ones
        while (options.size() < 4) {
            options.add("Option " + (char)('A' + options.size() - 1));
        }
        
        // Shuffle options and remember correct answer position
        Collections.shuffle(options);
        correctAnswerIndex = options.indexOf(currentFlashcard.getTerm());
        
        // Set options to radio buttons
        radioOptionA.setText(options.get(0));
        radioOptionB.setText(options.get(1));
        radioOptionC.setText(options.get(2));
        radioOptionD.setText(options.get(3));
    }

    private void resetAnswerOptions() {
        radioOptionA.setChecked(false);
        radioOptionB.setChecked(false);
        radioOptionC.setChecked(false);
        radioOptionD.setChecked(false);
        
        // Reset card colors
        int defaultColor = ContextCompat.getColor(this, R.color.surface_variant);
        cardOptionA.setCardBackgroundColor(defaultColor);
        cardOptionB.setCardBackgroundColor(defaultColor);
        cardOptionC.setCardBackgroundColor(defaultColor);
        cardOptionD.setCardBackgroundColor(defaultColor);
        
        // Enable all options
        radioOptionA.setEnabled(true);
        radioOptionB.setEnabled(true);
        radioOptionC.setEnabled(true);
        radioOptionD.setEnabled(true);
    }

    private void checkAnswer() {
        int selectedIndex = -1;
        if (radioOptionA.isChecked()) selectedIndex = 0;
        else if (radioOptionB.isChecked()) selectedIndex = 1;
        else if (radioOptionC.isChecked()) selectedIndex = 2;
        else if (radioOptionD.isChecked()) selectedIndex = 3;
        
        if (selectedIndex == -1) {
            Toast.makeText(this, "Please select an answer before continuing.", Toast.LENGTH_SHORT).show();
            return;
        }

        hasAnswered = true;
        totalAttempts++;
        
        isCorrect = (selectedIndex == correctAnswerIndex);
        
        if (isCorrect) {
            correctAnswers++;
        } else if (!retried.contains(currentFlashcard)) {
            retryQuestions.add(currentFlashcard);
            retried.add(currentFlashcard);
        }
        
        // Show visual feedback
        highlightAnswers(selectedIndex);
        showFeedback(isCorrect);
        
        // Disable all options
        radioOptionA.setEnabled(false);
        radioOptionB.setEnabled(false);
        radioOptionC.setEnabled(false);
        radioOptionD.setEnabled(false);
        
        // Update button states - hide Submit and Skip, show Next
        btnSubmit.setVisibility(MaterialButton.GONE);
        btnSkip.setVisibility(MaterialButton.GONE);
        btnNext.setVisibility(MaterialButton.VISIBLE);
        
    }

    private void highlightAnswers(int selectedIndex) {
        MaterialCardView[] cards = {cardOptionA, cardOptionB, cardOptionC, cardOptionD};
        
        // Highlight correct answer in green
        cards[correctAnswerIndex].setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.success));
        
        // If wrong answer selected, highlight in red
        if (selectedIndex != correctAnswerIndex) {
            cards[selectedIndex].setCardBackgroundColor(
                ContextCompat.getColor(this, R.color.error));
        }
    }

    private void showFeedback(boolean correct) {
        tvFeedback.setVisibility(TextView.VISIBLE);
        
        if (correct) {
            tvFeedback.setText("✓ Correct! Well done!");
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.success));
        } else {
            tvFeedback.setText("✗ Incorrect. The correct answer is: " + currentFlashcard.getTerm());
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.error));
        }
    }

    private void hideFeedback() {
        tvFeedback.setVisibility(TextView.GONE);
    }

    private void skipQuestion() {
        // Add to skipped questions for retry
        skippedQuestions.add(currentFlashcard);
        
        // Show correct answer
        highlightCorrectAnswer();
        tvFeedback.setVisibility(TextView.VISIBLE);
        tvFeedback.setText("Skipped. The correct answer is: " + currentFlashcard.getTerm());
        tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.warning));
        
        hasAnswered = true;
        totalAttempts++;
        
        // Update button states - hide Submit and Skip, show Next
        btnSubmit.setVisibility(MaterialButton.GONE);
        btnSkip.setVisibility(MaterialButton.GONE);
        btnNext.setVisibility(MaterialButton.VISIBLE);
        
    }

    private void highlightCorrectAnswer() {
        MaterialCardView[] cards = {cardOptionA, cardOptionB, cardOptionC, cardOptionD};
        cards[correctAnswerIndex].setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.success));
    }

    private void proceedToNext() {
        currentQuestionIndex++;
        loadNextQuestion();
    }

    private void showQuizResults() {
        // Return to the previous screen (flashcard detail) after quiz completion
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
        // Show confirmation dialog before leaving
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Exit Quiz")
            .setMessage("Are you sure you want to exit the quiz? Your progress will be lost.")
            .setPositiveButton("Exit", (dialog, which) -> super.onBackPressed())
            .setNegativeButton("Continue", null)
            .show();
    }
}
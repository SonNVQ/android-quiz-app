package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import app.quiz.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import app.quiz.data.models.Flashcard;
import app.quiz.ui.activities.TestResultActivity;

public class FillInBlankQuizActivity extends AppCompatActivity {

    // Constants for Intent extras
    public static final String EXTRA_FLASHCARD_GROUP = "extra_flashcard_group";
    public static final String EXTRA_FLASHCARDS = "extra_flashcards";

    // UI Components
    private TextView tvSentence;
    private EditText etUserAnswer;
    private Button btnSubmit;
    private Button btnRetry;
    private Button btnSkip;
    private TextView tvFeedback;
    private TextView tvHint;
    private TextView tvQuestionCounter;

    // Data
    private ArrayList<Flashcard> flashcards;
    private List<Flashcard> skippedQuestions;
    private int currentQuestionIndex = 0;
    private int correctAnswers = 0;
    private int totalAttempts = 0;
    private boolean isRetry = false;
    private long startTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_blank_quiz);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Fill in the Blank");
            }
        }

        // Initialize UI
        tvSentence = findViewById(R.id.tv_sentence);
        etUserAnswer = findViewById(R.id.et_user_answer);
        btnSubmit = findViewById(R.id.btn_submit);
        btnRetry = findViewById(R.id.btn_retry);
        btnSkip = findViewById(R.id.btn_skip);
        tvFeedback = findViewById(R.id.tv_feedback);
        tvHint = findViewById(R.id.tv_hint);
        tvQuestionCounter = findViewById(R.id.tv_question_counter);

        // Initialize data
        skippedQuestions = new ArrayList<>();
        startTime = System.currentTimeMillis();

        // Get flashcards from intent
        flashcards = getIntent().getParcelableArrayListExtra(EXTRA_FLASHCARDS);
        
        if (flashcards == null || flashcards.isEmpty()) {
            Toast.makeText(this, "No flashcards available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        
        Collections.shuffle(flashcards);

        // Set up button listeners
        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnRetry.setOnClickListener(v -> retryQuestion());
        btnSkip.setOnClickListener(v -> skipQuestion());

        // Set up text watcher for submit button
        etUserAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                btnSubmit.setEnabled(!s.toString().trim().isEmpty());
            }
        });

        loadNextQuestion();
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= flashcards.size()) {
            // Check if there are skipped questions to retry
            if (!skippedQuestions.isEmpty()) {
                flashcards.addAll(skippedQuestions);
                skippedQuestions.clear();
                Collections.shuffle(flashcards.subList(currentQuestionIndex, flashcards.size()));
            }
            
            if (currentQuestionIndex >= flashcards.size()) {
                // End quiz and show results
                showQuizResults();
                return;
            }
        }
        
        Flashcard card = flashcards.get(currentQuestionIndex);
        String sentence = generateSentenceWithBlank(card);
        
        tvSentence.setText(sentence);
        
        // Set hint with first few characters of the term
        String hint = "Hint: " + card.getTerm().substring(0, Math.min(2, card.getTerm().length())) + "...";
        tvHint.setText(hint);
        
        tvQuestionCounter.setText("Question " + (currentQuestionIndex + 1) + "/" + flashcards.size());
        
        // Reset UI state
        etUserAnswer.setText("");
        etUserAnswer.setEnabled(true);
        btnSubmit.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        btnRetry.setVisibility(View.GONE);
        btnSkip.setVisibility(View.VISIBLE);
        tvFeedback.setVisibility(View.GONE);
        isRetry = false;
        
        // Focus on input field
        etUserAnswer.requestFocus();
    }

    private void checkAnswer() {
        String userAnswer = etUserAnswer.getText().toString().trim();
        if (userAnswer.isEmpty()) {
            tvFeedback.setText("Please enter an answer before continuing.");
            tvFeedback.setVisibility(View.VISIBLE);
            return;
        }
        
        totalAttempts++;
        Flashcard card = flashcards.get(currentQuestionIndex);
        
        if (isAnswerCorrect(userAnswer, card.getTerm())) {
            correctAnswers++;
            tvFeedback.setText("Correct! ✓");
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            tvFeedback.setVisibility(View.VISIBLE);
            
            // Delay before moving to next question
            etUserAnswer.postDelayed(() -> {
                currentQuestionIndex++;
                loadNextQuestion();
            }, 1500);
        } else {
            tvFeedback.setText("Incorrect. The correct answer is: " + card.getTerm());
            tvFeedback.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            tvFeedback.setVisibility(View.VISIBLE);
            
            if (!isRetry) {
                btnRetry.setVisibility(View.VISIBLE);
                btnSubmit.setVisibility(View.GONE);
                isRetry = true;
            } else {
                // Second attempt failed, move to next question
                etUserAnswer.postDelayed(() -> {
                    currentQuestionIndex++;
                    loadNextQuestion();
                }, 2000);
            }
        }
    }

    private void retryQuestion() {
        etUserAnswer.setText("");
        tvFeedback.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.VISIBLE);
    }

    private void skipQuestion() {
        // Add current flashcard to skipped questions for later retry
        if (currentQuestionIndex < flashcards.size()) {
            skippedQuestions.add(flashcards.get(currentQuestionIndex));
        }
        currentQuestionIndex++;
        loadNextQuestion();
    }
    
    private String generateSentenceWithBlank(Flashcard card) {
        String definition = card.getDefinition();
        String term = card.getTerm();
        
        // Try to replace the term in the definition with a blank
        if (definition.toLowerCase().contains(term.toLowerCase())) {
            return definition.replaceAll("(?i)" + term, "____");
        } else {
            // If term is not in definition, create a simple sentence
            return "Complete the sentence: " + definition + " refers to ____";
        }
    }
    
    private boolean isAnswerCorrect(String userAnswer, String correctAnswer) {
        String userLower = userAnswer.toLowerCase().trim();
        String correctLower = correctAnswer.toLowerCase().trim();
        
        // Exact match
        if (userLower.equals(correctLower)) {
            return true;
        }
        
        // Fuzzy matching - allow minor typos (Levenshtein distance <= 1 for words > 3 chars)
        if (correctAnswer.length() > 3 && calculateLevenshteinDistance(userLower, correctLower) <= 1) {
            return true;
        }
        
        // Check for common synonyms or acceptable variations
        return checkSynonyms(userLower, correctLower);
    }
    
    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                if (s1.charAt(i - 1) == s2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    dp[i][j] = 1 + Math.min(Math.min(dp[i - 1][j], dp[i][j - 1]), dp[i - 1][j - 1]);
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
    }
    
    private boolean checkSynonyms(String userAnswer, String correctAnswer) {
        // Basic synonym checking - can be expanded
        return false;
    }
    
    private void showQuizResults() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putExtra("correct_answers", correctAnswers);
        intent.putExtra("total_questions", flashcards.size());
        intent.putExtra("duration", duration);
        intent.putExtra("test_type", "Fill in the Blank");
        startActivity(intent);
        finish();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
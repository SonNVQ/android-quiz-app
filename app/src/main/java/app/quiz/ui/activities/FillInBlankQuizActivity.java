package app.quiz.ui.activities;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import app.quiz.R; // Assuming R is in app.quiz

import java.util.ArrayList;
import java.util.Collections;
import app.quiz.data.models.Flashcard;

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
    private ArrayList<Flashcard> flashcards; // Assuming Flashcard model exists
    private int currentQuestionIndex = 0;
    private boolean isRetry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_in_blank_quiz);

        // Initialize UI
        tvSentence = findViewById(R.id.tv_sentence);
        etUserAnswer = findViewById(R.id.et_user_answer);
        btnSubmit = findViewById(R.id.btn_submit);
        btnRetry = findViewById(R.id.btn_retry);
        btnSkip = findViewById(R.id.btn_skip);
        tvFeedback = findViewById(R.id.tv_feedback);
        tvHint = findViewById(R.id.tv_hint);
        tvQuestionCounter = findViewById(R.id.tv_question_counter);

        // Get data from intent
        // FlashcardGroup flashcardGroup = getIntent().getParcelableExtra(EXTRA_FLASHCARD_GROUP);
        flashcards = getIntent().getParcelableArrayListExtra(EXTRA_FLASHCARDS);
        if (flashcards == null || flashcards.size() < 3) {
            // Handle error
            finish();
            return;
        }
        Collections.shuffle(flashcards);

        loadNextQuestion();

        btnSubmit.setOnClickListener(v -> checkAnswer());
        btnRetry.setOnClickListener(v -> retryQuestion());
        btnSkip.setOnClickListener(v -> skipQuestion());

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
    }

    private void loadNextQuestion() {
        if (currentQuestionIndex >= flashcards.size()) {
            // End quiz
            finish();
            return;
        }
        Flashcard card = flashcards.get(currentQuestionIndex);
        // Assuming Flashcard has term and definition
        tvSentence.setText("Complete the sentence: " + card.getDefinition().replace(card.getTerm(), "____"));
        tvHint.setText("Hint: " + card.getDefinition());
        tvQuestionCounter.setText("Question " + (currentQuestionIndex + 1) + "/" + flashcards.size());
        etUserAnswer.setText("");
        tvFeedback.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnSkip.setVisibility(View.VISIBLE);
        btnSubmit.setVisibility(View.VISIBLE);
        isRetry = false;
    }

    private void checkAnswer() {
        String userAnswer = etUserAnswer.getText().toString().trim().toLowerCase();
        if (userAnswer.isEmpty()) {
            tvFeedback.setText("Please enter an answer before continuing.");
            tvFeedback.setVisibility(View.VISIBLE);
            return;
        }
        Flashcard card = flashcards.get(currentQuestionIndex);
        String correctAnswer = card.getTerm().toLowerCase();
        if (userAnswer.equals(correctAnswer)) {
            tvFeedback.setText("Correct!");
            tvFeedback.setVisibility(View.VISIBLE);
            currentQuestionIndex++;
            loadNextQuestion();
        } else {
            tvFeedback.setText("Incorrect. The correct answer is " + correctAnswer);
            tvFeedback.setVisibility(View.VISIBLE);
            btnRetry.setVisibility(View.VISIBLE);
            btnSubmit.setVisibility(View.GONE);
            isRetry = true;
        }
        // Update profile and progress (TODO: implement)
    }

    private void retryQuestion() {
        etUserAnswer.setText("");
        tvFeedback.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnSubmit.setVisibility(View.VISIBLE);
    }

    private void skipQuestion() {
        // Log skip and re-queue (TODO: implement)
        currentQuestionIndex++;
        loadNextQuestion();
    }
}
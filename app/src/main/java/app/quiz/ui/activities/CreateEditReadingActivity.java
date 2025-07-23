package app.quiz.ui.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Reading;
import app.quiz.data.models.ReadingCreateDTO;
import app.quiz.data.models.ReadingQuestion;
import app.quiz.data.models.ReadingUpdateDto;
import app.quiz.data.models.User;
import app.quiz.data.remote.ReadingService;
import app.quiz.ui.adapters.QuestionEditAdapter;
import app.quiz.utils.SessionManager;

public class CreateEditReadingActivity extends AppCompatActivity implements QuestionEditAdapter.OnQuestionEditListener {

    private static final String TAG = "CreateEditReadingActivity";

    // UI Components
    private TextInputEditText etTitle;
    private TextInputEditText etDescription;
    private TextInputEditText etContent;
    private TextInputEditText etImageUrl;
    private TextInputLayout tilTitle;
    private TextInputLayout tilContent;
    private RecyclerView rvQuestions;
    private MaterialButton btnAddQuestion;
    private ProgressBar progressBar;
    private QuestionEditAdapter questionAdapter;

    // Data
    private ReadingService readingService;
    private SessionManager sessionManager;
    private List<ReadingQuestion> questions;
    private Reading editingReading;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_edit_reading);

        initializeComponents();
        checkAdminAccess();
        setupUI();
        loadDataFromIntent();
    }

    private void initializeComponents() {
        readingService = ReadingService.getInstance();
        sessionManager = SessionManager.getInstance(this);
        questions = new ArrayList<>();

        // Initialize UI components
        etTitle = findViewById(R.id.et_title);
        etDescription = findViewById(R.id.et_description);
        etContent = findViewById(R.id.et_content);
        etImageUrl = findViewById(R.id.et_image_url);
        tilTitle = findViewById(R.id.til_title);
        tilContent = findViewById(R.id.til_content);
        rvQuestions = findViewById(R.id.rv_questions);
        btnAddQuestion = findViewById(R.id.btn_add_question);
        progressBar = findViewById(R.id.progress_bar);

        Log.d(TAG, "Components initialized");
    }

    private void checkAdminAccess() {
        User currentUser = sessionManager.getCurrentUser();
        if (currentUser == null || !"Admin".equals(currentUser.getRole())) {
            Toast.makeText(this, "Access denied. Admin role required.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void setupUI() {
        // Check if this is edit mode
        isEditMode = getIntent().getBooleanExtra("isEdit", false);
        editingReading = getIntent().getParcelableExtra("reading");

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Reading" : "Create Reading");
        }

        // Setup RecyclerView for questions
        questionAdapter = new QuestionEditAdapter(questions, this);
        rvQuestions.setLayoutManager(new LinearLayoutManager(this));
        rvQuestions.setAdapter(questionAdapter);

        // Setup add question button
        btnAddQuestion.setOnClickListener(v -> showAddQuestionDialog());
    }

    private void loadDataFromIntent() {
        if (isEditMode && editingReading != null) {
            // Populate fields with existing data
            etTitle.setText(editingReading.getTitle() != null ? editingReading.getTitle() : "");
            etDescription.setText(editingReading.getDescription() != null ? editingReading.getDescription() : "");
            etContent.setText(editingReading.getContent() != null ? editingReading.getContent() : "");
            etImageUrl.setText(editingReading.getImageUrl() != null ? editingReading.getImageUrl() : "");

            // Load existing questions
            if (editingReading.getQuestions() != null) {
                questions.clear();
                questions.addAll(editingReading.getQuestions());
                questionAdapter.notifyDataSetChanged();
            }
        }
    }

    private void showAddQuestionDialog() {
        String[] questionTypes = {"Single Choice", "Fill in the Blank"};
        
        new AlertDialog.Builder(this)
                .setTitle("Select Question Type")
                .setItems(questionTypes, (dialog, which) -> {
                    if (which == 0) {
                        addSingleChoiceQuestion();
                    } else {
                        addFillInTheBlankQuestion();
                    }
                })
                .show();
    }

    private void addSingleChoiceQuestion() {
        ReadingQuestion question = new ReadingQuestion();
        question.setQuestionText("");
        question.setQuestionType(1); // SingleChoice
        question.setOptionA("");
        question.setOptionB("");
        question.setOptionC("");
        question.setOptionD("");
        question.setCorrectOption("");
        
        questions.add(question);
        questionAdapter.notifyItemInserted(questions.size() - 1);
        
        // Scroll to the new question
        rvQuestions.smoothScrollToPosition(questions.size() - 1);
    }

    private void addFillInTheBlankQuestion() {
        ReadingQuestion question = new ReadingQuestion();
        question.setQuestionText("");
        question.setQuestionType(2); // FillInTheBlank
        question.setAnswer("");
        
        questions.add(question);
        questionAdapter.notifyItemInserted(questions.size() - 1);
        
        // Scroll to the new question
        rvQuestions.smoothScrollToPosition(questions.size() - 1);
    }

    @Override
    public void onQuestionDelete(int position) {
        if (position >= 0 && position < questions.size()) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Question")
                    .setMessage("Are you sure you want to delete this question?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        questions.remove(position);
                        questionAdapter.notifyItemRemoved(position);
                        questionAdapter.notifyItemRangeChanged(position, questions.size());
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    private boolean validateInput() {
        boolean isValid = true;

        // Clear previous errors
        tilTitle.setError(null);
        tilContent.setError(null);

        // Validate title
        String title = etTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            tilTitle.setError("Title is required");
            isValid = false;
        }

        // Validate content
        String content = etContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            tilContent.setError("Content is required");
            isValid = false;
        }

        // Validate questions
        for (int i = 0; i < questions.size(); i++) {
            ReadingQuestion question = questions.get(i);
            if (TextUtils.isEmpty(question.getQuestionText())) {
                Toast.makeText(this, "Question " + (i + 1) + " text is required", Toast.LENGTH_SHORT).show();
                isValid = false;
                break;
            }

            if (question.isSingleChoice()) {
                if (TextUtils.isEmpty(question.getOptionA()) || TextUtils.isEmpty(question.getOptionB())) {
                    Toast.makeText(this, "Question " + (i + 1) + " must have at least options A and B", Toast.LENGTH_SHORT).show();
                    isValid = false;
                    break;
                }
                if (TextUtils.isEmpty(question.getCorrectOption())) {
                    Toast.makeText(this, "Question " + (i + 1) + " must have a correct option selected", Toast.LENGTH_SHORT).show();
                    isValid = false;
                    break;
                }
            } else if (question.isFillInBlank()) {
                if (TextUtils.isEmpty(question.getAnswer())) {
                    Toast.makeText(this, "Question " + (i + 1) + " must have an answer", Toast.LENGTH_SHORT).show();
                    isValid = false;
                    break;
                }
            }
        }

        return isValid;
    }

    private void saveReading() {
        if (!validateInput()) {
            return;
        }

        String authToken = sessionManager.getAuthToken();
        if (authToken == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String content = etContent.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();

        if (isEditMode) {
            // Update existing reading
            ReadingUpdateDto updateDto = new ReadingUpdateDto();
            updateDto.setId(editingReading.getId());
            updateDto.setTitle(title);
            updateDto.setDescription(description.isEmpty() ? null : description);
            updateDto.setContent(content);
            updateDto.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
            updateDto.setQuestions(new ArrayList<>(questions));

            readingService.updateReading(authToken, updateDto, new ReadingService.ReadingCallback<Reading>() {
                @Override
                public void onSuccess(Reading result) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEditReadingActivity.this, "Reading updated successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error, int statusCode) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEditReadingActivity.this, "Failed to update reading: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        } else {
            // Create new reading
            ReadingCreateDTO createDto = new ReadingCreateDTO();
            createDto.setTitle(title);
            createDto.setDescription(description.isEmpty() ? null : description);
            createDto.setContent(content);
            createDto.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
            createDto.setQuestions(new ArrayList<>(questions));

            readingService.createReading(authToken, createDto, new ReadingService.ReadingCallback<Reading>() {
                @Override
                public void onSuccess(Reading result) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEditReadingActivity.this, "Reading created successfully", Toast.LENGTH_SHORT).show();
                        setResult(RESULT_OK);
                        finish();
                    });
                }

                @Override
                public void onError(String error, int statusCode) {
                    runOnUiThread(() -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(CreateEditReadingActivity.this, "Failed to create reading: " + error, Toast.LENGTH_LONG).show();
                    });
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_edit_reading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save) {
            saveReading();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
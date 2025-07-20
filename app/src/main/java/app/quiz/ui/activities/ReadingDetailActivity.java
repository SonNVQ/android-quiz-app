package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

import app.quiz.R;
import app.quiz.data.models.Reading;
import app.quiz.data.models.ReadingQuestion;
import app.quiz.data.remote.ReadingService;

public class ReadingDetailActivity extends AppCompatActivity {

    private static final String TAG = "ReadingDetailActivity";
    public static final String EXTRA_READING_ID = "extra_reading_id";

    // UI Components
    private TextView tvTitle;
    private TextView tvDescription;
    private TextView tvContent;
    private ImageView ivImage;
    private ProgressBar progressBar;
    private MaterialButton btnTakeTest;

    private ReadingService readingService;
    private String readingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_detail);

        initializeComponents();
        loadReadingDetails();
    }

    private void initializeComponents() {
        readingService = ReadingService.getInstance();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reading Detail");
        }

        // Initialize UI components
        tvTitle = findViewById(R.id.tv_title);
        tvDescription = findViewById(R.id.tv_description);
        tvContent = findViewById(R.id.tv_content);
        ivImage = findViewById(R.id.iv_image);
        progressBar = findViewById(R.id.progress_bar);
        btnTakeTest = findViewById(R.id.btn_take_test);

        // Get reading ID from intent
        readingId = getIntent().getStringExtra(EXTRA_READING_ID);
        if (readingId == null) {
            Toast.makeText(this, "No reading ID provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadReadingDetails() {
        progressBar.setVisibility(View.VISIBLE);

        readingService.getReadingById(readingId, new ReadingService.ReadingCallback<Reading>() {
            @Override
            public void onSuccess(Reading result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    displayReading(result);
                });
            }

            @Override
            public void onError(String error, int statusCode) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(ReadingDetailActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayReading(Reading reading) {
        tvTitle.setText(reading.getTitle());
        tvDescription.setText(reading.getDescription());
        tvContent.setText(reading.getContent());

        if (reading.getImageUrl() != null && !reading.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(reading.getImageUrl())
                    .into(ivImage);
        } else {
            ivImage.setVisibility(View.GONE);
        }
        
        // Setup test button
        if (reading.getQuestions() != null && !reading.getQuestions().isEmpty()) {
            btnTakeTest.setVisibility(View.VISIBLE);
            btnTakeTest.setOnClickListener(v -> startReadingTest(reading));
        } else {
            btnTakeTest.setVisibility(View.GONE);
        }
    }
    
    private void startReadingTest(Reading reading) {
        if (reading.getQuestions() == null || reading.getQuestions().isEmpty()) {
            Toast.makeText(this, "No test available for this reading", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(this, ReadingTestActivity.class);
        intent.putExtra(ReadingTestActivity.EXTRA_READING_ID, reading.getId());
        intent.putExtra(ReadingTestActivity.EXTRA_READING_TITLE, reading.getTitle());
        intent.putParcelableArrayListExtra(ReadingTestActivity.EXTRA_QUESTIONS, 
                new ArrayList<>(reading.getQuestions()));
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.PagedResponse;
import app.quiz.data.models.Reading;
import app.quiz.data.models.User;
import app.quiz.data.remote.ReadingService;
import app.quiz.ui.adapters.AdminReadingAdapter;
import app.quiz.utils.SessionManager;

public class AdminReadingActivity extends AppCompatActivity implements AdminReadingAdapter.OnAdminReadingClickListener {

    private static final String TAG = "AdminReadingActivity";
    private static final int PAGE_SIZE = 10;
    private static final int REQUEST_CREATE_READING = 1001;
    private static final int REQUEST_EDIT_READING = 1002;

    // UI Components
    private RecyclerView recyclerView;
    private AdminReadingAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private View layoutEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputEditText etSearch;
    private FloatingActionButton fabAdd;

    // Data
    private ReadingService readingService;
    private SessionManager sessionManager;
    private List<Reading> readings;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reading);

        initializeComponents();
        checkAdminAccess();
        setupUI();
        loadReadings(true);
    }

    private void initializeComponents() {
        readingService = ReadingService.getInstance();
        sessionManager = SessionManager.getInstance(this);
        readings = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Manage Readings");
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.rv_readings);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        etSearch = findViewById(R.id.et_search);
        fabAdd = findViewById(R.id.fab_add);

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
        // Setup RecyclerView
        adapter = new AdminReadingAdapter(readings, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup pagination scroll listener
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                    if (!isLoading && hasMorePages) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                            loadMoreReadings();
                        }
                    }
                }
            }
        });

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            hasMorePages = true;
            loadReadings(true);
        });

        // Setup search functionality
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.equals(currentSearchQuery)) {
                    currentSearchQuery = query;
                    currentPage = 1;
                    hasMorePages = true;
                    loadReadings(true);
                }
            }
        });

        // Setup FAB
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateEditReadingActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_READING);
        });
    }

    private void loadReadings(boolean clearExisting) {
        if (isLoading) return;

        isLoading = true;

        if (clearExisting) {
            progressBar.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
        }

        readingService.getPublicReadings(currentPage, PAGE_SIZE, currentSearchQuery,
                new ReadingService.ReadingCallback<PagedResponse<Reading>>() {
                    @Override
                    public void onSuccess(PagedResponse<Reading> result) {
                        runOnUiThread(() -> {
                            isLoading = false;
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);

                            if (clearExisting) {
                                readings.clear();
                            }

                            if (result.getItems() != null && !result.getItems().isEmpty()) {
                                readings.addAll(result.getItems());
                                adapter.notifyDataSetChanged();

                                hasMorePages = result.hasNextPage();

                                layoutEmptyState.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                            } else if (readings.isEmpty()) {
                                showEmptyState();
                            }
                        });
                    }

                    @Override
                    public void onError(String error, int statusCode) {
                        runOnUiThread(() -> {
                            isLoading = false;
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);

                            if (readings.isEmpty()) {
                                showEmptyState();
                            }

                            Toast.makeText(AdminReadingActivity.this, error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }

    private void loadMoreReadings() {
        if (!hasMorePages || isLoading) return;

        currentPage++;
        loadReadings(false);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        tvEmptyTitle.setText("No Readings Found");
        
        if (currentSearchQuery.isEmpty()) {
            tvEmptyMessage.setText("No readings available. Tap the + button to create your first reading.");
        } else {
            tvEmptyMessage.setText("No readings found matching '" + currentSearchQuery + "'. Try a different search term.");
        }
    }

    @Override
    public void onReadingClick(Reading reading) {
        Intent intent = new Intent(this, ReadingDetailActivity.class);
        intent.putExtra(ReadingDetailActivity.EXTRA_READING_ID, reading.getId());
        startActivity(intent);
    }

    @Override
    public void onEditClick(Reading reading) {
        progressBar.setVisibility(View.VISIBLE);
        readingService.getReadingById(reading.getId(), new ReadingService.ReadingCallback<Reading>() {
            @Override
            public void onSuccess(Reading fullReading) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Intent intent = new Intent(AdminReadingActivity.this, CreateEditReadingActivity.class);
                    intent.putExtra("reading", fullReading);
                    intent.putExtra("isEdit", true);
                    startActivityForResult(intent, REQUEST_EDIT_READING);
                });
            }

            @Override
            public void onError(String error, int statusCode) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminReadingActivity.this, "Failed to load reading details: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    public void onDeleteClick(Reading reading) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Reading")
                .setMessage("Are you sure you want to delete '" + reading.getTitle() + "'? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteReading(reading))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteReading(Reading reading) {
        String authToken = sessionManager.getAuthToken();
        if (authToken == null) {
            Toast.makeText(this, "Authentication required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        readingService.deleteReading(authToken, reading.getId(), new ReadingService.ReadingCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminReadingActivity.this, "Reading deleted successfully", Toast.LENGTH_SHORT).show();
                    
                    // Refresh the list
                    currentPage = 1;
                    hasMorePages = true;
                    loadReadings(true);
                });
            }

            @Override
            public void onError(String error, int statusCode) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AdminReadingActivity.this, "Failed to delete reading: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (resultCode == RESULT_OK && (requestCode == REQUEST_CREATE_READING || requestCode == REQUEST_EDIT_READING)) {
            // Refresh the list after creating or editing
            currentPage = 1;
            hasMorePages = true;
            loadReadings(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_reading, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh) {
            currentPage = 1;
            hasMorePages = true;
            loadReadings(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
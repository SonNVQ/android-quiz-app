package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.PagedResponse;
import app.quiz.data.models.Reading;
import app.quiz.data.remote.ReadingService;
import app.quiz.ui.adapters.ReadingAdapter;

public class ReadingListActivity extends AppCompatActivity implements ReadingAdapter.OnReadingClickListener {

    private static final String TAG = "ReadingListActivity";
    private static final int PAGE_SIZE = 10;

    // UI Components
    private RecyclerView recyclerView;
    private ReadingAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private View layoutEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputEditText etSearch;

    // Data
    private ReadingService readingService;
    private List<Reading> readings;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private String currentSearchQuery = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_list);

        initializeComponents();
        setupUI();
        loadReadings(true);
    }

    private void initializeComponents() {
        readingService = ReadingService.getInstance();
        readings = new ArrayList<>();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Practice Reading");
        }

        // Initialize UI components
        recyclerView = findViewById(R.id.rv_readings);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        etSearch = findViewById(R.id.et_search);

        Log.d(TAG, "Components initialized");
    }

    private void setupUI() {
        // Setup RecyclerView
        adapter = new ReadingAdapter(readings, this);
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

                            Toast.makeText(ReadingListActivity.this, error, Toast.LENGTH_LONG).show();
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

        if (currentSearchQuery.isEmpty()) {
            tvEmptyTitle.setText("No readings available");
            tvEmptyMessage.setText("No practice readings available at the moment. Please check back later.");
        } else {
            tvEmptyTitle.setText("No results found");
            tvEmptyMessage.setText("No readings found for '" + currentSearchQuery + "'. Try a different search term.");
        }
    }

    @Override
    public void onReadingClick(Reading reading) {
        Intent intent = new Intent(this, ReadingDetailActivity.class);
        intent.putExtra(ReadingDetailActivity.EXTRA_READING_ID, reading.getId());
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
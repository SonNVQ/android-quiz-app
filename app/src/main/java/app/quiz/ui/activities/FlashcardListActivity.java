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
import app.quiz.data.models.FlashcardGroup;
import app.quiz.data.models.PagedResponse;
import app.quiz.data.remote.FlashcardService;
import app.quiz.ui.adapters.FlashcardGroupAdapter;

/**
 * FlashcardListActivity - Displays list of public flashcard sets
 * Implements UC-06: View public flashcard list
 */
public class FlashcardListActivity extends AppCompatActivity implements 
        FlashcardGroupAdapter.OnFlashcardGroupClickListener {
    
    private static final String TAG = "FlashcardListActivity";
    private static final int PAGE_SIZE = 10;
    
    // UI Components
    private RecyclerView recyclerView;
    private FlashcardGroupAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmptyTitle;
    private TextView tvEmptyMessage;
    private View layoutEmptyState;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextInputEditText etSearch;
    
    // Data
    private FlashcardService flashcardService;
    private List<FlashcardGroup> flashcardGroups;
    private int currentPage = 1;
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private String currentSearchQuery = "";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_list);
        
        initializeComponents();
        setupUI();
        loadFlashcards(true);
    }
    
    /**
     * Initialize components and dependencies
     */
    private void initializeComponents() {
        flashcardService = FlashcardService.getInstance();
        flashcardGroups = new ArrayList<>();
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Public Flashcards");
        }
        
        // Initialize UI components
        recyclerView = findViewById(R.id.rv_flashcards);
        progressBar = findViewById(R.id.progress_bar);
        tvEmptyTitle = findViewById(R.id.tv_empty_title);
        tvEmptyMessage = findViewById(R.id.tv_empty_message);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh);
        etSearch = findViewById(R.id.et_search);
        
        Log.d(TAG, "Components initialized");
    }
    
    /**
     * Setup UI components and listeners
     */
    private void setupUI() {
        // Setup RecyclerView
        adapter = new FlashcardGroupAdapter(flashcardGroups, this);
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
                    
                    Log.d(TAG, "Scroll - visible: " + visibleItemCount + ", total: " + totalItemCount + 
                          ", firstVisible: " + firstVisibleItemPosition + ", isLoading: " + isLoading + 
                          ", hasMorePages: " + hasMorePages + ", currentPage: " + currentPage);
                    
                    if (!isLoading && hasMorePages) {
                        if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 2) {
                            Log.d(TAG, "Triggering loadMoreFlashcards");
                            loadMoreFlashcards();
                        }
                    }
                }
            }
        });
        
        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(() -> {
            currentPage = 1;
            hasMorePages = true;
            loadFlashcards(true);
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
                    loadFlashcards(true);
                }
            }
        });
    }
    
    /**
     * Load flashcards from API
     * Implements UC-06 normal sequence step 3: retrieve all public flashcard sets
     * 
     * @param clearExisting Whether to clear existing data
     */
    private void loadFlashcards(boolean clearExisting) {
        if (isLoading) return;
        
        isLoading = true;
        
        if (clearExisting) {
            currentPage = 1;
            hasMorePages = true;
            progressBar.setVisibility(View.VISIBLE);
            layoutEmptyState.setVisibility(View.GONE);
            Log.d(TAG, "Starting fresh load from page 1");
        }
        
        flashcardService.getPublicFlashcards(currentPage, PAGE_SIZE, currentSearchQuery, 
                new FlashcardService.FlashcardCallback<PagedResponse<FlashcardGroup>>() {
                    @Override
                    public void onSuccess(PagedResponse<FlashcardGroup> result) {
                        runOnUiThread(() -> {
                            isLoading = false;
                            progressBar.setVisibility(View.GONE);
                            swipeRefreshLayout.setRefreshing(false);
                            
                            if (clearExisting) {
                                flashcardGroups.clear();
                            }
                            
                            if (result.getItems() != null && !result.getItems().isEmpty()) {
                                int previousSize = flashcardGroups.size();
                                flashcardGroups.addAll(result.getItems());
                                adapter.notifyDataSetChanged();
                                
                                // Update pagination state
                                hasMorePages = result.hasNextPage();
                                
                                // Hide empty state
                                layoutEmptyState.setVisibility(View.GONE);
                                recyclerView.setVisibility(View.VISIBLE);
                                
                                Log.d(TAG, "Loaded " + result.getItems().size() + " flashcard groups. Total: " + flashcardGroups.size() + 
                                      " (was " + previousSize + "). Page: " + currentPage + ", hasMorePages: " + hasMorePages + 
                                      ", totalPages: " + result.getTotalPages() + ", totalCount: " + result.getTotalCount());
                            } else if (flashcardGroups.isEmpty()) {
                                // Show empty state (EX-01: No public flashcard sets found)
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
                            
                            Log.e(TAG, "Failed to load flashcards: " + error);
                            
                            if (flashcardGroups.isEmpty()) {
                                showEmptyState();
                            }
                            
                            Toast.makeText(FlashcardListActivity.this, error, Toast.LENGTH_LONG).show();
                        });
                    }
                });
    }
    
    /**
     * Load more flashcards for pagination
     */
    private void loadMoreFlashcards() {
        Log.d(TAG, "loadMoreFlashcards called - hasMorePages: " + hasMorePages + ", isLoading: " + isLoading);
        if (!hasMorePages || isLoading) {
            Log.d(TAG, "loadMoreFlashcards aborted - hasMorePages: " + hasMorePages + ", isLoading: " + isLoading);
            return;
        }
        
        currentPage++;
        Log.d(TAG, "Loading page: " + currentPage);
        loadFlashcards(false);
    }
    
    /**
     * Show empty state when no flashcards are found
     * Implements EX-01: No public flashcard sets found
     */
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        
        if (currentSearchQuery.isEmpty()) {
            tvEmptyTitle.setText("No flashcards available");
            tvEmptyMessage.setText("No flashcards available at the moment. Please check back later.");
        } else {
            tvEmptyTitle.setText("No results found");
            tvEmptyMessage.setText("No flashcards found for '" + currentSearchQuery + "'. Try a different search term.");
        }
    }
    
    /**
     * Handle flashcard group click
     * Implements UC-06 normal sequence step 6: user selects a flashcard set
     */
    @Override
    public void onFlashcardGroupClick(FlashcardGroup flashcardGroup) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.EXTRA_FLASHCARD_GROUP, flashcardGroup);
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
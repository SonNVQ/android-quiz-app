package app.quiz.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import app.quiz.R;
import app.quiz.data.models.FlashcardGroup;
import app.quiz.data.models.PagedResponse;
import app.quiz.data.remote.FlashcardService;
import app.quiz.ui.activities.CreateFlashcardActivity;
import app.quiz.ui.adapters.FlashcardGroupAdapter;
import app.quiz.utils.SessionManager;

/**
 * Activity for displaying user's personal flashcard groups (UC-08: View my flashcards)
 * Allows users to view, search, and filter their created flashcard sets
 */
public class MyFlashcardsActivity extends AppCompatActivity implements FlashcardGroupAdapter.OnFlashcardGroupClickListener {
    
    private static final String TAG = "MyFlashcardsActivity";
    private static final int PAGE_SIZE = 20;
    private static final int REQUEST_CREATE_FLASHCARD = 1001;
    
    // UI Components
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private LinearLayout emptyStateLayout;
    private TextView emptyTitle;
    private TextView emptyMessage;
    private MaterialButton btnCreateFlashcard;
    private TextInputEditText searchEditText;
    private FloatingActionButton fabFilter;
    private FloatingActionButton fabCreateFlashcard;
    private MaterialCardView filterCard;
    private ChipGroup chipGroupSort;
    private Chip chipRecent, chipAlphabetical, chipOldest;
    
    // Data and Adapters
    private FlashcardGroupAdapter adapter;
    private List<FlashcardGroup> flashcardGroups;
    private List<FlashcardGroup> filteredFlashcardGroups;
    private FlashcardService flashcardService;
    private SessionManager sessionManager;
    
    // State variables
    private boolean isLoading = false;
    private boolean hasMorePages = true;
    private int currentPage = 1;
    private String currentSearchQuery = "";
    private SortOption currentSortOption = SortOption.RECENT;
    
    // Sort options enum
    private enum SortOption {
        RECENT, ALPHABETICAL, OLDEST
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flashcards);
        
        // Initialize services
        flashcardService = FlashcardService.getInstance();
        sessionManager = SessionManager.getInstance(this);
        
        // Check if user is logged in
        if (!sessionManager.isLoggedIn()) {
            // Redirect to login if not authenticated
            finish();
            return;
        }
        
        initializeViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupFilter();
        setupEmptyState();
        setupCreateFlashcardFab();
        
        // Load initial data
        loadMyFlashcards(true);
    }
    
    private void initializeViews() {
        toolbar = findViewById(R.id.toolbar);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        recyclerView = findViewById(R.id.rv_flashcards);
        progressBar = findViewById(R.id.progress_bar);
        emptyStateLayout = findViewById(R.id.layout_empty_state);
        emptyTitle = findViewById(R.id.tv_empty_title);
        emptyMessage = findViewById(R.id.tv_empty_message);
        btnCreateFlashcard = findViewById(R.id.btn_create_flashcard);
        searchEditText = findViewById(R.id.et_search);
        fabFilter = findViewById(R.id.fab_filter);
        fabCreateFlashcard = findViewById(R.id.fab_create_flashcard);
        filterCard = findViewById(R.id.card_filter);
        chipGroupSort = findViewById(R.id.chip_group_sort);
        chipRecent = findViewById(R.id.chip_recent);
        chipAlphabetical = findViewById(R.id.chip_alphabetical);
        chipOldest = findViewById(R.id.chip_oldest);
        
        // Initialize data lists
        flashcardGroups = new ArrayList<>();
        filteredFlashcardGroups = new ArrayList<>();
    }
    
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }
    
    private void setupRecyclerView() {
        adapter = new FlashcardGroupAdapter(filteredFlashcardGroups, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && !isLoading && hasMorePages) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
                    
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 5) {
                        loadMoreFlashcards();
                    }
                }
            }
        });
    }
    
    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1;
            hasMorePages = true;
            loadMyFlashcards(true);
        });
    }
    
    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(Editable s) {
                currentSearchQuery = s.toString().trim();
                filterFlashcards();
            }
        });
    }
    
    private void setupFilter() {
        fabFilter.setOnClickListener(v -> {
            boolean isVisible = filterCard.getVisibility() == View.VISIBLE;
            filterCard.setVisibility(isVisible ? View.GONE : View.VISIBLE);
        });
        
        chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (!checkedIds.isEmpty()) {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chip_recent) {
                    currentSortOption = SortOption.RECENT;
                } else if (checkedId == R.id.chip_alphabetical) {
                    currentSortOption = SortOption.ALPHABETICAL;
                } else if (checkedId == R.id.chip_oldest) {
                    currentSortOption = SortOption.OLDEST;
                }
                sortFlashcards();
            }
        });
    }
    
    private void setupEmptyState() {
        btnCreateFlashcard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateFlashcardActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_FLASHCARD);
        });
    }
    
    private void setupCreateFlashcardFab() {
        fabCreateFlashcard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CreateFlashcardActivity.class);
            startActivityForResult(intent, REQUEST_CREATE_FLASHCARD);
        });
    }
    
    private void loadMyFlashcards(boolean refresh) {
        if (isLoading) return;
        
        isLoading = true;
        
        if (refresh) {
            showLoading(true);
            flashcardGroups.clear();
            filteredFlashcardGroups.clear();
            adapter.notifyDataSetChanged();
        }
        
        String authToken = sessionManager.getAuthToken();
        
        new Thread(() -> {
            try {
                flashcardService.getUserFlashcards(authToken, currentPage, PAGE_SIZE, currentSearchQuery, new FlashcardService.FlashcardCallback<PagedResponse<FlashcardGroup>>() {
                    @Override
                    public void onSuccess(PagedResponse<FlashcardGroup> response) {
                        runOnUiThread(() -> {
                            handleFlashcardsLoaded(response, refresh);
                        });
                    }
                    
                    @Override
                    public void onError(String error, int statusCode) {
                        runOnUiThread(() -> {
                            handleLoadError(error);
                        });
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error loading flashcards", e);
                runOnUiThread(() -> {
                    handleLoadError(e.getMessage());
                });
            }
        }).start();
    }
    
    private void loadMoreFlashcards() {
        if (isLoading || !hasMorePages) return;
        
        currentPage++;
        loadMyFlashcards(false);
    }
    
    private void handleFlashcardsLoaded(PagedResponse<FlashcardGroup> response, boolean refresh) {
        isLoading = false;
        showLoading(false);
        swipeRefresh.setRefreshing(false);
        
        if (response != null && response.getItems() != null) {
            if (refresh) {
                flashcardGroups.clear();
            }
            flashcardGroups.addAll(response.getItems());
            hasMorePages = response.getPageNumber() < response.getTotalPages();
            
            filterFlashcards();
            
            if (flashcardGroups.isEmpty()) {
                showEmptyState();
            } else {
                hideEmptyState();
            }
        } else {
            if (flashcardGroups.isEmpty()) {
                showEmptyState();
            }
        }
    }
    
    private void handleLoadError(String errorMessage) {
        isLoading = false;
        showLoading(false);
        swipeRefresh.setRefreshing(false);
        
        Log.e(TAG, "Error loading flashcards: " + errorMessage);
        
        String displayMessage;
        if (errorMessage.toLowerCase().contains("network") || errorMessage.toLowerCase().contains("connection")) {
            displayMessage = getString(R.string.connection_error);
        } else {
            displayMessage = getString(R.string.error_loading_my_flashcards);
        }
        
        Toast.makeText(this, displayMessage, Toast.LENGTH_LONG).show();
        
        if (flashcardGroups.isEmpty()) {
            showEmptyState();
        }
    }
    
    private void filterFlashcards() {
        filteredFlashcardGroups.clear();
        
        if (currentSearchQuery.isEmpty()) {
            filteredFlashcardGroups.addAll(flashcardGroups);
        } else {
            String query = currentSearchQuery.toLowerCase();
            for (FlashcardGroup group : flashcardGroups) {
                if (group.getName().toLowerCase().contains(query) ||
                    (group.getDescription() != null && group.getDescription().toLowerCase().contains(query))) {
                    filteredFlashcardGroups.add(group);
                }
            }
        }
        
        sortFlashcards();
    }
    
    private void sortFlashcards() {
        switch (currentSortOption) {
            case ALPHABETICAL:
                Collections.sort(filteredFlashcardGroups, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                break;
            case OLDEST:
                Collections.sort(filteredFlashcardGroups, (a, b) -> a.getName().compareTo(b.getName()));
                break;
            case RECENT:
            default:
                Collections.sort(filteredFlashcardGroups, (a, b) -> b.getName().compareTo(a.getName()));
                break;
        }
        
        adapter.notifyDataSetChanged();
    }
    
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }
    
    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
    
    private void hideEmptyState() {
        emptyStateLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }
    
    @Override
    public void onFlashcardGroupClick(FlashcardGroup flashcardGroup) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra("extra_flashcard_group", flashcardGroup);
        startActivity(intent);
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
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        if (!flashcardGroups.isEmpty()) {
            currentPage = 1;
            hasMorePages = true;
            loadMyFlashcards(true);
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == REQUEST_CREATE_FLASHCARD && resultCode == RESULT_OK) {
            // Refresh the flashcard list after creating a new one
            currentPage = 1;
            hasMorePages = true;
            loadMyFlashcards(true);
            
            // Show success message
            Toast.makeText(this, "Flashcard created successfully!", Toast.LENGTH_SHORT).show();
        }
    }
}
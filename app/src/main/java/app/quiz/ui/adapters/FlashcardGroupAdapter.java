package app.quiz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.FlashcardGroup;

/**
 * Adapter for displaying flashcard groups in RecyclerView
 * Implements UC-06: Display flashcard sets with title, author, number of cards, etc.
 */
public class FlashcardGroupAdapter extends RecyclerView.Adapter<FlashcardGroupAdapter.FlashcardGroupViewHolder> {
    
    private List<FlashcardGroup> flashcardGroups;
    private OnFlashcardGroupClickListener listener;
    
    /**
     * Interface for handling flashcard group clicks
     */
    public interface OnFlashcardGroupClickListener {
        void onFlashcardGroupClick(FlashcardGroup flashcardGroup);
    }
    
    /**
     * Constructor
     * 
     * @param flashcardGroups List of flashcard groups
     * @param listener Click listener
     */
    public FlashcardGroupAdapter(List<FlashcardGroup> flashcardGroups, OnFlashcardGroupClickListener listener) {
        this.flashcardGroups = flashcardGroups;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FlashcardGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_group, parent, false);
        return new FlashcardGroupViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FlashcardGroupViewHolder holder, int position) {
        FlashcardGroup flashcardGroup = flashcardGroups.get(position);
        holder.bind(flashcardGroup);
    }
    
    @Override
    public int getItemCount() {
        return flashcardGroups != null ? flashcardGroups.size() : 0;
    }
    
    /**
     * Update the flashcard groups list
     * 
     * @param newFlashcardGroups New list of flashcard groups
     */
    public void updateFlashcardGroups(List<FlashcardGroup> newFlashcardGroups) {
        this.flashcardGroups.clear();
        if (newFlashcardGroups != null) {
            this.flashcardGroups.addAll(newFlashcardGroups);
        }
        notifyDataSetChanged();
    }
    
    /**
     * ViewHolder for flashcard group items
     */
    class FlashcardGroupViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvCardCount;
        private TextView tvPublicIndicator;
        
        public FlashcardGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvCardCount = itemView.findViewById(R.id.tv_card_count);
            tvPublicIndicator = itemView.findViewById(R.id.tv_public_indicator);
            
            // Set click listener
            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onFlashcardGroupClick(flashcardGroups.get(getAdapterPosition()));
                }
            });
        }
        
        /**
         * Bind flashcard group data to views
         * Implements UC-06 normal sequence step 4: display title, description, number of cards
         * 
         * @param flashcardGroup Flashcard group to bind
         */
        public void bind(FlashcardGroup flashcardGroup) {
            // Set title
            tvTitle.setText(flashcardGroup.getName());
            
            // Set description
            if (flashcardGroup.getDescription() != null && !flashcardGroup.getDescription().trim().isEmpty()) {
                tvDescription.setText(flashcardGroup.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
            
            // Set card count
            int cardCount = flashcardGroup.getFlashcardCount();
            String cardCountText = cardCount == 1 ? "1 card" : cardCount + " cards";
            tvCardCount.setText(cardCountText);
            
            // Set public indicator
            if (flashcardGroup.isPublic()) {
                tvPublicIndicator.setText("Public");
                tvPublicIndicator.setVisibility(View.VISIBLE);
            } else {
                tvPublicIndicator.setVisibility(View.GONE);
            }
        }
    }
}
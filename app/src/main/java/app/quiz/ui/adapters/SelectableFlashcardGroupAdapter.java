package app.quiz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.FlashcardGroup;

/**
 * SelectableFlashcardGroupAdapter - RecyclerView adapter for selecting flashcard groups
 * Used in FlashcardTestSetupActivity for test configuration
 */
public class SelectableFlashcardGroupAdapter extends RecyclerView.Adapter<SelectableFlashcardGroupAdapter.SelectableFlashcardGroupViewHolder> {
    
    public interface OnGroupSelectedListener {
        void onGroupSelected(FlashcardGroup group);
    }
    
    private List<FlashcardGroup> flashcardGroups;
    private OnGroupSelectedListener listener;
    private FlashcardGroup selectedGroup;
    
    public SelectableFlashcardGroupAdapter(List<FlashcardGroup> flashcardGroups, OnGroupSelectedListener listener) {
        this.flashcardGroups = flashcardGroups;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public SelectableFlashcardGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_group_selectable, parent, false);
        return new SelectableFlashcardGroupViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull SelectableFlashcardGroupViewHolder holder, int position) {
        FlashcardGroup group = flashcardGroups.get(position);
        boolean isSelected = group.equals(selectedGroup);
        holder.bind(group, isSelected, listener);
    }
    
    @Override
    public int getItemCount() {
        return flashcardGroups.size();
    }
    
    public void setSelectedGroup(FlashcardGroup group) {
        FlashcardGroup previousSelected = selectedGroup;
        selectedGroup = group;
        
        // Notify changes for previous and current selection
        if (previousSelected != null) {
            int previousIndex = flashcardGroups.indexOf(previousSelected);
            if (previousIndex != -1) {
                notifyItemChanged(previousIndex);
            }
        }
        
        int currentIndex = flashcardGroups.indexOf(group);
        if (currentIndex != -1) {
            notifyItemChanged(currentIndex);
        }
    }
    
    public FlashcardGroup getSelectedGroup() {
        return selectedGroup;
    }
    
    public void updateFlashcardGroups(List<FlashcardGroup> newFlashcardGroups) {
        this.flashcardGroups = newFlashcardGroups;
        selectedGroup = null; // Reset selection when data changes
        notifyDataSetChanged();
    }
    
    static class SelectableFlashcardGroupViewHolder extends RecyclerView.ViewHolder {
        
        private MaterialCardView cardView;
        private TextView tvGroupName;
        private TextView tvGroupDescription;
        private TextView tvFlashcardCount;
        private View selectionIndicator;
        private ImageView ivSelectionCheck;
        
        public SelectableFlashcardGroupViewHolder(@NonNull View itemView) {
            super(itemView);
            
            cardView = (MaterialCardView) itemView;
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvGroupDescription = itemView.findViewById(R.id.tv_group_description);
            tvFlashcardCount = itemView.findViewById(R.id.tv_flashcard_count);
            selectionIndicator = itemView.findViewById(R.id.selection_indicator);
            ivSelectionCheck = itemView.findViewById(R.id.iv_selection_check);
        }
        
        public void bind(FlashcardGroup group, boolean isSelected, OnGroupSelectedListener listener) {
            tvGroupName.setText(group.getName());
            tvGroupDescription.setText(group.getDescription());
            
            int flashcardCount = group.getFlashcards().size();
            tvFlashcardCount.setText(String.format("%d card%s", 
                flashcardCount, flashcardCount == 1 ? "" : "s"));
            
            // Update selection state
            updateSelectionState(isSelected);
            
            // Set click listener
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGroupSelected(group);
                }
            });
        }
        
        private void updateSelectionState(boolean isSelected) {
            if (isSelected) {
                cardView.setStrokeColor(itemView.getContext().getResources()
                        .getColor(R.color.primary_color, null));
                cardView.setStrokeWidth(4);
                selectionIndicator.setVisibility(View.VISIBLE);
                ivSelectionCheck.setVisibility(View.VISIBLE);
                cardView.setCardElevation(8f);
            } else {
                cardView.setStrokeColor(itemView.getContext().getResources()
                        .getColor(android.R.color.transparent, null));
                cardView.setStrokeWidth(0);
                selectionIndicator.setVisibility(View.GONE);
                ivSelectionCheck.setVisibility(View.GONE);
                cardView.setCardElevation(4f);
            }
        }
    }
}
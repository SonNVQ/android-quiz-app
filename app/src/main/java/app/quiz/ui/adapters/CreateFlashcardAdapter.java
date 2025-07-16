package app.quiz.ui.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Flashcard;

/**
 * Adapter for managing flashcard items in the create flashcard activity
 */
public class CreateFlashcardAdapter extends RecyclerView.Adapter<CreateFlashcardAdapter.FlashcardViewHolder> {
    
    private List<Flashcard> flashcards;
    private OnFlashcardActionListener listener;
    
    public interface OnFlashcardActionListener {
        void onDeleteFlashcard(int position);
        void onFlashcardChanged(int position, String term, String definition);
    }
    
    public CreateFlashcardAdapter(List<Flashcard> flashcards, OnFlashcardActionListener listener) {
        this.flashcards = flashcards;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_create_flashcard, parent, false);
        return new FlashcardViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        holder.bind(flashcards.get(position), position);
    }
    
    @Override
    public int getItemCount() {
        return flashcards.size();
    }
    
    public void updateFlashcards(List<Flashcard> newFlashcards) {
        this.flashcards = newFlashcards;
        notifyDataSetChanged();
    }
    
    class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCardNumber;
        private MaterialButton btnDeleteCard;
        private TextInputEditText etTerm;
        private TextInputEditText etDefinition;
        
        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCardNumber = itemView.findViewById(R.id.tv_card_number);
            btnDeleteCard = itemView.findViewById(R.id.btn_delete_card);
            etTerm = itemView.findViewById(R.id.et_term);
            etDefinition = itemView.findViewById(R.id.et_definition);
        }
        
        public void bind(Flashcard flashcard, int position) {
            // Set card number
            tvCardNumber.setText("Card " + (position + 1));
            
            // Set flashcard data
            etTerm.setText(flashcard.getTerm());
            etDefinition.setText(flashcard.getDefinition());
            
            // Set delete button click listener
            btnDeleteCard.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteFlashcard(getAdapterPosition());
                }
            });
            
            // Add text watchers to notify changes
            etTerm.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null) {
                        String definition = etDefinition.getText() != null ? etDefinition.getText().toString() : "";
                        listener.onFlashcardChanged(getAdapterPosition(), s.toString(), definition);
                    }
                }
            });
            
            etDefinition.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                
                @Override
                public void afterTextChanged(Editable s) {
                    if (listener != null) {
                        String term = etTerm.getText() != null ? etTerm.getText().toString() : "";
                        listener.onFlashcardChanged(getAdapterPosition(), term, s.toString());
                    }
                }
            });
        }
    }
}
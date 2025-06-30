package app.quiz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Flashcard;

public class FlashcardListAdapter extends RecyclerView.Adapter<FlashcardListAdapter.FlashcardListViewHolder> {

    private List<Flashcard> flashcards;
    private OnFlashcardClickListener listener;

    public interface OnFlashcardClickListener {
        void onFlashcardClick(int position);
    }

    public FlashcardListAdapter(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
    }

    public void setOnFlashcardClickListener(OnFlashcardClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_list, parent, false);
        return new FlashcardListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardListViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard, position);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public class FlashcardListViewHolder extends RecyclerView.ViewHolder {
        private TextView tvListTerm, tvListDefinition;

        public FlashcardListViewHolder(@NonNull View itemView) {
            super(itemView);
            tvListTerm = itemView.findViewById(R.id.tv_list_term);
            tvListDefinition = itemView.findViewById(R.id.tv_list_definition);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onFlashcardClick(position);
                    }
                }
            });
        }

        public void bind(Flashcard flashcard, int position) {
            tvListTerm.setText(flashcard.getTerm());
            tvListDefinition.setText(flashcard.getDefinition());
        }
    }

    public void updateFlashcards(List<Flashcard> newFlashcards) {
        this.flashcards = newFlashcards;
        notifyDataSetChanged();
    }
}
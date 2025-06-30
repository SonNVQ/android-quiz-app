package app.quiz.ui.adapters;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Flashcard;

public class FlashcardSliderAdapter extends RecyclerView.Adapter<FlashcardSliderAdapter.FlashcardViewHolder> {

    private List<Flashcard> flashcards;
    private boolean[] isFlipped;

    public FlashcardSliderAdapter(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
        this.isFlipped = new boolean[flashcards.size()];
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_flashcard_flip, parent, false);
        return new FlashcardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard flashcard = flashcards.get(position);
        holder.bind(flashcard, position);
    }

    @Override
    public int getItemCount() {
        return flashcards.size();
    }

    public class FlashcardViewHolder extends RecyclerView.ViewHolder {
        private MaterialCardView cardFront, cardBack;
        private TextView tvTerm, tvDefinition;
        private boolean isCurrentlyFlipped = false;

        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardFront = itemView.findViewById(R.id.card_front);
            cardBack = itemView.findViewById(R.id.card_back);
            tvTerm = itemView.findViewById(R.id.tv_term);
            tvDefinition = itemView.findViewById(R.id.tv_definition);

            // Set click listeners for flipping
            cardFront.setOnClickListener(v -> flipCard());
            cardBack.setOnClickListener(v -> flipCard());
        }

        public void bind(Flashcard flashcard, int position) {
            tvTerm.setText(flashcard.getTerm());
            tvDefinition.setText(flashcard.getDefinition());
            
            // Reset flip state
            isCurrentlyFlipped = isFlipped[position];
            updateCardVisibility();
        }

        private void flipCard() {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                isCurrentlyFlipped = !isCurrentlyFlipped;
                isFlipped[position] = isCurrentlyFlipped;
                
                // Create flip animation
                AnimatorSet flipOut = (AnimatorSet) AnimatorInflater.loadAnimator(
                        itemView.getContext(), R.animator.card_flip_out);
                AnimatorSet flipIn = (AnimatorSet) AnimatorInflater.loadAnimator(
                        itemView.getContext(), R.animator.card_flip_in);

                View currentCard = isCurrentlyFlipped ? cardFront : cardBack;
                View nextCard = isCurrentlyFlipped ? cardBack : cardFront;

                flipOut.setTarget(currentCard);
                flipIn.setTarget(nextCard);

                flipOut.start();
                flipIn.start();

                // Update visibility after animation starts
                flipOut.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        updateCardVisibility();
                    }
                });
            }
        }

        private void updateCardVisibility() {
            if (isCurrentlyFlipped) {
                cardFront.setVisibility(View.GONE);
                cardBack.setVisibility(View.VISIBLE);
            } else {
                cardFront.setVisibility(View.VISIBLE);
                cardBack.setVisibility(View.GONE);
            }
        }
    }

    public void resetFlipStates() {
        for (int i = 0; i < isFlipped.length; i++) {
            isFlipped[i] = false;
        }
        notifyDataSetChanged();
    }
}
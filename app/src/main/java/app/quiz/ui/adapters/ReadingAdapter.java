package app.quiz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.Reading;

public class ReadingAdapter extends RecyclerView.Adapter<ReadingAdapter.ReadingViewHolder> {

    private List<Reading> readings;
    private OnReadingClickListener listener;

    public interface OnReadingClickListener {
        void onReadingClick(Reading reading);
    }

    public ReadingAdapter(List<Reading> readings, OnReadingClickListener listener) {
        this.readings = readings;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading, parent, false);
        return new ReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        Reading reading = readings.get(position);
        holder.bind(reading);
    }

    @Override
    public int getItemCount() {
        return readings != null ? readings.size() : 0;
    }

    public void updateReadings(List<Reading> newReadings) {
        this.readings.clear();
        if (newReadings != null) {
            this.readings.addAll(newReadings);
        }
        notifyDataSetChanged();
    }

    class ReadingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDescription;

        public ReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onReadingClick(readings.get(getAdapterPosition()));
                }
            });
        }

        public void bind(Reading reading) {
            tvTitle.setText(reading.getTitle());
            if (reading.getDescription() != null && !reading.getDescription().trim().isEmpty()) {
                tvDescription.setText(reading.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }
        }
    }
}
package app.quiz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import app.quiz.R;
import app.quiz.data.models.Reading;

public class AdminReadingAdapter extends RecyclerView.Adapter<AdminReadingAdapter.AdminReadingViewHolder> {

    private List<Reading> readings;
    private OnAdminReadingClickListener listener;
    private SimpleDateFormat dateFormat;

    public interface OnAdminReadingClickListener {
        void onReadingClick(Reading reading);
        void onEditClick(Reading reading);
        void onDeleteClick(Reading reading);
    }

    public AdminReadingAdapter(List<Reading> readings, OnAdminReadingClickListener listener) {
        this.readings = readings;
        this.listener = listener;
        this.dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    @NonNull
    @Override
    public AdminReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_reading, parent, false);
        return new AdminReadingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminReadingViewHolder holder, int position) {
        Reading reading = readings.get(position);
        holder.bind(reading);
    }

    @Override
    public int getItemCount() {
        return readings.size();
    }

    public class AdminReadingViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivThumbnail;
        private TextView tvTitle;
        private TextView tvDescription;
        private TextView tvQuestionCount;
        private TextView tvCreatedDate;
        private ImageButton btnEdit;
        private ImageButton btnDelete;
        private View itemContainer;

        public AdminReadingViewHolder(@NonNull View itemView) {
            super(itemView);
            
            ivThumbnail = itemView.findViewById(R.id.iv_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvQuestionCount = itemView.findViewById(R.id.tv_question_count);
            tvCreatedDate = itemView.findViewById(R.id.tv_created_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            itemContainer = itemView.findViewById(R.id.item_container);
        }

        public void bind(Reading reading) {
            // Set title
            tvTitle.setText(reading.getTitle());

            // Set description
            if (reading.getDescription() != null && !reading.getDescription().trim().isEmpty()) {
                tvDescription.setText(reading.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Set question count
            int questionCount = reading.getQuestions() != null ? reading.getQuestions().size() : 0;
            tvQuestionCount.setText(questionCount + " question" + (questionCount != 1 ? "s" : ""));

            // Set created date
            if (reading.getCreatedAt() != null) {
                tvCreatedDate.setText("Created: " + dateFormat.format(reading.getCreatedAt()));
                tvCreatedDate.setVisibility(View.VISIBLE);
            } else {
                tvCreatedDate.setVisibility(View.GONE);
            }

            // Load thumbnail image
            if (reading.getImageUrl() != null && !reading.getImageUrl().trim().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(reading.getImageUrl())
                        .apply(new RequestOptions()
                                .transform(new RoundedCorners(16))
                                .placeholder(R.drawable.ic_image_placeholder)
                                .error(R.drawable.ic_image_error))
                        .into(ivThumbnail);
                ivThumbnail.setVisibility(View.VISIBLE);
            } else {
                ivThumbnail.setVisibility(View.GONE);
            }

            // Set click listeners
            itemContainer.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReadingClick(reading);
                }
            });

            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(reading);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(reading);
                }
            });
        }
    }

    public void updateReadings(List<Reading> newReadings) {
        this.readings = newReadings;
        notifyDataSetChanged();
    }

    public void addReadings(List<Reading> newReadings) {
        int startPosition = this.readings.size();
        this.readings.addAll(newReadings);
        notifyItemRangeInserted(startPosition, newReadings.size());
    }

    public void removeReading(Reading reading) {
        int position = readings.indexOf(reading);
        if (position != -1) {
            readings.remove(position);
            notifyItemRemoved(position);
        }
    }
}
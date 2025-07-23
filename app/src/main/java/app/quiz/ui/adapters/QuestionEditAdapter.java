package app.quiz.ui.adapters;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.ReadingQuestion;

public class QuestionEditAdapter extends RecyclerView.Adapter<QuestionEditAdapter.QuestionEditViewHolder> {

    private List<ReadingQuestion> questions;
    private OnQuestionEditListener listener;

    public interface OnQuestionEditListener {
        void onQuestionDelete(int position);
    }

    public QuestionEditAdapter(List<ReadingQuestion> questions, OnQuestionEditListener listener) {
        this.questions = questions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public QuestionEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_edit, parent, false);
        return new QuestionEditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionEditViewHolder holder, int position) {
        ReadingQuestion question = questions.get(position);
        holder.bind(question, position);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    public class QuestionEditViewHolder extends RecyclerView.ViewHolder {
        private TextView tvQuestionNumber;
        private TextView tvQuestionType;
        private TextInputEditText etQuestionText;
        private TextInputLayout tilQuestionText;
        private ImageButton btnDelete;
        
        // Single Choice components
        private View layoutSingleChoice;
        private TextInputEditText etOptionA;
        private TextInputEditText etOptionB;
        private TextInputEditText etOptionC;
        private TextInputEditText etOptionD;
        private RadioGroup rgCorrectOption;
        private RadioButton rbOptionA;
        private RadioButton rbOptionB;
        private RadioButton rbOptionC;
        private RadioButton rbOptionD;
        
        // Fill in the Blank components
        private View layoutFillInBlank;
        private TextInputEditText etAnswer;
        private TextInputLayout tilAnswer;

        public QuestionEditViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvQuestionType = itemView.findViewById(R.id.tv_question_type);
            etQuestionText = itemView.findViewById(R.id.et_question_text);
            tilQuestionText = itemView.findViewById(R.id.til_question_text);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            
            // Single Choice
            layoutSingleChoice = itemView.findViewById(R.id.layout_single_choice);
            etOptionA = itemView.findViewById(R.id.et_option_a);
            etOptionB = itemView.findViewById(R.id.et_option_b);
            etOptionC = itemView.findViewById(R.id.et_option_c);
            etOptionD = itemView.findViewById(R.id.et_option_d);
            rgCorrectOption = itemView.findViewById(R.id.rg_correct_option);
            rbOptionA = itemView.findViewById(R.id.rb_option_a);
            rbOptionB = itemView.findViewById(R.id.rb_option_b);
            rbOptionC = itemView.findViewById(R.id.rb_option_c);
            rbOptionD = itemView.findViewById(R.id.rb_option_d);
            
            // Fill in the Blank
            layoutFillInBlank = itemView.findViewById(R.id.layout_fill_in_blank);
            etAnswer = itemView.findViewById(R.id.et_answer);
            tilAnswer = itemView.findViewById(R.id.til_answer);
        }

        public void bind(ReadingQuestion question, int position) {
            // Clear previous listeners to avoid conflicts
            clearTextWatchers();
            
            // Set question number and type
            tvQuestionNumber.setText("Question " + (position + 1));
            tvQuestionType.setText(question.isSingleChoice() ? "Single Choice" : "Fill in the Blank");
            
            // Set question text
            etQuestionText.setText(question.getQuestionText());
            
            // Show/hide layouts based on question type
            if (question.isSingleChoice()) {
                layoutSingleChoice.setVisibility(View.VISIBLE);
                layoutFillInBlank.setVisibility(View.GONE);
                bindSingleChoiceQuestion(question);
            } else {
                layoutSingleChoice.setVisibility(View.GONE);
                layoutFillInBlank.setVisibility(View.VISIBLE);
                bindFillInBlankQuestion(question);
            }
            
            // Set up text watchers
            setupTextWatchers(question);
            
            // Set delete button listener
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onQuestionDelete(getAdapterPosition());
                }
            });
        }
        
        private void bindSingleChoiceQuestion(ReadingQuestion question) {
            etOptionA.setText(question.getOptionA());
            etOptionB.setText(question.getOptionB());
            etOptionC.setText(question.getOptionC());
            etOptionD.setText(question.getOptionD());
            
            // Set correct option
            rgCorrectOption.clearCheck();
            String correctOption = question.getCorrectOption();
            if ("A".equals(correctOption)) {
                rbOptionA.setChecked(true);
            } else if ("B".equals(correctOption)) {
                rbOptionB.setChecked(true);
            } else if ("C".equals(correctOption)) {
                rbOptionC.setChecked(true);
            } else if ("D".equals(correctOption)) {
                rbOptionD.setChecked(true);
            }
            
            // Set radio group listener
            rgCorrectOption.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_option_a) {
                    question.setCorrectOption("A");
                } else if (checkedId == R.id.rb_option_b) {
                    question.setCorrectOption("B");
                } else if (checkedId == R.id.rb_option_c) {
                    question.setCorrectOption("C");
                } else if (checkedId == R.id.rb_option_d) {
                    question.setCorrectOption("D");
                }
            });
        }
        
        private void bindFillInBlankQuestion(ReadingQuestion question) {
            etAnswer.setText(question.getAnswer());
        }
        
        private void clearTextWatchers() {
            // This method would clear any existing TextWatchers if needed
            // For simplicity, we'll set up new ones each time
        }
        
        private void setupTextWatchers(ReadingQuestion question) {
            // Question text watcher
            etQuestionText.addTextChangedListener(new SimpleTextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    question.setQuestionText(s.toString());
                }
            });
            
            if (question.isSingleChoice()) {
                // Option A watcher
                etOptionA.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        question.setOptionA(s.toString());
                    }
                });
                
                // Option B watcher
                etOptionB.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        question.setOptionB(s.toString());
                    }
                });
                
                // Option C watcher
                etOptionC.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        question.setOptionC(s.toString());
                    }
                });
                
                // Option D watcher
                etOptionD.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        question.setOptionD(s.toString());
                    }
                });
            } else {
                // Answer watcher for fill in the blank
                etAnswer.addTextChangedListener(new SimpleTextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        question.setAnswer(s.toString());
                    }
                });
            }
        }
    }
    
    // Helper class to simplify TextWatcher implementation
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        
        @Override
        public abstract void afterTextChanged(Editable s);
    }
}
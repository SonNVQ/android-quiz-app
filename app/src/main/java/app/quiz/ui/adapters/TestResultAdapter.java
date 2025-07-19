package app.quiz.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.quiz.R;
import app.quiz.data.models.TestQuestion;
import app.quiz.data.models.TestResult;

/**
 * TestResultAdapter - RecyclerView adapter for displaying test question results
 * Shows incorrect answers and skipped questions with details
 */
public class TestResultAdapter extends RecyclerView.Adapter<TestResultAdapter.QuestionResultViewHolder> {
    
    public static final int TYPE_INCORRECT = 1;
    public static final int TYPE_SKIPPED = 2;
    
    private List<TestResult.QuestionResult> questionResults;
    private int resultType;
    
    public TestResultAdapter(List<TestResult.QuestionResult> questionResults, int resultType) {
        this.questionResults = questionResults;
        this.resultType = resultType;
    }
    
    @NonNull
    @Override
    public QuestionResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_question_result, parent, false);
        return new QuestionResultViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull QuestionResultViewHolder holder, int position) {
        TestResult.QuestionResult result = questionResults.get(position);
        holder.bind(result, resultType, position + 1);
    }
    
    @Override
    public int getItemCount() {
        return questionResults.size();
    }
    
    static class QuestionResultViewHolder extends RecyclerView.ViewHolder {
        
        private TextView tvQuestionNumber;
        private TextView tvQuestionType;
        private TextView tvQuestionText;
        private TextView tvUserAnswer;
        private TextView tvCorrectAnswer;
        private TextView tvStatus;
        
        public QuestionResultViewHolder(@NonNull View itemView) {
            super(itemView);
            
            tvQuestionNumber = itemView.findViewById(R.id.tv_question_number);
            tvQuestionType = itemView.findViewById(R.id.tv_question_type);
            tvQuestionText = itemView.findViewById(R.id.tv_question_text);
            tvUserAnswer = itemView.findViewById(R.id.tv_user_answer);
            tvCorrectAnswer = itemView.findViewById(R.id.tv_correct_answer);
            tvStatus = itemView.findViewById(R.id.tv_status);
        }
        
        public void bind(TestResult.QuestionResult result, int resultType, int questionNumber) {
            // Question number and type
            tvQuestionNumber.setText(String.valueOf(questionNumber));
            tvQuestionType.setText(getQuestionTypeText(result.getQuestionType()));
            
            // Question text from flashcard
            if (result.getSourceFlashcard() != null) {
                String questionText = generateQuestionText(result);
                tvQuestionText.setText(questionText);
            } else {
                tvQuestionText.setText("Question text not available");
            }
            
            // Status and answers based on result type
            if (resultType == TYPE_INCORRECT) {
                tvStatus.setText("Incorrect");
                tvStatus.setTextColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_red_dark, null));
                
                tvUserAnswer.setText("Your answer: " + formatAnswer(result.getUserAnswer(), result.getQuestionType()));
                tvUserAnswer.setVisibility(View.VISIBLE);
                
                tvCorrectAnswer.setText("Correct answer: " + result.getCorrectAnswer());
                tvCorrectAnswer.setVisibility(View.VISIBLE);
                
            } else if (resultType == TYPE_SKIPPED) {
                tvStatus.setText("Skipped");
                tvStatus.setTextColor(itemView.getContext().getResources()
                        .getColor(android.R.color.holo_orange_dark, null));
                
                tvUserAnswer.setVisibility(View.GONE);
                
                tvCorrectAnswer.setText("Answer: " + result.getCorrectAnswer());
                tvCorrectAnswer.setVisibility(View.VISIBLE);
            }
        }
        
        private String getQuestionTypeText(TestQuestion.QuestionType type) {
            switch (type) {
                case MULTIPLE_CHOICE:
                    return "Multiple Choice";
                case TRUE_FALSE:
                    return "True/False";
                case FILL_IN_BLANK:
                    return "Fill in the Blank";
                default:
                    return "Unknown";
            }
        }
        
        private String generateQuestionText(TestResult.QuestionResult result) {
            if (result.getSourceFlashcard() == null) {
                return "Question not available";
            }
            
            String term = result.getSourceFlashcard().getTerm();
            String definition = result.getSourceFlashcard().getDefinition();
            
            switch (result.getQuestionType()) {
                case MULTIPLE_CHOICE:
                    return "What is the meaning of '" + term + "'?";
                case TRUE_FALSE:
                    return "True or False: '" + term + "' means '" + definition + "'";
                case FILL_IN_BLANK:
                    return "Fill in the blank: The meaning of '" + term + "' is ____";
                default:
                    return term + " - " + definition;
            }
        }
        
        private String formatAnswer(String userAnswer, TestQuestion.QuestionType type) {
            if (userAnswer == null || userAnswer.isEmpty()) {
                return "No answer";
            }
            
            switch (type) {
                case TRUE_FALSE:
                    try {
                        int answerIndex = Integer.parseInt(userAnswer);
                        return answerIndex == 0 ? "True" : "False";
                    } catch (NumberFormatException e) {
                        return userAnswer;
                    }
                case MULTIPLE_CHOICE:
                    try {
                        int answerIndex = Integer.parseInt(userAnswer);
                        return "Option " + (answerIndex + 1);
                    } catch (NumberFormatException e) {
                        return userAnswer;
                    }
                case FILL_IN_BLANK:
                default:
                    return userAnswer;
            }
        }
    }
}
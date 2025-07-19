package app.quiz.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * TestQuestion model representing a single test question
 * Supports multiple question types: multiple choice, true/false, fill-in-the-blank
 */
public class TestQuestion implements Parcelable {
    public enum QuestionType {
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        FILL_IN_BLANK
    }
    
    private String id;
    private String questionText;
    private QuestionType type;
    private List<String> options; // For multiple choice
    private int correctAnswerIndex; // For multiple choice and true/false
    private String correctAnswer; // For fill-in-the-blank
    private String hint;
    private Flashcard sourceFlashcard;
    
    // Default constructor
    public TestQuestion() {
        this.options = new ArrayList<>();
    }
    
    // Constructor for multiple choice
    public TestQuestion(String id, String questionText, List<String> options, int correctAnswerIndex, String hint, Flashcard sourceFlashcard) {
        this.id = validateId(id);
        this.questionText = validateQuestionText(questionText);
        this.type = QuestionType.MULTIPLE_CHOICE;
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
        this.correctAnswerIndex = correctAnswerIndex;
        this.hint = hint;
        this.sourceFlashcard = sourceFlashcard;
    }
    
    // Constructor for true/false
    public TestQuestion(String id, String questionText, boolean correctAnswer, String hint, Flashcard sourceFlashcard) {
        this.id = validateId(id);
        this.questionText = validateQuestionText(questionText);
        this.type = QuestionType.TRUE_FALSE;
        this.options = new ArrayList<>();
        this.options.add("True");
        this.options.add("False");
        this.correctAnswerIndex = correctAnswer ? 0 : 1;
        this.hint = hint;
        this.sourceFlashcard = sourceFlashcard;
    }
    
    // Constructor for fill-in-the-blank
    public TestQuestion(String id, String questionText, String correctAnswer, String hint, Flashcard sourceFlashcard) {
        this.id = validateId(id);
        this.questionText = validateQuestionText(questionText);
        this.type = QuestionType.FILL_IN_BLANK;
        this.options = new ArrayList<>();
        this.correctAnswer = correctAnswer;
        this.hint = hint;
        this.sourceFlashcard = sourceFlashcard;
    }
    
    // Validation methods
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Question ID cannot be null or empty");
        }
        return id.trim();
    }
    
    private String validateQuestionText(String questionText) {
        if (questionText == null || questionText.trim().isEmpty()) {
            throw new IllegalArgumentException("Question text cannot be null or empty");
        }
        return questionText.trim();
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = validateId(id);
    }
    
    public String getQuestionText() {
        return questionText;
    }
    
    public void setQuestionText(String questionText) {
        this.questionText = validateQuestionText(questionText);
    }
    
    public QuestionType getType() {
        return type;
    }
    
    public void setType(QuestionType type) {
        this.type = type;
    }
    
    public List<String> getOptions() {
        return new ArrayList<>(options);
    }
    
    public void setOptions(List<String> options) {
        this.options = options != null ? new ArrayList<>(options) : new ArrayList<>();
    }
    
    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }
    
    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
    
    public String getCorrectAnswer() {
        return correctAnswer;
    }
    
    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }
    
    public String getHint() {
        return hint;
    }
    
    public void setHint(String hint) {
        this.hint = hint;
    }
    
    public Flashcard getSourceFlashcard() {
        return sourceFlashcard;
    }
    
    public void setSourceFlashcard(Flashcard sourceFlashcard) {
        this.sourceFlashcard = sourceFlashcard;
    }
    
    // Helper methods
    public boolean isCorrectAnswer(String userAnswer) {
        if (type == QuestionType.FILL_IN_BLANK) {
            return correctAnswer != null && correctAnswer.trim().equalsIgnoreCase(userAnswer.trim());
        } else {
            try {
                int userAnswerIndex = Integer.parseInt(userAnswer);
                return userAnswerIndex == correctAnswerIndex;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }
    
    public boolean isCorrectAnswer(int selectedIndex) {
        return selectedIndex == correctAnswerIndex;
    }
    
    // Parcelable implementation
    protected TestQuestion(Parcel in) {
        id = in.readString();
        questionText = in.readString();
        type = QuestionType.valueOf(in.readString());
        options = in.createStringArrayList();
        correctAnswerIndex = in.readInt();
        correctAnswer = in.readString();
        hint = in.readString();
        sourceFlashcard = in.readParcelable(Flashcard.class.getClassLoader());
    }
    
    public static final Creator<TestQuestion> CREATOR = new Creator<TestQuestion>() {
        @Override
        public TestQuestion createFromParcel(Parcel in) {
            return new TestQuestion(in);
        }
        
        @Override
        public TestQuestion[] newArray(int size) {
            return new TestQuestion[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(questionText);
        dest.writeString(type.name());
        dest.writeStringList(options);
        dest.writeInt(correctAnswerIndex);
        dest.writeString(correctAnswer);
        dest.writeString(hint);
        dest.writeParcelable(sourceFlashcard, flags);
    }
    
    @Override
    public String toString() {
        return "TestQuestion{" +
                "id='" + id + '\'' +
                ", questionText='" + questionText + '\'' +
                ", type=" + type +
                ", options=" + options +
                ", correctAnswerIndex=" + correctAnswerIndex +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", hint='" + hint + '\'' +
                '}';
    }
}
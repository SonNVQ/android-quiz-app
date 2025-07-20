package app.quiz.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ReadingQuestion implements Parcelable {
    private String id;
    private String questionText;
    private int questionType; // 1 = SingleChoice, 2 = FillInTheBlank
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctOption; // "A", "B", "C", "D" for single choice
    private String answer; // For fill in the blank questions

    // Question types
    public static final int TYPE_SINGLE_CHOICE = 1;
    public static final int TYPE_FILL_IN_BLANK = 2;

    public ReadingQuestion() {}

    public ReadingQuestion(String id, String questionText, int questionType) {
        this.id = id;
        this.questionText = questionText;
        this.questionType = questionType;
    }

    protected ReadingQuestion(Parcel in) {
        id = in.readString();
        questionText = in.readString();
        questionType = in.readInt();
        optionA = in.readString();
        optionB = in.readString();
        optionC = in.readString();
        optionD = in.readString();
        correctOption = in.readString();
        answer = in.readString();
    }

    public static final Creator<ReadingQuestion> CREATOR = new Creator<ReadingQuestion>() {
        @Override
        public ReadingQuestion createFromParcel(Parcel in) {
            return new ReadingQuestion(in);
        }

        @Override
        public ReadingQuestion[] newArray(int size) {
            return new ReadingQuestion[size];
        }
    };

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public int getQuestionType() { return questionType; }
    public void setQuestionType(int questionType) { this.questionType = questionType; }

    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }

    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }

    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }

    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }

    public String getCorrectOption() { return correctOption; }
    public void setCorrectOption(String correctOption) { this.correctOption = correctOption; }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    // Helper methods
    public boolean isSingleChoice() {
        return questionType == TYPE_SINGLE_CHOICE;
    }

    public boolean isFillInBlank() {
        return questionType == TYPE_FILL_IN_BLANK;
    }

    public List<String> getOptions() {
        List<String> options = new ArrayList<>();
        if (isSingleChoice()) {
            if (optionA != null) options.add(optionA);
            if (optionB != null) options.add(optionB);
            if (optionC != null) options.add(optionC);
            if (optionD != null) options.add(optionD);
        }
        return options;
    }

    public int getCorrectAnswerIndex() {
        if (isSingleChoice() && correctOption != null) {
            switch (correctOption.toUpperCase()) {
                case "A": return 0;
                case "B": return 1;
                case "C": return 2;
                case "D": return 3;
                default: return -1;
            }
        }
        return -1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(questionText);
        dest.writeInt(questionType);
        dest.writeString(optionA);
        dest.writeString(optionB);
        dest.writeString(optionC);
        dest.writeString(optionD);
        dest.writeString(correctOption);
        dest.writeString(answer);
    }
}
package app.quiz.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * TestResult model representing the results of a flashcard test
 * Tracks score, performance, and areas for improvement
 */
public class TestResult implements Parcelable {
    private String testId;
    private String flashcardGroupId;
    private String flashcardGroupName;
    private Date testDate;
    private int totalQuestions;
    private int correctAnswers;
    private int skippedQuestions;
    private long testDurationMs;
    private List<QuestionResult> questionResults;
    private List<Flashcard> incorrectFlashcards;
    private List<Flashcard> skippedFlashcards;
    
    // Inner class for individual question results
    public static class QuestionResult implements Parcelable {
        private String questionId;
        private TestQuestion.QuestionType questionType;
        private String userAnswer;
        private String correctAnswer;
        private boolean isCorrect;
        private boolean wasSkipped;
        private long timeSpentMs;
        private Flashcard sourceFlashcard;
        
        public QuestionResult() {}
        
        public QuestionResult(String questionId, TestQuestion.QuestionType questionType, 
                            String userAnswer, String correctAnswer, boolean isCorrect, 
                            boolean wasSkipped, long timeSpentMs, Flashcard sourceFlashcard) {
            this.questionId = questionId;
            this.questionType = questionType;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
            this.isCorrect = isCorrect;
            this.wasSkipped = wasSkipped;
            this.timeSpentMs = timeSpentMs;
            this.sourceFlashcard = sourceFlashcard;
        }
        
        // Getters and setters
        public String getQuestionId() { return questionId; }
        public void setQuestionId(String questionId) { this.questionId = questionId; }
        
        public TestQuestion.QuestionType getQuestionType() { return questionType; }
        public void setQuestionType(TestQuestion.QuestionType questionType) { this.questionType = questionType; }
        
        public String getUserAnswer() { return userAnswer; }
        public void setUserAnswer(String userAnswer) { this.userAnswer = userAnswer; }
        
        public String getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
        
        public boolean isCorrect() { return isCorrect; }
        public void setCorrect(boolean correct) { isCorrect = correct; }
        
        public boolean wasSkipped() { return wasSkipped; }
        public void setSkipped(boolean skipped) { wasSkipped = skipped; }
        
        public long getTimeSpentMs() { return timeSpentMs; }
        public void setTimeSpentMs(long timeSpentMs) { this.timeSpentMs = timeSpentMs; }
        
        public Flashcard getSourceFlashcard() { return sourceFlashcard; }
        public void setSourceFlashcard(Flashcard sourceFlashcard) { this.sourceFlashcard = sourceFlashcard; }
        
        // Parcelable implementation
        protected QuestionResult(Parcel in) {
            questionId = in.readString();
            questionType = TestQuestion.QuestionType.valueOf(in.readString());
            userAnswer = in.readString();
            correctAnswer = in.readString();
            isCorrect = in.readByte() != 0;
            wasSkipped = in.readByte() != 0;
            timeSpentMs = in.readLong();
            sourceFlashcard = in.readParcelable(Flashcard.class.getClassLoader());
        }
        
        public static final Creator<QuestionResult> CREATOR = new Creator<QuestionResult>() {
            @Override
            public QuestionResult createFromParcel(Parcel in) {
                return new QuestionResult(in);
            }
            
            @Override
            public QuestionResult[] newArray(int size) {
                return new QuestionResult[size];
            }
        };
        
        @Override
        public int describeContents() {
            return 0;
        }
        
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(questionId);
            dest.writeString(questionType.name());
            dest.writeString(userAnswer);
            dest.writeString(correctAnswer);
            dest.writeByte((byte) (isCorrect ? 1 : 0));
            dest.writeByte((byte) (wasSkipped ? 1 : 0));
            dest.writeLong(timeSpentMs);
            dest.writeParcelable(sourceFlashcard, flags);
        }
    }
    
    // Default constructor
    public TestResult() {
        this.testDate = new Date();
        this.questionResults = new ArrayList<>();
        this.incorrectFlashcards = new ArrayList<>();
        this.skippedFlashcards = new ArrayList<>();
    }
    
    // Constructor
    public TestResult(String testId, String flashcardGroupId, String flashcardGroupName) {
        this();
        this.testId = testId;
        this.flashcardGroupId = flashcardGroupId;
        this.flashcardGroupName = flashcardGroupName;
    }
    
    // Getters and setters
    public String getTestId() {
        return testId;
    }
    
    public void setTestId(String testId) {
        this.testId = testId;
    }
    
    public String getFlashcardGroupId() {
        return flashcardGroupId;
    }
    
    public void setFlashcardGroupId(String flashcardGroupId) {
        this.flashcardGroupId = flashcardGroupId;
    }
    
    public String getFlashcardGroupName() {
        return flashcardGroupName;
    }
    
    public void setFlashcardGroupName(String flashcardGroupName) {
        this.flashcardGroupName = flashcardGroupName;
    }
    
    public Date getTestDate() {
        return testDate;
    }
    
    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }
    
    public int getTotalQuestions() {
        return totalQuestions;
    }
    
    public void setTotalQuestions(int totalQuestions) {
        this.totalQuestions = totalQuestions;
    }
    
    public int getCorrectAnswers() {
        return correctAnswers;
    }
    
    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }
    
    public int getSkippedQuestions() {
        return skippedQuestions;
    }
    
    public void setSkippedQuestions(int skippedQuestions) {
        this.skippedQuestions = skippedQuestions;
    }
    
    public long getTestDurationMs() {
        return testDurationMs;
    }
    
    public void setTestDurationMs(long testDurationMs) {
        this.testDurationMs = testDurationMs;
    }
    
    public List<QuestionResult> getQuestionResults() {
        return new ArrayList<>(questionResults);
    }
    
    public void setQuestionResults(List<QuestionResult> questionResults) {
        this.questionResults = questionResults != null ? new ArrayList<>(questionResults) : new ArrayList<>();
    }
    
    public List<Flashcard> getIncorrectFlashcards() {
        return new ArrayList<>(incorrectFlashcards);
    }
    
    public void setIncorrectFlashcards(List<Flashcard> incorrectFlashcards) {
        this.incorrectFlashcards = incorrectFlashcards != null ? new ArrayList<>(incorrectFlashcards) : new ArrayList<>();
    }
    
    public List<Flashcard> getSkippedFlashcards() {
        return new ArrayList<>(skippedFlashcards);
    }
    
    public void setSkippedFlashcards(List<Flashcard> skippedFlashcards) {
        this.skippedFlashcards = skippedFlashcards != null ? new ArrayList<>(skippedFlashcards) : new ArrayList<>();
    }
    
    // Helper methods
    public void addQuestionResult(QuestionResult result) {
        if (result != null) {
            questionResults.add(result);
            if (result.isCorrect()) {
                correctAnswers++;
            } else if (!result.wasSkipped()) {
                incorrectFlashcards.add(result.getSourceFlashcard());
            }
            if (result.wasSkipped()) {
                skippedQuestions++;
                skippedFlashcards.add(result.getSourceFlashcard());
            }
        }
    }
    
    public double getScorePercentage() {
        if (totalQuestions == 0) return 0.0;
        return (double) correctAnswers / totalQuestions * 100.0;
    }
    
    public int getIncorrectAnswers() {
        return totalQuestions - correctAnswers - skippedQuestions;
    }
    
    public String getFormattedDuration() {
        long seconds = testDurationMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    public String getPerformanceLevel() {
        double percentage = getScorePercentage();
        if (percentage >= 90) return "Excellent";
        else if (percentage >= 80) return "Good";
        else if (percentage >= 70) return "Fair";
        else if (percentage >= 60) return "Needs Improvement";
        else return "Poor";
    }
    
    // Parcelable implementation
    protected TestResult(Parcel in) {
        testId = in.readString();
        flashcardGroupId = in.readString();
        flashcardGroupName = in.readString();
        testDate = new Date(in.readLong());
        totalQuestions = in.readInt();
        correctAnswers = in.readInt();
        skippedQuestions = in.readInt();
        testDurationMs = in.readLong();
        questionResults = in.createTypedArrayList(QuestionResult.CREATOR);
        incorrectFlashcards = in.createTypedArrayList(Flashcard.CREATOR);
        skippedFlashcards = in.createTypedArrayList(Flashcard.CREATOR);
    }
    
    public static final Creator<TestResult> CREATOR = new Creator<TestResult>() {
        @Override
        public TestResult createFromParcel(Parcel in) {
            return new TestResult(in);
        }
        
        @Override
        public TestResult[] newArray(int size) {
            return new TestResult[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(testId);
        dest.writeString(flashcardGroupId);
        dest.writeString(flashcardGroupName);
        dest.writeLong(testDate.getTime());
        dest.writeInt(totalQuestions);
        dest.writeInt(correctAnswers);
        dest.writeInt(skippedQuestions);
        dest.writeLong(testDurationMs);
        dest.writeTypedList(questionResults);
        dest.writeTypedList(incorrectFlashcards);
        dest.writeTypedList(skippedFlashcards);
    }
    
    @Override
    public String toString() {
        return "TestResult{" +
                "testId='" + testId + '\'' +
                ", flashcardGroupName='" + flashcardGroupName + '\'' +
                ", correctAnswers=" + correctAnswers +
                ", totalQuestions=" + totalQuestions +
                ", scorePercentage=" + getScorePercentage() +
                '}';
    }
}
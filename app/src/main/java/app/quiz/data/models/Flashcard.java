package app.quiz.data.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Flashcard model representing a single flashcard with term and definition
 * Implements Parcelable for data transfer between activities
 */
public class Flashcard implements Parcelable {
    private String term;
    private String definition;
    
    // Default constructor
    public Flashcard() {}
    
    // Constructor with validation
    public Flashcard(String term, String definition) {
        this.term = validateTerm(term);
        this.definition = validateDefinition(definition);
    }
    
    // Validation methods
    private String validateTerm(String term) {
        if (term == null || term.trim().isEmpty()) {
            throw new IllegalArgumentException("Flashcard term cannot be null or empty");
        }
        return term.trim();
    }
    
    private String validateDefinition(String definition) {
        if (definition == null || definition.trim().isEmpty()) {
            throw new IllegalArgumentException("Flashcard definition cannot be null or empty");
        }
        return definition.trim();
    }
    
    // Getters and setters
    public String getTerm() {
        return term;
    }
    
    public void setTerm(String term) {
        this.term = validateTerm(term);
    }
    
    public String getDefinition() {
        return definition;
    }
    
    public void setDefinition(String definition) {
        this.definition = validateDefinition(definition);
    }
    
    // Parcelable implementation
    protected Flashcard(Parcel in) {
        term = in.readString();
        definition = in.readString();
    }
    
    public static final Creator<Flashcard> CREATOR = new Creator<Flashcard>() {
        @Override
        public Flashcard createFromParcel(Parcel in) {
            return new Flashcard(in);
        }
        
        @Override
        public Flashcard[] newArray(int size) {
            return new Flashcard[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(term);
        dest.writeString(definition);
    }
    
    @Override
    public String toString() {
        return "Flashcard{" +
                "term='" + term + '\'' +
                ", definition='" + definition + '\'' +
                '}';
    }
}
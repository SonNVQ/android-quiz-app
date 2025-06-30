package app.quiz.data.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * FlashcardGroup model representing a group/set of flashcards
 * Implements Parcelable for data transfer between activities
 */
public class FlashcardGroup implements Parcelable {
    private String id;
    private String name;
    private String description;
    private boolean isPublic;
    private List<Flashcard> flashcards;
    
    // Default constructor
    public FlashcardGroup() {
        this.flashcards = new ArrayList<>();
    }
    
    // Constructor with validation
    public FlashcardGroup(String id, String name, String description, boolean isPublic) {
        this.id = validateId(id);
        this.name = validateName(name);
        this.description = description != null ? description.trim() : "";
        this.isPublic = isPublic;
        this.flashcards = new ArrayList<>();
    }
    
    // Constructor with flashcards
    public FlashcardGroup(String id, String name, String description, boolean isPublic, List<Flashcard> flashcards) {
        this.id = validateId(id);
        this.name = validateName(name);
        this.description = description != null ? description.trim() : "";
        this.isPublic = isPublic;
        this.flashcards = flashcards != null ? new ArrayList<>(flashcards) : new ArrayList<>();
    }
    
    // Validation methods
    private String validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("FlashcardGroup ID cannot be null or empty");
        }
        return id.trim();
    }
    
    private String validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("FlashcardGroup name cannot be null or empty");
        }
        return name.trim();
    }
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = validateId(id);
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = validateName(name);
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }
    
    public boolean isPublic() {
        return isPublic;
    }
    
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    public List<Flashcard> getFlashcards() {
        return new ArrayList<>(flashcards);
    }
    
    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards != null ? new ArrayList<>(flashcards) : new ArrayList<>();
    }
    
    public void addFlashcard(Flashcard flashcard) {
        if (flashcard != null) {
            this.flashcards.add(flashcard);
        }
    }
    
    public int getFlashcardCount() {
        return flashcards.size();
    }
    
    // Parcelable implementation
    protected FlashcardGroup(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        isPublic = in.readByte() != 0;
        flashcards = in.createTypedArrayList(Flashcard.CREATOR);
        if (flashcards == null) {
            flashcards = new ArrayList<>();
        }
    }
    
    public static final Creator<FlashcardGroup> CREATOR = new Creator<FlashcardGroup>() {
        @Override
        public FlashcardGroup createFromParcel(Parcel in) {
            return new FlashcardGroup(in);
        }
        
        @Override
        public FlashcardGroup[] newArray(int size) {
            return new FlashcardGroup[size];
        }
    };
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeByte((byte) (isPublic ? 1 : 0));
        dest.writeTypedList(flashcards);
    }
    
    @Override
    public String toString() {
        return "FlashcardGroup{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", isPublic=" + isPublic +
                ", flashcardCount=" + flashcards.size() +
                '}';
    }
}
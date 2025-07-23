package app.quiz.data.models;

import java.util.List;

public class ReadingUpdateDto {
    private String id;
    private String title;
    private String content;
    private String imageUrl;
    private String description;
    private List<ReadingQuestion> questions;

    public ReadingUpdateDto() {}

    public ReadingUpdateDto(String id, String title, String content) {
        this.id = id;
        this.title = title;
        this.content = content;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<ReadingQuestion> getQuestions() { return questions; }
    public void setQuestions(List<ReadingQuestion> questions) { this.questions = questions; }

    // Validation method
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               title != null && !title.trim().isEmpty() &&
               content != null && !content.trim().isEmpty();
    }
}
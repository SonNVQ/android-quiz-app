package app.quiz.data.models;

import java.util.List;

public class ReadingCreateDTO {
    private String title;
    private String content;
    private String imageUrl;
    private String description;
    private List<ReadingQuestion> questions;

    public ReadingCreateDTO() {}

    public ReadingCreateDTO(String title, String content) {
        this.title = title;
        this.content = content;
    }

    // Getters and setters
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
        return title != null && !title.trim().isEmpty() &&
               content != null && !content.trim().isEmpty();
    }
}
package app.quiz.utils;

import app.quiz.data.models.Flashcard;
import app.quiz.data.models.TestQuestion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Utility class for generating test questions from flashcards
 * Supports multiple question types: multiple choice, true/false, fill-in-the-blank
 */
public class TestGenerator {
    private static final Random random = new Random();
    private static final int MIN_FLASHCARDS_FOR_MULTIPLE_CHOICE = 4;
    private static final int MULTIPLE_CHOICE_OPTIONS_COUNT = 4;
    
    /**
     * Generate a mixed test with different question types from flashcards
     * @param flashcards List of flashcards to generate questions from
     * @param questionCount Number of questions to generate
     * @return List of test questions
     */
    public static List<TestQuestion> generateMixedTest(List<Flashcard> flashcards, int questionCount) {
        if (flashcards == null || flashcards.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<TestQuestion> questions = new ArrayList<>();
        List<Flashcard> shuffledFlashcards = new ArrayList<>(flashcards);
        Collections.shuffle(shuffledFlashcards);
        
        int questionsPerType = questionCount / 3;
        int remainingQuestions = questionCount % 3;
        
        // Generate fill-in-the-blank questions
        int fillInBlankCount = questionsPerType + (remainingQuestions > 0 ? 1 : 0);
        questions.addAll(generateFillInBlankQuestions(shuffledFlashcards, fillInBlankCount));
        
        // Generate true/false questions
        int trueFalseCount = questionsPerType + (remainingQuestions > 1 ? 1 : 0);
        questions.addAll(generateTrueFalseQuestions(shuffledFlashcards, trueFalseCount));
        
        // Generate multiple choice questions (if enough flashcards)
        int multipleChoiceCount = questionsPerType;
        if (flashcards.size() >= MIN_FLASHCARDS_FOR_MULTIPLE_CHOICE) {
            questions.addAll(generateMultipleChoiceQuestions(shuffledFlashcards, multipleChoiceCount));
        } else {
            // If not enough flashcards for multiple choice, generate more fill-in-the-blank
            questions.addAll(generateFillInBlankQuestions(shuffledFlashcards, multipleChoiceCount));
        }
        
        // Shuffle the final question list
        Collections.shuffle(questions);
        
        // Trim to exact count if needed
        if (questions.size() > questionCount) {
            questions = questions.subList(0, questionCount);
        }
        
        return questions;
    }
    
    /**
     * Generate fill-in-the-blank questions
     */
    public static List<TestQuestion> generateFillInBlankQuestions(List<Flashcard> flashcards, int count) {
        List<TestQuestion> questions = new ArrayList<>();
        
        for (int i = 0; i < Math.min(count, flashcards.size()); i++) {
            Flashcard flashcard = flashcards.get(i);
            String questionId = UUID.randomUUID().toString();
            
            // Create question text by replacing the term with blank
            String questionText = "Fill in the blank: " + 
                flashcard.getDefinition().replace(flashcard.getTerm(), "____");
            
            // If the term is not in the definition, create a different format
            if (!flashcard.getDefinition().toLowerCase().contains(flashcard.getTerm().toLowerCase())) {
                questionText = "What term matches this definition: " + flashcard.getDefinition() + "?";
            }
            
            String hint = "Definition: " + flashcard.getDefinition();
            
            TestQuestion question = new TestQuestion(
                questionId,
                questionText,
                flashcard.getTerm(),
                hint,
                flashcard
            );
            
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * Generate true/false questions
     */
    public static List<TestQuestion> generateTrueFalseQuestions(List<Flashcard> flashcards, int count) {
        List<TestQuestion> questions = new ArrayList<>();
        
        for (int i = 0; i < Math.min(count, flashcards.size()); i++) {
            Flashcard flashcard = flashcards.get(i);
            String questionId = UUID.randomUUID().toString();
            
            boolean isCorrectStatement = random.nextBoolean();
            String questionText;
            String hint = "Think about the definition: " + flashcard.getDefinition();
            
            if (isCorrectStatement) {
                // Create a true statement
                questionText = "True or False: The term '" + flashcard.getTerm() + 
                    "' means '" + flashcard.getDefinition() + "'.";
            } else {
                // Create a false statement by using a wrong definition
                String wrongDefinition = getRandomWrongDefinition(flashcard, flashcards);
                questionText = "True or False: The term '" + flashcard.getTerm() + 
                    "' means '" + wrongDefinition + "'.";
            }
            
            TestQuestion question = new TestQuestion(
                questionId,
                questionText,
                isCorrectStatement,
                hint,
                flashcard
            );
            
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * Generate multiple choice questions
     */
    public static List<TestQuestion> generateMultipleChoiceQuestions(List<Flashcard> flashcards, int count) {
        List<TestQuestion> questions = new ArrayList<>();
        
        if (flashcards.size() < MIN_FLASHCARDS_FOR_MULTIPLE_CHOICE) {
            return questions; // Not enough flashcards for multiple choice
        }
        
        for (int i = 0; i < Math.min(count, flashcards.size()); i++) {
            Flashcard correctFlashcard = flashcards.get(i);
            String questionId = UUID.randomUUID().toString();
            
            // Randomly choose question format
            boolean askForDefinition = random.nextBoolean();
            String questionText;
            List<String> options = new ArrayList<>();
            int correctAnswerIndex;
            
            if (askForDefinition) {
                // Ask for definition given term
                questionText = "What is the definition of '" + correctFlashcard.getTerm() + "'?";
                options.add(correctFlashcard.getDefinition());
                
                // Add wrong definitions
                List<String> wrongDefinitions = getRandomWrongDefinitions(correctFlashcard, flashcards, MULTIPLE_CHOICE_OPTIONS_COUNT - 1);
                options.addAll(wrongDefinitions);
            } else {
                // Ask for term given definition
                questionText = "Which term matches this definition: '" + correctFlashcard.getDefinition() + "'?";
                options.add(correctFlashcard.getTerm());
                
                // Add wrong terms
                List<String> wrongTerms = getRandomWrongTerms(correctFlashcard, flashcards, MULTIPLE_CHOICE_OPTIONS_COUNT - 1);
                options.addAll(wrongTerms);
            }
            
            // Shuffle options and find correct answer index
            correctAnswerIndex = 0; // Correct answer is currently at index 0
            Collections.shuffle(options);
            
            // Find where the correct answer ended up after shuffling
            String correctAnswer = askForDefinition ? correctFlashcard.getDefinition() : correctFlashcard.getTerm();
            correctAnswerIndex = options.indexOf(correctAnswer);
            
            String hint = "Think about: " + (askForDefinition ? correctFlashcard.getTerm() : correctFlashcard.getDefinition());
            
            TestQuestion question = new TestQuestion(
                questionId,
                questionText,
                options,
                correctAnswerIndex,
                hint,
                correctFlashcard
            );
            
            questions.add(question);
        }
        
        return questions;
    }
    
    /**
     * Get a random wrong definition for true/false questions
     */
    private static String getRandomWrongDefinition(Flashcard correctFlashcard, List<Flashcard> allFlashcards) {
        List<Flashcard> otherFlashcards = new ArrayList<>();
        for (Flashcard flashcard : allFlashcards) {
            if (!flashcard.getTerm().equals(correctFlashcard.getTerm())) {
                otherFlashcards.add(flashcard);
            }
        }
        
        if (otherFlashcards.isEmpty()) {
            return "This is not the correct definition";
        }
        
        return otherFlashcards.get(random.nextInt(otherFlashcards.size())).getDefinition();
    }
    
    /**
     * Get random wrong definitions for multiple choice questions
     */
    private static List<String> getRandomWrongDefinitions(Flashcard correctFlashcard, List<Flashcard> allFlashcards, int count) {
        List<String> wrongDefinitions = new ArrayList<>();
        List<Flashcard> otherFlashcards = new ArrayList<>();
        
        for (Flashcard flashcard : allFlashcards) {
            if (!flashcard.getTerm().equals(correctFlashcard.getTerm())) {
                otherFlashcards.add(flashcard);
            }
        }
        
        Collections.shuffle(otherFlashcards);
        
        for (int i = 0; i < Math.min(count, otherFlashcards.size()); i++) {
            wrongDefinitions.add(otherFlashcards.get(i).getDefinition());
        }
        
        // Fill remaining slots with generic wrong answers if needed
        while (wrongDefinitions.size() < count) {
            wrongDefinitions.add("This is not the correct definition " + (wrongDefinitions.size() + 1));
        }
        
        return wrongDefinitions;
    }
    
    /**
     * Get random wrong terms for multiple choice questions
     */
    private static List<String> getRandomWrongTerms(Flashcard correctFlashcard, List<Flashcard> allFlashcards, int count) {
        List<String> wrongTerms = new ArrayList<>();
        List<Flashcard> otherFlashcards = new ArrayList<>();
        
        for (Flashcard flashcard : allFlashcards) {
            if (!flashcard.getTerm().equals(correctFlashcard.getTerm())) {
                otherFlashcards.add(flashcard);
            }
        }
        
        Collections.shuffle(otherFlashcards);
        
        for (int i = 0; i < Math.min(count, otherFlashcards.size()); i++) {
            wrongTerms.add(otherFlashcards.get(i).getTerm());
        }
        
        // Fill remaining slots with generic wrong answers if needed
        while (wrongTerms.size() < count) {
            wrongTerms.add("Wrong term " + (wrongTerms.size() + 1));
        }
        
        return wrongTerms;
    }
    
    /**
     * Validate if flashcards are suitable for test generation
     */
    public static boolean canGenerateTest(List<Flashcard> flashcards, int minQuestions) {
        return flashcards != null && flashcards.size() >= minQuestions;
    }
    
    /**
     * Get recommended question count based on flashcard count
     */
    public static int getRecommendedQuestionCount(int flashcardCount) {
        if (flashcardCount < 5) return flashcardCount;
        if (flashcardCount < 10) return Math.min(8, flashcardCount);
        if (flashcardCount < 20) return Math.min(12, flashcardCount);
        return Math.min(15, flashcardCount);
    }
}
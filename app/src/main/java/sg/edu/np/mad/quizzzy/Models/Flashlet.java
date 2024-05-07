package sg.edu.np.mad.quizzzy.Models;

public class Flashlet {
    String id; // Unique UUID
    String title;
    String description;
    String[] creatorId;
    String classId;
    Flashcard[] flashcards;

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatorID(String[] creatorId) { this.creatorId = creatorId; }
    public void setClassId(String classId) { this.classId = classId; }
    public void setFlashcards(Flashcard[] flashcards) { this.flashcards = flashcards; }

    // Getters
    public String getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getDescription() { return this.description; }
    public String[] getCreatorID() { return this.creatorId; }
    public String getClassId() { return this.classId; }
    public Flashcard[] getFlashcards() { return this.flashcards; }

    // Constructor
    public Flashlet(String id, String title, String description, String[] creatorId, String classId, Flashcard[] flashcards) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
        this.classId = classId;
        this.flashcards = flashcards;
    }
}
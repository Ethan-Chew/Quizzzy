package sg.edu.np.mad.quizzzy.Models;

import androidx.appcompat.app.AlertDialog;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Flashlet {
    String id; // Unique UUID
    String title;
    String description; // Description
    String creatorId;
    String classId;
    ArrayList<Flashcard> flashcards;
    Boolean isPublic;
    long lastUpdatedUnix; // Stored in s

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatorID(String creatorId) { this.creatorId = creatorId; }
    public void setClassId(String classId) { this.classId = classId; }
    public void setFlashcards(ArrayList<Flashcard> flashcards) { this.flashcards = flashcards; }
    public void setLastUpdatedUnix(long lastUpdatedUnix) { this.lastUpdatedUnix = lastUpdatedUnix; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }

    // Getters
    public String getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getDescription() { return this.description; }
    public String getCreatorID() { return this.creatorId; }
    public String getClassId() { return this.classId; }
    public ArrayList<Flashcard> getFlashcards() { return this.flashcards; }
    public Boolean getIsPublic() { return this.isPublic; }
    public long getLastUpdatedUnix() { return this.lastUpdatedUnix; }

    // Methods
    public void addFlashcard(Flashcard flashcard) {
        flashcards.add(flashcard);
    }

    // Constructor
    public Flashlet(String id, String title, String description, String creatorId, String classId, ArrayList<Flashcard> flashcards, long lastUpdatedUnix, Boolean isPublic) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
        this.classId = classId;
        this.flashcards = flashcards;
        this.lastUpdatedUnix = lastUpdatedUnix;
        this.isPublic = isPublic;
    }
}
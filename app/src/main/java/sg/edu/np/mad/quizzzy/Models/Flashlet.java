package sg.edu.np.mad.quizzzy.Models;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Flashlet {
    String id; // Unique UUID
    String title;
    String description;
    ArrayList<String> creatorId;
    String classId;
    ArrayList<Flashcard> flashcards;
    long lastUpdatedUnix; // Stored in s

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setCreatorID(ArrayList<String> creatorId) { this.creatorId = creatorId; }
    public void setClassId(String classId) { this.classId = classId; }
    public void setFlashcards(ArrayList<Flashcard> flashcards) { this.flashcards = flashcards; }
    public void setLastUpdatedUnix(long lastUpdatedUnix) { this.lastUpdatedUnix = lastUpdatedUnix; }

    // Getters
    public String getId() { return this.id; }
    public String getTitle() { return this.title; }
    public String getDescription() { return this.description; }
    public ArrayList<String> getCreatorID() { return this.creatorId; }
    public String getClassId() { return this.classId; }
    public ArrayList<Flashcard> getFlashcards() { return this.flashcards; }
    public long getLastUpdatedUnix() { return this.lastUpdatedUnix; }

    // Constructor
    public Flashlet(String id, String title, String description, ArrayList<String> creatorId, String classId, ArrayList<Flashcard> flashcards, long lastUpdatedUnix) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.creatorId = creatorId;
        this.classId = classId;
        this.flashcards = flashcards;
        this.lastUpdatedUnix = lastUpdatedUnix;
    }
}
package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

// FlashletWithInsensitive is used to decode the Firebase Object sent back from Firebase.
public class FlashletWithInsensitive extends Flashlet {
    String insensitiveTitle;

    // Getters
    public String getInsensitiveTitle() {
        return insensitiveTitle;
    }

    // Setters
    public void setInsensitiveTitle(String insensitiveTitle) {
        this.insensitiveTitle = insensitiveTitle;
    }

    // Constructor
    public FlashletWithInsensitive(String id, String title, String description, ArrayList<String> creatorID, String classId, ArrayList<Flashcard> flashcards, long lastUpdatedUnix, Boolean isPublic, String insensitiveTitle) {
        super(id, title, description, creatorID, classId, flashcards, lastUpdatedUnix, isPublic);

        this.insensitiveTitle = insensitiveTitle;
    }

    public FlashletWithInsensitive(Flashlet flashlet, String insensitiveTitle) {
        super(flashlet.id, flashlet.title, flashlet.description, flashlet.creatorID, flashlet.classId, flashlet.flashcards, flashlet.lastUpdatedUnix, flashlet.isPublic);

        this.insensitiveTitle = insensitiveTitle;
    }
}

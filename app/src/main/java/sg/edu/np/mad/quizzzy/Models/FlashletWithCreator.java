package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

public class FlashletWithCreator extends Flashlet {
    private User creator;

    public User getCreator() { return this.creator; }
    public void setCreator(User creator) { this.creator = creator; }

    public FlashletWithCreator(Flashlet flashlet, User creator) {
        super(flashlet.id, flashlet.title, flashlet.description, flashlet.creatorID, flashlet.classId, flashlet.flashcards, flashlet.lastUpdatedUnix, flashlet.isPublic);

        this.creator = creator;
    }
}

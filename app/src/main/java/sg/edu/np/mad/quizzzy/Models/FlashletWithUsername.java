package sg.edu.np.mad.quizzzy.Models;

/**
 * <b>FlashletWithUsername</b> extends the Flashlet Owner's Username and Id to the original Flashlet class
 * Used in the SearchActivity to make associating a Flashlet with it's Owner easier
 * */
public class FlashletWithUsername extends FlashletWithInsensitive {
    private String ownerUsername;
    private String ownerId;

    // Getters
    public String getOwnerUsername() {
        return this.ownerUsername;
    }
    public String getOwnerId() {
        return this.ownerId;
    }

    // Setters
    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    // Constructor
    public FlashletWithUsername(FlashletWithInsensitive flashlet, String ownerUsername, String ownerId) {
        super(flashlet.id, flashlet.title, flashlet.description, flashlet.creatorID, flashlet.classId, flashlet.flashcards, flashlet.lastUpdatedUnix, flashlet.isPublic, flashlet.insensitiveTitle);

        this.ownerUsername = ownerUsername;
        this.ownerId = ownerId;
    }
}

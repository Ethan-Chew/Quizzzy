package sg.edu.np.mad.quizzzy.Models;

public class FlashletWithUsername {
    private Flashlet flashlet;
    private String ownerUsername;
    private String ownerId;

    // Getters
    public Flashlet getFlashlet() {
        return this.flashlet;
    }
    public String getOwnerUsername() {
        return this.ownerUsername;
    }
    public String getOwnerId() {
        return this.ownerId;
    }

    // Setters
    public void setFlashlet(Flashlet flashlet) {
        this.flashlet = flashlet;
    }
    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public FlashletWithUsername(Flashlet flashlet, String ownerUsername, String ownerId) {
        this.flashlet = flashlet;
        this.ownerUsername = ownerUsername;
        this.ownerId = ownerId;
    }
}

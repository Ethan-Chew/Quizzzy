package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

/**
 * <b>SearchResult</b> stores the query results after querying the Firebase Cloud Firestore database with the user's search term.<br>
 * Allows for the Search Result to be passed around between files easily
 * */
public class SearchResult {
    private ArrayList<FlashletWithUsername> flashlets;
    private ArrayList<User> users;

    // Getters
    public ArrayList<FlashletWithUsername> getFlashlets() {
        return this.flashlets;
    }
    public ArrayList<User> getUsers() {
        return this.users;
    }

    // Setters
    public void setFlashlets(ArrayList<FlashletWithUsername> flashlets) {
        this.flashlets = flashlets;
    }
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public SearchResult(ArrayList<FlashletWithUsername> flashlets, ArrayList<User> users) {
        this.flashlets = flashlets;
        this.users = users;
    }

    public SearchResult() {
        this.flashlets = new ArrayList<FlashletWithUsername>();
        this.users = new ArrayList<User>();
    }
}

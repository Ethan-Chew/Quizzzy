package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

/**
 * <b>SearchResult</b> stores the query results after querying the Firebase Cloud Firestore database with the user's search term.<br>
 * Allows for the Search Result to be passed around between files easily
 * */
public class SearchResult {
    private ArrayList<FlashletWithCreator> flashlets;
    private ArrayList<User> users;

    // Getters
    public ArrayList<FlashletWithCreator> getFlashlets() {
        return this.flashlets;
    }
    public ArrayList<User> getUsers() {
        return this.users;
    }

    // Setters
    public void setFlashlets(ArrayList<FlashletWithCreator> flashlets) {
        this.flashlets = flashlets;
    }
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public SearchResult(ArrayList<FlashletWithCreator> flashlets, ArrayList<User> users) {
        this.flashlets = flashlets;
        this.users = users;
    }

    public SearchResult() {
        this.flashlets = new ArrayList<FlashletWithCreator>();
        this.users = new ArrayList<User>();
    }
}

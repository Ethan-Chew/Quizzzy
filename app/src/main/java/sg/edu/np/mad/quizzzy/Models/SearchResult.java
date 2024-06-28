package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

public class SearchResult {
    private ArrayList<Flashlet> flashlets;
    private ArrayList<User> users;

    // Getters
    public ArrayList<Flashlet> getFlashlets() {
        return this.flashlets;
    }
    public ArrayList<User> getUsers() {
        return this.users;
    }

    // Setters
    public void setFlashlets(ArrayList<Flashlet> flashlets) {
        this.flashlets = flashlets;
    }
    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
}

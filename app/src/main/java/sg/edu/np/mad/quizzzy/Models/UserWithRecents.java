package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

public class UserWithRecents {
    private User user;
    private ArrayList<String> recentlyOpenedFlashlets;

    public UserWithRecents(User user) {
        this.user = user;
        this.recentlyOpenedFlashlets = new ArrayList<String>();
    }

    public UserWithRecents(User user, ArrayList<String> recentlyOpenedFlashlets) {
        this.user = user;
        this.recentlyOpenedFlashlets = recentlyOpenedFlashlets;
    }

    // Setters
    public void setUser(User user) {
        this.user = user;
    }

    public void setRecentlyOpenedFlashlets(ArrayList<String> recentlyOpenedFlashlets) {
        this.recentlyOpenedFlashlets = recentlyOpenedFlashlets;
    }

    // Getters
    public User getUser() {
        return this.user;
    }

    public ArrayList<String> getRecentlyOpenedFlashlets() {
        return this.recentlyOpenedFlashlets;
    }
}
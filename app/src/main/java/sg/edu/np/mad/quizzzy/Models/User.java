package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;
import java.util.Queue;

public class User {
    String id; // Unique UUID
    String username;
    String email;
    //ArrayList<String> createdFlashlets;
    //ArrayList<String> recentlyViewedFlashlets; // Note: Max Queue Length (5)
    //ArrayList<String> joinedClasses;

    // Getter
    public String getId() { return this.id; }
    public String getUsername() { return this.username; }
    public String getEmail() {return this.email;}
//    public ArrayList<String> getCreatedFlashlets() { return this.createdFlashlets; }
//    public ArrayList<String> getRecentlyViewedFlashlets() { return this.recentlyViewedFlashlets; }
//    public ArrayList<String> getJoinedClasses() { return this.joinedClasses; }

    // Setter


    // Constructor
    public User (String id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
//        createdFlashlets = new ArrayList<>();
//        recentlyViewedFlashlets = new ArrayList<>();
//        joinedClasses = new ArrayList<>();
    }

    // TODO: @Darius add attributes for Statistics
}

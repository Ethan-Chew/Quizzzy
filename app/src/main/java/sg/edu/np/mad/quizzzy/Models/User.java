package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;
import java.util.Queue;

public class User {
    String id; // Unique UUID
    String username;
    String password;
    String fullName;
    ArrayList<String> createdFlashlets;
    Queue<String> recentlyViewedFlashlets; // Note: Max Queue Length (5)
    ArrayList<String> joinedClasses;

    // Getter
    public String getId() { return this.id; }

    // Setter

    // TODO: @Darius add attributes for Statistics
}

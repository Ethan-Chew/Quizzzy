package sg.edu.np.mad.quizzzy.Models;

import java.util.Queue;

public class User {
    String id; // Unique UUID
    String username;
    String password;
    String fullName;
    String[] createdFlashlets;
    Queue<String> recentlyViewedFlashlets; // Note: Max Queue Length (5)
    String[] joinedClasses;

    // TODO: @Darius add attributes for Statistics
}

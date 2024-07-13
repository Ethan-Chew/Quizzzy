package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;
import java.util.Queue;

public class User {
    private String id; // Unique UUID
    private String username;
    private String email;
    private ArrayList<String> createdFlashlets;

    // Getter
    public String getId() { return this.id; }
    public String getUsername() { return this.username; }
    public String getEmail() {return this.email;}
    public ArrayList<String> getCreatedFlashlets() { return this.createdFlashlets; }


    // Setter
    public void setId(String id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setCreatedFlashlets(ArrayList<String> createdFlashlets) {
        this.createdFlashlets = createdFlashlets;
    }

    // Constructor
    public User(String id, String username, String email, ArrayList<String> createdFlashlets) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.createdFlashlets = createdFlashlets;
    }
}


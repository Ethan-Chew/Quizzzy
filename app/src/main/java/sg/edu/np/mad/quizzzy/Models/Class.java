package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

public class Class {
    String ClassId;
    ArrayList<String> creatorId;
    ArrayList<String> memberId;

    // Setters
    public void addMember(String id) {
        this.memberId.add(id);
    }
    public void removeMember(String id) {
        // Implement this
    }


    // Getters
    public ArrayList<String> getMembers() { return this.memberId; }
    public ArrayList<String> getCreators() { return this.creatorId; }
}

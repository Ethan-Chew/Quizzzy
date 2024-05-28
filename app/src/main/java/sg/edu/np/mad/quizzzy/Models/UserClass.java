package sg.edu.np.mad.quizzzy.Models;

import java.util.ArrayList;

public class UserClass {
    String id;
    String classTitle;
    ArrayList<String> creatorId;
    ArrayList<String> memberId;
    long lastUpdatedUnix;

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setClassTitle(String classTitle) {
        this.classTitle = classTitle;
    }
    public void setCreatorId(ArrayList<String> creatorId) {
        this.creatorId = creatorId;
    }
    public void setMemberId(ArrayList<String> memberId) {
        this.memberId = memberId;
    }
    public void addMember(String userId) {
        this.memberId.add(id);
    }
    public void removeMember(String id) {
        this.memberId.remove(id);
    }

    // Getters
    public String getId() { return this.id; }
    public ArrayList<String> getMemberId() { return this.memberId; }
    public String getClassTitle() { return this.classTitle; }
    public ArrayList<String> getCreatorId() { return this.creatorId; }

    public UserClass(String id, String classTitle, ArrayList<String> creatorId, ArrayList<String> memberId, long lastUpdatedUnix) {
        this.id = id;
        this.classTitle = classTitle;
        this.creatorId = creatorId;
        this.memberId = memberId;
        this.lastUpdatedUnix = lastUpdatedUnix;
    }
}

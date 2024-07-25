package sg.edu.np.mad.quizzzy.Models;

public class FirebaseRTDBHelper {
    String userId;
    String studyDuration;

    // Setters
    public void setUserId(String userId) { this.userId = userId; }

    public void setStudyDuration(String studyDuration) { this.studyDuration = studyDuration; }

    // Getters
    public String getUserId() { return userId; }

    public String getStudyDuration() { return studyDuration; }

    public FirebaseRTDBHelper(String userId, String studyDuration) {
        this.userId = userId;
        this.studyDuration = studyDuration;
    }
}

package sg.edu.np.mad.quizzzy.Models;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class StudyDurationHelper {
    String userId;
    String studyDuration;
    String currentDate;

    // Setters
    public void setUserId(String userId) { this.userId = userId; }

    public void setStudyDuration(String studyDuration) { this.studyDuration = studyDuration; }
    public void setCurrentDate(String currentDate) { this.currentDate = currentDate; }

    // Getters
    public String getUserId() { return userId; }

    public String getStudyDuration() { return studyDuration; }
    public String getCurrentDate() { return currentDate; }

    public StudyDurationHelper(String userId, String studyDuration) {
        this.userId = userId;
        this.studyDuration = studyDuration;

        // Formats
        this.currentDate = SimpleDateFormat.getDateInstance().format(new Date());
    }
}

package sg.edu.np.mad.quizzzy.Models;

public class UsageStatistic {
    long startTime;
    long timeElapsed;
    int flashcardsAccessed;
    boolean activityChanged;

    // Setters
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public void setTimeElapsed(long timeElapsed) { this.timeElapsed = timeElapsed; }
    public void nextFlashcardAccessed() { this.flashcardsAccessed++; }
    public void setActivityChanged(boolean activityChanged) { this.activityChanged = activityChanged; };

    // Getters
    public long getStartTime() { return this.startTime; }
    public long getTimeElapsed() { return this.timeElapsed; }
    public int getFlashcardsAccessed() { return this.flashcardsAccessed; }
    public boolean getActivityChanged() { return activityChanged; };

    // Methods
    // Updates total time spent on activity
    public void updateTimeData() {
        timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        startTime = System.currentTimeMillis();
    }

    // Reset number of flashcards accessed
    public void resetFlashcardsAccessed() {
        flashcardsAccessed = 0;
    }

    // Constructor
    public UsageStatistic() {
        this.startTime = System.currentTimeMillis();
        this.timeElapsed = 0;
        this.activityChanged = false;
    }
}

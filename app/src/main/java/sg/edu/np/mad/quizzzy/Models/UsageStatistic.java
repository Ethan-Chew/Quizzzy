package sg.edu.np.mad.quizzzy.Models;

public class UsageStatistic {
    long startTime;
    long timeElapsed;

    // Setters
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    // Getters
    public long getStartTime() { return this.startTime; }
    public long getTimeElapsed() { return this.timeElapsed; }

    // Methods
    // Stores total time spent on activity and updates SQLite DB
    public void updateTimeData() {
        // Resets timeElapsed before updating
        timeElapsed = 0;
        timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        startTime = System.currentTimeMillis();
    }

    // Constructor
    public UsageStatistic() {
        this.startTime = System.currentTimeMillis();
        this.timeElapsed = 0;
    }
}

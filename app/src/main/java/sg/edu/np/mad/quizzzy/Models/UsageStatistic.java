package sg.edu.np.mad.quizzzy.Models;

public class UsageStatistic {
    public long startTime;
    public long timeElapsed;

    public UsageStatistic() {
        this.startTime = System.currentTimeMillis();
        this.timeElapsed = 0;
    }

    // Stores total time spent on activity and updates SQLite DB
    public void updateTimeData() {
        // Resets timeElapsed before updating
        timeElapsed = 0;
        timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
        startTime = System.currentTimeMillis();
    }
}

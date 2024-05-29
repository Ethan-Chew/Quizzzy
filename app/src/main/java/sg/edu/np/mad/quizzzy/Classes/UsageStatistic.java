package sg.edu.np.mad.quizzzy.Classes;

public class UsageStatistic {
    public long startTime;
    public int flashcardTime;
    public int flashletTime;
    public int classTime;

    public UsageStatistic(int flashcardTime, int flashletTime, int classTime) {
        this.flashcardTime = flashcardTime;
        this.flashletTime = flashletTime;
        this.classTime = classTime;
        this.startTime = System.currentTimeMillis();
    }

    // Stores total time spent on activity and updates SQLite DB
    public void updateTimeData(int timeType) {
        long totalTimeMins = (System.currentTimeMillis() - startTime) / 1000;

        // timeType is the time spent on each section that we are tracking the usage time of
        // flashcards = 0, flashlets = 1, classes = 2
        switch (timeType) {
            case 0:
                flashcardTime += (int)totalTimeMins;
                break;
            case 1:
                flashletTime += (int)totalTimeMins;
                break;
            case 2:
                classTime += (int)totalTimeMins;
                break;
        }
    }
}

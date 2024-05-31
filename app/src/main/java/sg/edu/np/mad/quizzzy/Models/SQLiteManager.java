package sg.edu.np.mad.quizzzy.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.stream.IntStream;

import sg.edu.np.mad.quizzzy.Classes.UsageStatistic;

public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManager;
    private static final String DATABASE_NAME = "QuizzzyDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Users";
    // Attribute Names
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String CREATED_FLASHLETS = "createdflashlets";
    private static final String RECENTLY_VIEWED_FLASHLETS = "recentlyviewedflashlets";
    private static final String STATISTICS_FLASHCARD = "flashcardUsageTime";
    private static final String STATISTICS_FLASHLET = "flashletUsageTime";
    private static final String STATISTICS_CLASSES = "classUsageTime";

    public SQLiteManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public static SQLiteManager instanceOfDatabase(Context context) {
        if(sqLiteManager == null){
            sqLiteManager = new SQLiteManager(context);
        }
        return sqLiteManager;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuery = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " TEXT, "
                + USERNAME + " TEXT, "
                + EMAIL + " TEXT, "
                + CREATED_FLASHLETS + " TEXT, "
                + RECENTLY_VIEWED_FLASHLETS + " TEXT, "
                + STATISTICS_FLASHCARD + " TEXT, "
                + STATISTICS_FLASHLET + " TEXT, "
                + STATISTICS_CLASSES + " TEXT)";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void addUser(UserWithRecents user) {
        SQLiteDatabase db = this.getWritableDatabase();

        User userWithoutRecents = user.getUser();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, userWithoutRecents.getId());
        contentValues.put(USERNAME, userWithoutRecents.getUsername());
        contentValues.put(EMAIL, userWithoutRecents.getEmail());

        // Covert ArrayList to String joined with ;
        contentValues.put(CREATED_FLASHLETS, convertArrayToString(userWithoutRecents.getCreatedFlashlets()));
        contentValues.put(RECENTLY_VIEWED_FLASHLETS, convertArrayToString(user.getRecentlyOpenedFlashlets()));

        // Add usage statistics values
        contentValues.put(STATISTICS_FLASHCARD, "0;0;0;0;0;0;0");
        contentValues.put(STATISTICS_FLASHLET, "0;0;0;0;0;0;0");
        contentValues.put(STATISTICS_CLASSES, "0;0;0;0;0;0;0");

        db.insert(TABLE_NAME, null, contentValues);
    }

    public UserWithRecents getUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        User user = null;
        UserWithRecents userWithRecents = null;
        try (Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if (result.getCount() != 0) {
                while (result.moveToNext()) {
                    String id = result.getString(0);
                    String username = result.getString(1);
                    String email = result.getString(2);

                    ArrayList<String> createdFlashlets = convertStringToArray(result.getString(3));
                    ArrayList<String> recentlyViewedFlashlets = convertStringToArray(result.getString(4));

                    user = new User(id, username, email, createdFlashlets);
                    userWithRecents = new UserWithRecents(user, recentlyViewedFlashlets);
                }
            }
        }
        return userWithRecents;
    }

    public HashMap<String, int[]> getStatistics() {
        // Data will be in this format: Sun;Mon;Tue;Weds;Thu;Fri;Sat
        SQLiteDatabase db = this.getReadableDatabase();

        // Statistics int[] to store data as array instead of String
        int[] flashcardUsage = new int[]{0,0,0,0,0,0,0};
        int[] flashletUsage = new int[]{0,0,0,0,0,0,0};
        int[] classUsage = new int[]{0,0,0,0,0,0,0};

        try (Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if (result.getCount() != 0) {
                while (result.moveToNext()) {
                    // TODO: Check if user is current user
                    if (result.getString(0) == "0") {
                        String flashcardUsageString = result.getString(1);
                        String flashletUsageString = result.getString(2);
                        String classUsageString = result.getString(3);

                        // Split each usage statistic into an array of ints for easier parsing
                        for (int i = 0; i < 7; i++) {
                            flashcardUsage[i] = Integer.parseInt(flashcardUsageString.split(";")[i]);
                            flashletUsage[i] = Integer.parseInt(flashletUsageString.split(";")[i]);
                            classUsage[i] = Integer.parseInt(classUsageString.split(";")[i]);
                        }
                    }
                }
            }
        }

        // Put each int[] of usage statistic into HashMap so we can pass it to other functions
        HashMap<String, int[]> statistics = new HashMap<>();
        statistics.put("flashcardUsage", flashcardUsage);
        statistics.put("flashletUsage", flashletUsage);
        statistics.put("classUsage", classUsage);

        return statistics;
    }

    public void dropUser(String userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, ID + " =? ", new String[]{String.valueOf((userId))});
    }

    public void updateUser(UserWithRecents user) {
        SQLiteDatabase db = this.getWritableDatabase();

        User userWithoutRecents = user.getUser();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, userWithoutRecents.getId());
        contentValues.put(USERNAME, userWithoutRecents.getUsername());
        contentValues.put(EMAIL, userWithoutRecents.getEmail());

        // Covert ArrayList to String joined with ;
        contentValues.put(CREATED_FLASHLETS, convertArrayToString(userWithoutRecents.getCreatedFlashlets()));
        contentValues.put(RECENTLY_VIEWED_FLASHLETS, convertArrayToString(user.getRecentlyOpenedFlashlets()));

        db.update(TABLE_NAME, contentValues, ID + " =? ", new String[]{String.valueOf((userWithoutRecents.getId()))});
    }

    public void updateStatistics(UsageStatistic data, int timeType) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get day as int value using Calander
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Get data converted into int[] from getStatistics()
        HashMap<String, int[]> statistics = new HashMap<>(getStatistics());
        int[] flashcardUsage = statistics.get("flashcardUsage");
        int[] flashletUsage = statistics.get("flashletUsage");
        int[] classUsage = statistics.get("classUsage");

        // Get screen usage data and update DB
        data.updateTimeData(timeType);
        switch (timeType) {
            case 0:
                flashcardUsage[today] += data.flashcardTime;
                break;
            case 1:
                flashletUsage[today] += data.flashletTime;
                break;
            case 2:
                classUsage[today] += data.classTime;
                break;
        }

        // Convert int[] to String[]
        String[] flashcardUsageArrStr = new String[flashcardUsage.length];
        String[] flashletUsageArrStr = new String[flashletUsage.length];
        String[] classUsageArrStr = new String[classUsage.length];

        for (int i = 0; i < 7; i++) {
            flashcardUsageArrStr[i] = String.valueOf(flashcardUsage[i]);
            flashletUsageArrStr[i] = String.valueOf(flashletUsage[i]);
            classUsageArrStr[i] = String.valueOf(classUsage[i]);
        }

        // Update DB after joining each String[] with ;
        ContentValues values = new ContentValues();
        values.put(STATISTICS_FLASHCARD, String.join(";", flashcardUsageArrStr));
        values.put(STATISTICS_FLASHLET, String.join(";", flashletUsageArrStr));
        values.put(STATISTICS_CLASSES, String.join(";", classUsageArrStr));

        // TODO: Help me fix this too to update the correct user
        db.update(TABLE_NAME, values, ID + " =? ", new String[]{String.valueOf((userWithoutRecents.getId()))});
    }

    public void updateCreatedFlashcards(String id, ArrayList<String> createdFlashlets) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(CREATED_FLASHLETS, convertArrayToString(createdFlashlets));

        db.update(TABLE_NAME, contentValues, ID + " =? ", new String[]{id});
    }

    public void updateRecentlyViewed(String id, ArrayList<String> recentlyOpenedFlashcards) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(RECENTLY_VIEWED_FLASHLETS, convertArrayToString(recentlyOpenedFlashcards));

        db.update(TABLE_NAME, contentValues, ID + " =? ", new String[]{id});
    }

    // Calculates all the statistics stored in the DB
    public HashMap<String, Integer> calculateStatistics() {
        HashMap<String, int[]> statistics = new HashMap<>(getStatistics());

        // Get today's day
        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Create HashMap to store data to be passed to Activity
        HashMap<String, Integer> usageStats = new HashMap<String, Integer>();

        // Get daily statistics using calander
        usageStats.put("todayFlashcardUsage", statistics.get("flashcardUsage")[today]);
        usageStats.put("todayFlashletUsage", statistics.get("flashletUsage")[today]);
        usageStats.put("todayClassUsage", statistics.get("classUsage")[today]);

        // Using IntStream so that IntStream.sum() can be used to get weekly total and average
        usageStats.put("totalFlashcardUsage", IntStream.of(statistics.get("flashcardUsage")).sum());
        usageStats.put("averageFlashcardUsage", Math.floorDiv(IntStream.of(statistics.get("flashcardUsage")).sum(), 7));

        usageStats.put("totalFlashletUsage", IntStream.of(statistics.get("flashletUsage")).sum());
        usageStats.put("averageFlashletUsage", Math.floorDiv(IntStream.of(statistics.get("flashletUsage")).sum(), 7));

        usageStats.put("totalClassUsage", IntStream.of(statistics.get("classUsage")).sum());
        usageStats.put("averageClassUsage", Math.floorDiv(IntStream.of(statistics.get("classUsage")).sum(), 7));

        return usageStats;
    }

    // Convert ArrayListToString
    private String convertArrayToString(ArrayList<String> arr) {
        if (arr.isEmpty()) { return ""; }
        StringBuilder stringBuilder = new StringBuilder();

        for (String str : arr) {
            stringBuilder.append(str).append(";");
        }

        // Remove last separator
        stringBuilder.setLength(stringBuilder.length() - 1);

        return stringBuilder.toString();
    }

    private ArrayList<String> convertStringToArray(String str) {
        if (str.isEmpty()) { return new ArrayList<String>(); }
        return new ArrayList<String>(Arrays.asList(str.split(";")));
    }
}



package sg.edu.np.mad.quizzzy.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;

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
    private static final String JOINED_CLASSES = "joinedclases";


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
        String createQuery = "CREATE TABLE " + TABLE_NAME + "(" + ID + " TEXT, " + USERNAME + " TEXT, " + EMAIL + " TEXT, " + CREATED_FLASHLETS + " TEXT, " + RECENTLY_VIEWED_FLASHLETS + " TEXT, " + JOINED_CLASSES + " TEXT)";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, user.getId());
        contentValues.put(USERNAME, user.getUsername());
        contentValues.put(EMAIL, user.getEmail());

        // Covert ArrayList to String joined with ;
        Log.d("AAA", user.getCreatedFlashlets().toString());
        contentValues.put(CREATED_FLASHLETS, convertArrayToString(user.getCreatedFlashlets()));
        contentValues.put(RECENTLY_VIEWED_FLASHLETS, convertArrayToString(user.getRecentlyViewedFlashlets()));
        contentValues.put(JOINED_CLASSES, convertArrayToString(user.getJoinedClasses()));

        db.insert(TABLE_NAME, null, contentValues);
    }

    public User getUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        User user = null;
        try (Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if (result.getCount() != 0) {
                while (result.moveToNext()) {
                    String id = result.getString(0);
                    String username = result.getString(1);
                    String email = result.getString(2);
                    ArrayList<String> createdFlashlets = convertStringToArray(result.getString(3));
                    ArrayList<String> recentlyViewedFlashlets = convertStringToArray(result.getString(4));
                    ArrayList<String> joinedClasses = convertStringToArray(result.getString(5));
                    user = new User(id, username, email, createdFlashlets, recentlyViewedFlashlets, joinedClasses);
                }
            }
        }
        return user;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, user.getId());
        contentValues.put(USERNAME, user.getUsername());
        contentValues.put(EMAIL, user.getEmail());

        // Covert ArrayList to String joined with ;
        contentValues.put(CREATED_FLASHLETS, convertArrayToString(user.getCreatedFlashlets()));
        contentValues.put(RECENTLY_VIEWED_FLASHLETS, convertArrayToString(user.getRecentlyViewedFlashlets()));
        contentValues.put(JOINED_CLASSES, convertArrayToString(user.getJoinedClasses()));

        db.update(TABLE_NAME, contentValues, ID + " =? ", new String[]{String.valueOf((user.getId()))});
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



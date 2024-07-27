package sg.edu.np.mad.quizzzy.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * <b>SQLiteRecentSearchesManager</b> manages the User's Recent Searches in a SQLite Database.
 * It allows for the CRUD (Create, Read, Update and Delete) of Recent Searches. Results returned are sorted by timestamp
 *
 * Main Functionalities:
 * 1. Retrieve Recent Search Queries (sorted by timestamp)
 * 2. Add a new Search Query with it's timestamp
 * 3. Deleting a Specific Search Query
 * 4. Deleting All Search Queries
 * */

public class SQLiteRecentSearchesManager extends SQLiteOpenHelper {
    private static SQLiteRecentSearchesManager sqLiteManager;
    private static final String DATABASE_NAME = "SearchRecents";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "RecentSearches";

    // Attribute Names
    private static final String ID = "id";
    private static final String SEARCH_QUERY = "search";
    private static final String SEARCH_TIMESTAMP = "timestamp";

    public SQLiteRecentSearchesManager(Context context) { super (context, DATABASE_NAME, null, DATABASE_VERSION); }

    public static SQLiteRecentSearchesManager instanceOfDatabase(Context context) {
        if (sqLiteManager == null) {
            sqLiteManager = new SQLiteRecentSearchesManager(context);
        }
        return sqLiteManager;
    }

    // Database Methods
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuery = "CREATE TABLE " + TABLE_NAME + "("
                + ID + " INTEGER PRIMARY KEY, "
                + SEARCH_QUERY + " TEXT UNIQUE, "
                + SEARCH_TIMESTAMP + " BIGINT)";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Getter and Setter for Database
    public ArrayList<String> getSearchQueries() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> searchQueries = new ArrayList<String>();

        String query = "SELECT * FROM " + TABLE_NAME + " ORDER BY " + SEARCH_TIMESTAMP + " DESC";
        try (Cursor result = db.rawQuery(query, null)) {
            if (result.getCount() != 0) {
                while (result.moveToNext()) {
                    searchQueries.add(result.getString(1));
                }
            }
        }

        return searchQueries;
    }

    public void addSearchQueries(String query, Long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SEARCH_QUERY, query);
        contentValues.put(SEARCH_TIMESTAMP, timestamp);

        db.insert(TABLE_NAME, null, contentValues);
    }

    public void updateSearchQueryTimestamp(String query, Long timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SEARCH_TIMESTAMP, timestamp);
        db.update(TABLE_NAME, contentValues, SEARCH_QUERY + " =? ", new String[]{ query });
    }

    public void dropSearchQuery(String query) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, SEARCH_QUERY + " =? ", new String[]{ query });
    }

    public void dropAllSearchQuery() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> searchQueries = getSearchQueries();

        for (String query : searchQueries) {
            dropSearchQuery(query);
        }
    }
}

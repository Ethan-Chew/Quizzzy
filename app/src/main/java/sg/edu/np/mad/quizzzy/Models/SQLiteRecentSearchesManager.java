package sg.edu.np.mad.quizzzy.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class SQLiteRecentSearchesManager extends SQLiteOpenHelper {
    private static SQLiteRecentSearchesManager sqLiteManager;
    private static final String DATABASE_NAME = "SearchRecents";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "RecentSearches";

    // Attribute Names
    private static final String ID = "id";
    private static final String SEARCH_QUERY = "search";

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
                + SEARCH_QUERY + " TEXT UNIQUE) ";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // Getter and Setter for Database
    public ArrayList<String> getSearchQueries() {
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<String> searchQueries = new ArrayList<String>();

        try (Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if (result.getCount() != 0) {
                while (result.moveToNext()) {
                    searchQueries.add(result.getString(1));
                }
            }
        }

        return searchQueries;
    }

    public void addSearchQueries(String query) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SEARCH_QUERY, query);

        db.insert(TABLE_NAME, null, contentValues);
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

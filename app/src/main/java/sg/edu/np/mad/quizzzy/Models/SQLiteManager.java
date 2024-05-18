package sg.edu.np.mad.quizzzy.Models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteManager extends SQLiteOpenHelper {

    private static SQLiteManager sqLiteManager;
    private static final String DATABASE_NAME = "QuizzzyDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Users";
    private static final String ID = "id";
    private static final String USERNAME = "username";
    private static final String EMAIL = "email";


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
        StringBuilder sql;
        sql = new StringBuilder()
                .append("CREATE TABLE ")
                .append(TABLE_NAME)
                .append("(")
                .append(ID)
                .append(" TEXT, ")
                .append(USERNAME)
                .append(" TEXT, ")
                .append(EMAIL)
                .append(" TEXT)");
        db.execSQL(sql.toString());
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

    db.insert(TABLE_NAME, null, contentValues);
    }

    public User populateUser() {
        SQLiteDatabase db = this.getReadableDatabase();

        User user = null;
        try (Cursor result = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)) {
            if (result.getCount() != 0) {
                while (result.moveToNext()) {
                    String id = result.getString(0);
                    String username = result.getString(1);
                    String email = result.getString(2);
                    user = new User(id, username, email);
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

        db.update(TABLE_NAME, contentValues, ID + " =? ", new String[]{String.valueOf((user.getId()))});
    }

}



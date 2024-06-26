package sg.edu.np.mad.quizzzy.Widgets;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Objects;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletList;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.databinding.PersonalFlashletWidgetConfigureBinding;

/**
 * The configuration screen for the {@link PersonalFlashletWidget PersonalFlashletWidget} AppWidget.
 */
public class PersonalFlashletWidgetConfigureActivity extends Activity {
    private static final String PREFS_NAME = "sg.edu.np.mad.quizzzy.PersonalFlashletWidget";
    private static final String PREF_PREFIX_KEY = "personalappwidget_";

    // Data
    ArrayList<String> userFlashletNames = new ArrayList<String>();
    ArrayList<Flashlet> userFlashlets = new ArrayList<Flashlet>();
    Flashlet selectedFlashlet;
    SQLiteManager localDB;
    UserWithRecents userWithRecents;
//    String selectedFlashletId = "";
//    String selectedFlashletName = "";
    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private PersonalFlashletWidgetConfigureBinding binding;
    public PersonalFlashletWidgetConfigureActivity() { super(); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = PersonalFlashletWidgetConfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ListView flashletListView = binding.pFWCFlashletList;

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        // Get User from SQLite DB
        localDB = SQLiteManager.instanceOfDatabase(PersonalFlashletWidgetConfigureActivity.this);
        userWithRecents = localDB.getUser();

        // Get Flashlets from Firebase
        Gson gson = new Gson();
        ArrayList<String> userFlashletIDs = userWithRecents.getUser().getCreatedFlashlets();
        CollectionReference flashletColRef = db.collection("flashlets");
        flashletColRef.whereIn("id", userFlashletIDs).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String flashletJson = gson.toJson(document.getData());
                    Flashlet flashlet = gson.fromJson(flashletJson, Flashlet.class);
                    userFlashletNames.add(flashlet.getTitle());
                    userFlashlets.add(flashlet);
                }
                ArrayAdapter<String> flashletAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.listview_adapter, R.id.widgetListViewItem, userFlashletNames);
                flashletListView.setAdapter(flashletAdapter);

                // Set onClick Listener on ListView
                flashletListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String selectedFlashletName = userFlashletNames.get(position);

                        for (Flashlet flashlet : userFlashlets) {
                            if (Objects.equals(flashlet.getTitle(), selectedFlashletName)) {
                                selectedFlashlet = flashlet;
                                break;
                            }
                        }
                    }
                });

                // Set onClick Listener on Button
                binding.pFWCConfirmSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Save FlashletId to SharedPreferences
                        saveFlashletPref(getApplicationContext(), selectedFlashlet, userWithRecents.getUser().getId(), mAppWidgetId);

                        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
                        PersonalFlashletWidget.updateAppWidget(getApplicationContext(), appWidgetManager, mAppWidgetId);

                        // Make sure we pass back the original appWidgetId
                        Intent resultValue = new Intent();
                        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                        setResult(RESULT_OK, resultValue);
                        finish();
                    }
                });
            }
        });

    }

    static void saveFlashletPref(Context context, Flashlet flashlet, String userId, int appWidgetId) {
        Gson gson = new Gson();

        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "flashletJSON", gson.toJson(flashlet));
        prefs.putString(PREF_PREFIX_KEY + appWidgetId + "userId", userId);
        prefs.apply();
    }

    static void deleteFlashletPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "flashletJSON");
        prefs.remove(PREF_PREFIX_KEY + appWidgetId + "userId");
        prefs.apply();
    }

    static String[] loadFlashletPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String flashletJson = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "flashletJSON", null);
        String userId = prefs.getString(PREF_PREFIX_KEY + appWidgetId + "userId", null);

        if (!(flashletJson == null & userId == null)) {
            return new String[]{ flashletJson, userId };
        }

        return null;
    }
}
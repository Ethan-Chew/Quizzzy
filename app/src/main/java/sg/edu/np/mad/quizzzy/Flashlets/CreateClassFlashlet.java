package sg.edu.np.mad.quizzzy.Flashlets;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.Classes.ClassDetail;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.FlashletWithInsensitive;
import sg.edu.np.mad.quizzzy.Models.GeminiHandlerResponse;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class CreateClassFlashlet extends AppCompatActivity {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    // Data Variables
    Flashlet newFlashlet;
    ArrayList<Flashcard> flashcards = new ArrayList<Flashcard>();
    private String classJson = "";

    // View Variables
    private TextView addNewFlashcardBtn;
    private Button createFlashletBtn;
    private LinearLayout flashcardListView;
    private View newFlashcardView;
    private EditText createFlashletTitle;
    private Switch isFlashletPublicSwitch;

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_flashlet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add Flashlet to SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(CreateClassFlashlet.this);
        User user = localDB.getUser().getUser();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 1, user.getId());

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.cFToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                CreateClassFlashlet.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Handle Back Button Click
        // Enabled is true so that the code within handleOnBackPressed will be executed
        // This also disables the back button press from going to the previous screen
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous screen
                setEnabled(false);
                // Call the default back press behavior again to return to previous screen
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        // Bottom Navigation View
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.search) {
                    startActivity(new Intent(getApplicationContext(), SearchActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(getApplicationContext(), FlashletList.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                }
                return false;
            }
        });

        // Get intent data
        Intent receivingIntent = getIntent();
        String classId = receivingIntent.getStringExtra("classId");
        String userId = receivingIntent.getStringExtra("userId");
        classJson = receivingIntent.getStringExtra("classJson");
        String autofilledFlashlet = receivingIntent.getStringExtra("autofilledFlashletJSON");
        if (autofilledFlashlet != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    autofillGeminiFlashlet(gson.fromJson(autofilledFlashlet, GeminiHandlerResponse.class));
                }
            });
        }

        // Listen for onClick on 'Create Flashlet' button, then create the flashlet
        addNewFlashcardBtn = findViewById(R.id.cFAddNewFlashcardBtn);
        createFlashletBtn = findViewById(R.id.cFCreateNewFlashletButton);
        flashcardListView = findViewById(R.id.cFCreateNewFlashcardList);
        addNewFlashcardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Flashcard newFlashcard = new Flashcard("", "");

                // Add the Flashcard to the Screen
                createFlashcardItem(newFlashcard);

                flashcards.add(newFlashcard);
            }
        });

        // Handle Flashlet creation submission
        createFlashletTitle = findViewById(R.id.cFNewTitle);
        isFlashletPublicSwitch = findViewById(R.id.cFIsPublicSwitch);
        createFlashletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate that all fields are filled in
                String title = createFlashletTitle.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter a title before continuing!", Toast.LENGTH_LONG).show();
                    return;
                }

                /// Ensure that there is at least one Flashcard
                if (flashcards.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "You need to create at least one flashcard!", Toast.LENGTH_LONG).show();
                    return;
                }

                for (Flashcard flashcard : flashcards) {
                    if (flashcard.getDefinition().isEmpty() || flashcard.getKeyword().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Flashcard Keyword or Definition cannot be empty!", Toast.LENGTH_LONG).show();
                        return;
                    }
                }

                // Else, Create the Flashlet
                String id = UUID.randomUUID().toString();
                newFlashlet = new FlashletWithInsensitive(id, title, "", new ArrayList<>(Arrays.asList(userId)), null, flashcards, System.currentTimeMillis() / 1000L, isFlashletPublicSwitch.isChecked(), title.toLowerCase()); // Initialise Flashlet with Empty Description

                if (classId != null) {
                    newFlashlet.setClassId(classId);
                }

                // Disable button to prevent double-adding
                createFlashletBtn.setEnabled(false);
                createFlashletBtn.setText("Loading...");

                // Add Flashlet to Firebase
                db.collection("flashlets")
                        .document(id)
                        .set(newFlashlet)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                CollectionReference docRef = db.collection("class");
                                docRef.whereIn("id", Collections.singletonList(classId)).get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (QueryDocumentSnapshot document : task.getResult()) {
                                                String flashletJson = gson.toJson(document.getData());
                                                JsonObject jsonObject = gson.fromJson( flashletJson, JsonObject.class);
                                                JsonArray jArray = jsonObject.getAsJsonArray("createdFlashlets");
                                                ArrayList<String> createdFlashlets = new ArrayList<String>();
                                                if (jArray != null) {
                                                    for (int i=0;i<jArray.size();i++){
                                                        createdFlashlets.add(String.valueOf(jArray.get(i)).replace("\"", ""));
                                                        }
                                                    }
                                                createdFlashlets.add(newFlashlet.getId());
                                                db.collection("class").document(classId).update("createdFlashlets", createdFlashlets)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void unused) {
                                                                createFlashletBtn.setText("Create Flashlet");
                                                                Toast.makeText(getApplicationContext(), "Flashlet Created!", Toast.LENGTH_LONG).show();
                                                                // Send User back to class Page
                                                                Intent flashletListIntent = new Intent(CreateClassFlashlet.this, ClassDetail.class);
                                                                flashletListIntent.putExtra("classJson", classJson);

                                                                // Save statistics to SQLite DB before changing Activity.
                                                                // timeType of 1 because this is a Flashlet Activity
                                                                localDB.updateStatistics(usage, 1, user.getId());
                                                                // Kills updateStatisticsLoop as we are switching to another activity.
                                                                usage.setActivityChanged(true);

                                                                startActivity(flashletListIntent);
                                                            }
                                                        });
                                            }
                                        } else {
                                            Log.d(TAG, "get failed with ", task.getException());
                                        }
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                createFlashletBtn.setText("Create Flashlet");
                                createFlashletBtn.setEnabled(true);

                                Log.e("Flashlet Creation", e.toString());
                                Toast.makeText(getApplicationContext(), "Failed to Create Flashlet", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void autofillGeminiFlashlet(GeminiHandlerResponse handlerResponse) {
        flashcardListView = findViewById(R.id.cFCreateNewFlashcardList);
        // Set the Title of the Flashlet
        createFlashletTitle = findViewById(R.id.cFNewTitle);
        createFlashletTitle.setText(handlerResponse.getTitle());

        // Create the necessary flashcards and add it to the screen
        flashcards = handlerResponse.getFlashcards();
        for (Flashcard flashcard : flashcards) {
            createFlashcardItem(flashcard);
        }
    }

    private void createFlashcardItem(Flashcard flashcard) {
        View newFlashcardView = LayoutInflater.from(CreateClassFlashlet.this).inflate(R.layout.create_flashlet_newflashcard, null, false);

        // Listen for updates in the Flashcard Info
        EditText keywordEditText = newFlashcardView.findViewById(R.id.newFlashcardKeywordInput);
        keywordEditText.setText(flashcard.getKeyword());
        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                flashcard.setKeyword(keywordEditText.getText().toString());
            }
        });
        EditText definitionEditText = newFlashcardView.findViewById(R.id.newFlashcardDefinitionInput);
        definitionEditText.setText(flashcard.getDefinition());
        definitionEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                flashcard.setDefinition(definitionEditText.getText().toString());
            }
        });

        // Handle Delete Flashcard Item
        ImageView deleteFlashcardItem = newFlashcardView.findViewById(R.id.newFlashcardDelete);
        deleteFlashcardItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashcards.remove(flashcard);
                flashcardListView.removeView(newFlashcardView);
            }
        });

        // Add Inflated View to LinearLayout Container
        flashcardListView.addView(newFlashcardView);
    }

    // To re-initialize the DB update loop when returning to the screen
    @Override
    protected void onRestart() {
        super.onRestart();
        // Recreate the activity so that the DB update loop will be called again and be able to be terminated
        recreate();
    }
}
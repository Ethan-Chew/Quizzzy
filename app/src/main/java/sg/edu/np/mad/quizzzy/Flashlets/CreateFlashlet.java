package sg.edu.np.mad.quizzzy.Flashlets;

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
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.FlashletWithInsensitive;
import sg.edu.np.mad.quizzzy.Models.GeminiHandlerResponse;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.Search.SearchActivity;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class CreateFlashlet extends AppCompatActivity {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    // Data Variables
    FlashletWithInsensitive newFlashlet;
    ArrayList<Flashcard> flashcards = new ArrayList<Flashcard>();

    // View Variables
    private Button addNewFlashcardBtn;
    private Button createFlashletBtn;
    private LinearLayout flashcardListView;
    private EditText createFlashletTitle;
    private Switch isFlashletPublicSwitch;

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
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(CreateFlashlet.this);
        User user = localDB.getUser().getUser();
        String userId = user.getId();

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

                CreateFlashlet.this.getOnBackPressedDispatcher().onBackPressed();
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

        // Get Data from Intents
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

        // Check if Redirect from Class Page
        Intent receivingIntent = getIntent();
        String classId = receivingIntent.getStringExtra("classId");
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
                                ArrayList<String> createdFlashlets = user.getCreatedFlashlets();
                                createdFlashlets.add(newFlashlet.getId());
                                localDB.updateCreatedFlashcards(user.getId(), createdFlashlets);

                                // Save Flashlet ID to User's Firebase
                                db.collection("users").document(user.getId()).update("createdFlashlets", createdFlashlets)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                createFlashletBtn.setText("Create Flashlet"); // Change Flashcard Create button back
                                                Toast.makeText(getApplicationContext(), "Flashlet Created!", Toast.LENGTH_LONG).show();
                                                // Send User back to List Page
                                                Intent flashletListIntent = new Intent(CreateFlashlet.this, FlashletList.class);

                                                // Save statistics to SQLite DB before changing Activity.
                                                // timeType of 1 because this is a Flashlet Activity
                                                localDB.updateStatistics(usage, 1, user.getId());
                                                // Kills updateStatisticsLoop as we are switching to another activity.
                                                usage.setActivityChanged(true);

                                                startActivity(flashletListIntent);
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
        View newFlashcardView = LayoutInflater.from(CreateFlashlet.this).inflate(R.layout.create_flashlet_newflashcard, null, false);

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
package sg.edu.np.mad.quizzzy.Flashlets;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.SwipeGestureDetector;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

public class FlashletDetail extends AppCompatActivity {
    Gson gson = new Gson();

    // Data Variables
    Flashlet flashlet;

    // View Variables
    TextView flashletTitleLbl;
    TextView flashletFlashcardCountLbl;
    Button studyFlashcardBtn;
    LinearLayout flashcardViewList;
    ViewFlipper flashcardPreview;
    GestureDetector gestureDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashlet_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Flashlet from Intent
        Intent receiveIntent = getIntent();
        flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletJSON"), Flashlet.class);
        String userId = receiveIntent.getStringExtra("userId");
        ArrayList<Flashcard> flashcards = flashlet.getFlashcards();

        // Update SQLite with Recently Opened
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletDetail.this);
        ArrayList<String> recentlyViewed = localDB.getUser().getRecentlyOpenedFlashlets();

        // Create new UsageStatistic class and start the update loop
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 1, userId);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.flashlets);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                if (itemId == R.id.home) {
                    startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.create) {
                    Intent createFlashletIntent = new Intent(getApplicationContext(), CreateFlashlet.class);
                    createFlashletIntent.putExtra("userId", "");
                    startActivity(createFlashletIntent);
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.flashlets) {
                    startActivity(new Intent(getApplicationContext(), FlashletList.class));
                    overridePendingTransition(0,0);
                    return true;
                } else if (itemId == R.id.stats) {
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fDViewToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                FlashletDetail.this.getOnBackPressedDispatcher().onBackPressed();
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
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous screen
                setEnabled(false);
                // Call the default back press behavior again to return to previous screen
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        // Handle Edit Button Pressed
        ImageView editFlashletBtn = findViewById(R.id.fDEditOption);
        editFlashletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendToEdit = new Intent(FlashletDetail.this, UpdateFlashlet.class);
                sendToEdit.putExtra("flashletJSON", gson.toJson(flashlet));

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                startActivity(sendToEdit);
            }
        });

        // Find View Components
        flashletTitleLbl = findViewById(R.id.fDFlashletTitle);
        flashletFlashcardCountLbl = findViewById(R.id.fDCounterLabel);
        studyFlashcardBtn = findViewById(R.id.fDStudyFlashcards);
        flashcardViewList = findViewById(R.id.fDFlashcardsContainer);
        flashcardPreview = findViewById(R.id.fDFlashcardPreview);

        // Configure Study Flashcards Button
        Button studyFlashcards = findViewById(R.id.fDStudyFlashcards);
        studyFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendToStudyFlashcards = new Intent(FlashletDetail.this, FlashcardList.class);
                sendToStudyFlashcards.putExtra("flashletJson", gson.toJson(flashlet));

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 1 because this is a Flashlet Activity
                localDB.updateStatistics(usage, 1, userId);
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                startActivity(sendToStudyFlashcards);
            }
        });

        if (recentlyViewed.size() == 5) {
            recentlyViewed.remove(4);
        }
        if (!recentlyViewed.contains(flashlet.getId())) {
            recentlyViewed.add(flashlet.getId());
            localDB.updateRecentlyViewed(userId, recentlyViewed);
        }

        // Update UI based on Flashlet Info
        flashletTitleLbl.setText(flashlet.getTitle());
        String flashcardCount = flashcards.size() + " Total Flashcard" + (flashcards.size() == 1 ? "" : "s");
        flashletFlashcardCountLbl.setText(flashcardCount);

        //Set gesture detector
        gestureDetector = new GestureDetector(this, new SwipeGestureDetector() {
            @Override
            public boolean onSwipeRight() {
                flashcardPreview.setInAnimation(FlashletDetail.this, R.anim.slide_in_left);
                flashcardPreview.setOutAnimation(FlashletDetail.this, R.anim.slide_out_right);
                flashcardPreview.showPrevious();
                return true;
            }

            @Override
            public boolean onSwipeLeft() {
                flashcardPreview.setInAnimation(FlashletDetail.this, R.anim.slide_in_right);
                flashcardPreview.setOutAnimation(FlashletDetail.this, R.anim.slide_out_left);
                flashcardPreview.showNext();
                return true;
            }
        });

        //Set flashcard preview
        for (int i = 0; i < flashcards.size() && i < 8; i++) {
            View flashcardView = LayoutInflater.from(this).inflate(R.layout.flashcard_view_item, flashcardPreview, false);
            TextView keyword = flashcardView.findViewById(R.id.flashcardKeyword);
            TextView definition = flashcardView.findViewById(R.id.flashcardDefinition);
            keyword.setText(flashcards.get(i).getKeyword());
            definition.setText(flashcards.get(i).getDefinition());

            //Add flashcard to ViewFlipper
            flashcardPreview.addView(flashcardView);
        }

        // Add Flashlets to Screen
        for (int i = 0; i < flashcards.size(); i++) {
            // Create Flashcard
            View flashcardView = LayoutInflater.from(FlashletDetail.this).inflate(R.layout.flashcard_list_item, null, false);
            TextView flashcardKeyword = flashcardView.findViewById(R.id.fCLIKeyword);
            TextView flashcardDefinition = flashcardView.findViewById(R.id.fCLIDefinition);

            flashcardKeyword.setText(flashcards.get(i).getKeyword());
            flashcardDefinition.setText(flashcards.get(i).getDefinition());

            /// Add Inflated Flashcard to Containner
            flashcardViewList.addView(flashcardView);

            // Add Spacer View
            View spacerView = new View(FlashletDetail.this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    20
            );
            flashcardViewList.addView(spacerView, spacerParams);
        }

        //On touch listener for gesture
        flashcardPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    // To re-initialize the DB update loop when returning to the screen
    @Override
    protected void onRestart() {
        super.onRestart();
        // Recreate the activity so that the DB update loop will be called again and be able to be terminated
        recreate();
    }
}
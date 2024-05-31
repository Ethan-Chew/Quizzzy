package sg.edu.np.mad.quizzzy.Flashlets;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import androidx.activity.OnBackPressedCallback;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.FlashcardList;
import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.SwipeGestureDetector;
import sg.edu.np.mad.quizzzy.R;

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
    RelativeLayout card_main;
    private CardView flashcard_front, flashcard_back;
    private boolean isFront = true;

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

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.flashlets);
        bottomNavigationView.setOnApplyWindowInsetsListener(null);
        bottomNavigationView.setPadding(0,0,0,0);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();
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
                    // TODO: Integrate Darius's Part
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
                FlashletDetail.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Find View Components
        flashletTitleLbl = findViewById(R.id.fDFlashletTitle);
        flashletFlashcardCountLbl = findViewById(R.id.fDCounterLabel);
        studyFlashcardBtn = findViewById(R.id.fDStudyFlashcards);
        flashcardViewList = findViewById(R.id.fDFlashcardsContainer);
        flashcardPreview = findViewById(R.id.fDFlashcardPreview);

        // Get Flashlet from Intent
        Intent receiveIntent = getIntent();
        flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletJSON"), Flashlet.class);
        String userId = receiveIntent.getStringExtra("userId");
        ArrayList<Flashcard> flashcards = flashlet.getFlashcards();

        // Configure Study Flashcards Button
        Button studyFlashcards = findViewById(R.id.fDStudyFlashcards);
        studyFlashcards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendToStudyFlashcards = new Intent(FlashletDetail.this, FlashcardList.class);
                sendToStudyFlashcards.putExtra("flashletJson", gson.toJson(flashlet));
                startActivity(sendToStudyFlashcards);
            }
        });

        // Update SQLite with Recently Opened
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashletDetail.this);
        ArrayList<String> recentlyViewed = localDB.getUser().getRecentlyOpenedFlashlets();
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

        //Set flashcard preview
        for (int i = 0; i < flashcards.size() && i < 8; i++) {
            View flashcardView = LayoutInflater.from(this).inflate(R.layout.flashcard_view_item, flashcardPreview, false);
            TextView keyword = flashcardView.findViewById(R.id.flashcardKeyword);
            TextView definition = flashcardView.findViewById(R.id.flashcardDefinition);
            keyword.setText(flashcards.get(i).getKeyword());
            definition.setText(flashcards.get(i).getDefinition());

            flashcard_front = flashcardView.findViewById(R.id.flashcard_front);
            flashcard_back = flashcardView.findViewById(R.id.flashcard_back);

            card_main = flashcardView.findViewById(R.id.card_main);

            flashcardPreview.addView(flashcardView);
        }

        card_main.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ flip_card_anim();}
        });


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

        gestureDetector = new GestureDetector(this, new SwipeGestureDetector(flashcardPreview));

        flashcardPreview.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });
    }

    private void flip_card_anim(){
        AnimatorSet setOut = (AnimatorSet) AnimatorInflater.loadAnimator(FlashletDetail.this,R.animator.card_flip_out);
        AnimatorSet setIn = (AnimatorSet) AnimatorInflater.loadAnimator(FlashletDetail.this,R.animator.card_flip_in);

        setOut.setTarget(isFront ? flashcard_front : flashcard_back);
        setIn.setTarget((isFront ? flashcard_back : flashcard_front));

        setOut.start();
        setIn.start();

        isFront = !isFront;
    }

}
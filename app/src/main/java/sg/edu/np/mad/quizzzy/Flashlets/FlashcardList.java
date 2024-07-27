package sg.edu.np.mad.quizzzy.Flashlets;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.R;

public class FlashcardList extends AppCompatActivity {
    Gson gson = new Gson();

    private int arrayIndex;
    private ArrayList<Flashcard> flashcards;
    private CardView flashcard_front, flashcard_back;
    private boolean isFront = true;
    UsageStatistic usage;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashcard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Add Flashlet to SQLite DB
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(FlashcardList.this);
        User user = localDB.getUser().getUser();

        // Create new UsageStatistic class and start the update loop
        usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 0, user.getId());

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fCToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 0 because this is a Flashcard Activity
                localDB.updateStatistics(usage, 0, user.getId());
                localDB.updateFlashcardsAccessed(usage, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                finish();
            }
        });

        // Handle Back Button Click
        // Enabled is true so that the code within handleOnBackPressed will be executed
        // This also disables the back button press from going to the previous screen
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 0 because this is a Flashcard Activity
                localDB.updateStatistics(usage, 0, user.getId());
                localDB.updateFlashcardsAccessed(usage, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous screen
                setEnabled(false);
                // Call the default back press behavior again to return to previous screen
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        RelativeLayout card_main = findViewById(R.id.card_main);

        //Find CardView
        flashcard_front = findViewById(R.id.flashcard_front);
        flashcard_back = findViewById(R.id.flashcard_back);

        //Find Button
        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnEdit = findViewById(R.id.btnEdit);


        //Find TextView
        TextView tvFLName = findViewById(R.id.tvFLName);
        TextView tvKeyword = findViewById(R.id.tvFCKeyword);
        TextView tvDefinition = findViewById(R.id.tvFCDefinition);


        // Get from Intent
        Intent receiveIntent = getIntent();
        Flashlet flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletJson"), Flashlet.class);
        flashcards = flashlet.getFlashcards();

        tvFLName.setText(flashlet.getTitle());
        tvKeyword.setText(flashcards.get(0).getKeyword());
        tvDefinition.setText(flashcards.get(0).getDefinition());

        arrayIndex = 0;

        //Flip the flashcard
        card_main.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                flip_card_anim();
            }
        });

        //Shuffle flashcards in a flashlet
        btnShuffle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Collections.shuffle(flashcards);

                arrayIndex = 0;
                tvKeyword.setText(flashcards.get(arrayIndex).getKeyword());
                tvDefinition.setText(flashcards.get(arrayIndex).getDefinition());
            }
        });


        //Set ActivityResultLauncher for starting an activiyt to edit flashcard
        ActivityResultLauncher<Intent> editFlashcardLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        //Check if result is Ok and not null
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            //Retrieve data from intent
                            Intent data = result.getData();
                            int index = data.getIntExtra("Array_Index", -1);
                            String keyword = data.getStringExtra("Keyword");
                            String definition = data.getStringExtra("Definition");

                            if (index != -1) {
                                flashcards.get(index).setKeyword(keyword);
                                flashcards.get(index).setDefinition(definition);

                                // Update the displayed flashcard if it's the current one
                                if (index == arrayIndex) {
                                    TextView tvKeyword = findViewById(R.id.tvFCKeyword);
                                    TextView tvDefinition = findViewById(R.id.tvFCDefinition);
                                    tvKeyword.setText(keyword);
                                    tvDefinition.setText(definition);
                                }
                            }
                        }
                    }
                });


        //Launch edit flashcard page
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(FlashcardList.this, EditFlashcard.class);
            intent.putExtra("Array_Index", arrayIndex);
            intent.putExtra("flashletJson", gson.toJson(flashlet));
            editFlashcardLauncher.launch(intent);
        });

        //Set gesture detector
        GestureDetector gestureDetector = new GestureDetector(this, new SwipeGestureDetector());

        card_main.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
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

    //Flip card animation
    private void flip_card_anim(){
        AnimatorSet setOut = (AnimatorSet) AnimatorInflater.loadAnimator(FlashcardList.this,R.animator.card_flip_out);
        AnimatorSet setIn = (AnimatorSet) AnimatorInflater.loadAnimator(FlashcardList.this,R.animator.card_flip_in);

        setOut.setTarget(isFront ? flashcard_front : flashcard_back);
        setIn.setTarget((isFront ? flashcard_back : flashcard_front));

        setOut.start();
        setIn.start();

        isFront = !isFront;
    }

    private class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                result = true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    private void onSwipeRight() {
        if (arrayIndex > 0) {
            arrayIndex--;
            usage.nextFlashcardAccessed();
            showFlashcard();
        } else {
            Toast.makeText(this, "No more flashcards to the left", Toast.LENGTH_SHORT).show();
        }
    }

    private void onSwipeLeft() {
        if (arrayIndex < flashcards.size() - 1) {
            arrayIndex++;
            usage.nextFlashcardAccessed();
            showFlashcard();
        } else {
            Toast.makeText(this, "No more flashcards to the right", Toast.LENGTH_SHORT).show();
        }
    }

    private void showFlashcard() {
        TextView tvKeyword = findViewById(R.id.tvFCKeyword);
        TextView tvDefinition = findViewById(R.id.tvFCDefinition);
        tvKeyword.setText(flashcards.get(arrayIndex).getKeyword());
        tvDefinition.setText(flashcards.get(arrayIndex).getDefinition());

        if (!isFront){ flip_card_anim();}

    }

}
package sg.edu.np.mad.quizzzy;

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

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.UsageStatistic;
import sg.edu.np.mad.quizzzy.Models.User;

public class FlashcardList extends AppCompatActivity {
    Gson gson = new Gson();

    private int arrayIndex;
    private ArrayList<Flashcard> flashcards;
    private CardView flashcard_front, flashcard_back;
    private boolean isFront = true;

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
        UsageStatistic usage = new UsageStatistic();
        localDB.updateStatisticsLoop(usage, 0, user.getId());

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.fCToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Save statistics to SQLite DB before changing Activity.
                // timeType of 0 because this is a Flashcard Activity
                localDB.updateStatistics(usage, 0, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                finish();
            }
        });

        // Handle Back Button Click
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Log.d("Backbutton", "handleOnBackPressed: pee");

                // Save statistics to SQLite DB before changing Activity.
                // timeType of 0 because this is a Flashcard Activity
                localDB.updateStatistics(usage, 0, user.getId());
                // Kills updateStatisticsLoop as we are switching to another activity.
                usage.setActivityChanged(true);

                // Enable the back button to be able to be used to go to the previous page
                setEnabled(false);
                // Call the default back press behavior again to return to previous page
                getOnBackPressedDispatcher().onBackPressed();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, backPressedCallback);

        flashcard_front = findViewById(R.id.flashcard_front);
        flashcard_back = findViewById(R.id.flashcard_back);

        Button btnFlipper = findViewById(R.id.btnFlipper);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnEdit = findViewById(R.id.btnEdit);

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

        btnFlipper.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                flip_card_anim();
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Collections.shuffle(flashcards);

                arrayIndex = 0;
                tvKeyword.setText(flashcards.get(arrayIndex).getKeyword());
                tvDefinition.setText(flashcards.get(arrayIndex).getDefinition());
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayIndex++;

                if (!isFront) {flip_card_anim();}

                if (arrayIndex < flashcards.size()) {
                    tvKeyword.setText(flashcards.get(arrayIndex).getKeyword());
                    tvDefinition.setText(flashcards.get(arrayIndex).getDefinition());
                } else {
                    Toast.makeText(FlashcardList.this, "No more flashcards", Toast.LENGTH_SHORT).show();
                    arrayIndex = 0;
                }

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                arrayIndex--;

                if (!isFront) {flip_card_anim();}

                if (arrayIndex >= 0) {
                    tvKeyword.setText(flashcards.get(arrayIndex).getKeyword());
                    tvDefinition.setText(flashcards.get(arrayIndex).getDefinition());
                } else {
                    Toast.makeText(FlashcardList.this, "No more flashcards", Toast.LENGTH_SHORT).show();
                }
            }
        });

        ActivityResultLauncher<Intent> editFlashcardLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
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


        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(FlashcardList.this, EditFlashcard.class);
            intent.putExtra("Array_Index", arrayIndex);
            intent.putExtra("flashletJson", gson.toJson(flashlet));
            editFlashcardLauncher.launch(intent);
        });
    }

    private void flip_card_anim(){
        AnimatorSet setOut = (AnimatorSet) AnimatorInflater.loadAnimator(FlashcardList.this,R.animator.card_flip_out);
        AnimatorSet setIn = (AnimatorSet) AnimatorInflater.loadAnimator(FlashcardList.this,R.animator.card_flip_in);

        setOut.setTarget(isFront ? flashcard_front : flashcard_back);
        setIn.setTarget((isFront ? flashcard_back : flashcard_front));

        setOut.start();
        setIn.start();

        isFront = !isFront;
    }
}
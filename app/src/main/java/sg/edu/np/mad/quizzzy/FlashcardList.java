package sg.edu.np.mad.quizzzy;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.mad.quizzzy.Models.Flashcard;

public class FlashcardList extends AppCompatActivity {

    private int arrayIndex;
    private ArrayList<Flashcard> flashLet;
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


        flashcard_front = findViewById(R.id.flashcard_front);
        flashcard_back = findViewById(R.id.flashcard_back);

        Button btnFlipper = findViewById(R.id.btnFlipper);
        Button btnNext = findViewById(R.id.btnNext);
        Button btnBack = findViewById(R.id.btnBack);
        Button btnShuffle = findViewById(R.id.btnShuffle);
        Button btnEdit = findViewById(R.id.btnEdit);

        TextView tvKeyword = findViewById(R.id.tvFCKeyword);
        TextView tvDefinition = findViewById(R.id.tvFCDefinition);


        Flashcard fc1 = new Flashcard("House", "a place to stay");
        Flashcard fc2 = new Flashcard("Orange", "a fruit");
        Flashcard fc3 = new Flashcard("Cola","a black colored fizzy drink");

        flashLet = new ArrayList<>();
        flashLet.add(fc1);
        flashLet.add(fc2);
        flashLet.add(fc3);

        tvKeyword.setText(flashLet.get(0).getKeyword());
        tvDefinition.setText(flashLet.get(0).getDefinition());

        arrayIndex = 0;

        btnFlipper.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                flip_card_anim();
            }
        });

        btnShuffle.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Collections.shuffle(flashLet);

                arrayIndex = 0;
                tvKeyword.setText(flashLet.get(arrayIndex).getKeyword());
                tvDefinition.setText(flashLet.get(arrayIndex).getDefinition());
            }
        });


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayIndex++;

                if (!isFront) {flip_card_anim();}

                if (arrayIndex < flashLet.size()) {
                    tvKeyword.setText(flashLet.get(arrayIndex).getKeyword());
                    tvDefinition.setText(flashLet.get(arrayIndex).getDefinition());
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
                    tvKeyword.setText(flashLet.get(arrayIndex).getKeyword());
                    tvDefinition.setText(flashLet.get(arrayIndex).getDefinition());
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
                                flashLet.get(index).setKeyword(keyword);
                                flashLet.get(index).setDefinition(definition);

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
            intent.putExtra("Keyword", flashLet.get(arrayIndex).getKeyword());
            intent.putExtra("Definition", flashLet.get(arrayIndex).getDefinition());
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
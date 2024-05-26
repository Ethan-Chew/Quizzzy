package sg.edu.np.mad.quizzzy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import android.widget.Toast;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.Flashcard;

public class FlashcardList extends AppCompatActivity {

    private int arrayIndex;
    private ArrayList<Flashcard> flashLet;

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


        Button btnFlipper = findViewById(R.id.btnFlipper);

        ViewSwitcher fcViewSwitcher = findViewById(R.id.viewSwitcher);

        Animation in = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);

        fcViewSwitcher.setInAnimation(in);
        fcViewSwitcher.setOutAnimation(out);

        btnFlipper.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                fcViewSwitcher.showNext();
            }
        });

        TextView tvKeyword = findViewById(R.id.tvFCKeyword);
        TextView tvDefinition = findViewById(R.id.tvFCDefinition);

        Flashcard fc1 = new Flashcard("House", "A place to stay");
        Flashcard fc2 = new Flashcard("Orange", "a fruit");

        flashLet = new ArrayList<>();
        flashLet.add(fc1);
        flashLet.add(fc2);

        Button btnNext = findViewById(R.id.btnNext);
        Button btnBack = findViewById(R.id.btnBack);
        View vKeyword = findViewById(R.id.tvFCKeyword);

        tvKeyword.setText(flashLet.get(0).getKeyword());
        tvDefinition.setText(flashLet.get(0).getDefinition());

        arrayIndex = 0;


        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                arrayIndex++;

                if (fcViewSwitcher.getNextView() == vKeyword) {
                    fcViewSwitcher.showNext();
                }

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

                if (fcViewSwitcher.getNextView() == vKeyword) {
                    fcViewSwitcher.showPrevious();
                }

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


        Button btnEdit = findViewById(R.id.btnEdit);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(FlashcardList.this, EditFlashcard.class);
            intent.putExtra("Array_Index", arrayIndex);
            intent.putExtra("Keyword", flashLet.get(arrayIndex).getKeyword());
            intent.putExtra("Definition", flashLet.get(arrayIndex).getDefinition());
            editFlashcardLauncher.launch(intent);
        });
    }
}
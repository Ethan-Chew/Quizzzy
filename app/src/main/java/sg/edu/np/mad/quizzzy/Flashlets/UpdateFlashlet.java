package sg.edu.np.mad.quizzzy.Flashlets;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class UpdateFlashlet extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Gson gson = new Gson();

    // Data Variables
    Flashlet flashlet;

    // View Variables
    LinearLayout flashcardListView;
    Button addNewFlashcard;
    Button updateFlashletBtn;
    EditText updateFlashletTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_update_flashlet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Get Flashlet from Intent
        Intent receiveIntent = getIntent();
        flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletJSON"), Flashlet.class);

        // Update UI Fields according to Loaded Flashlet
        updateFlashletTitle = findViewById(R.id.uFTitle);
        updateFlashletTitle.setHint(flashlet.getTitle());

        /// Populate existing list of Flashcards in Flashlet
        flashcardListView = findViewById(R.id.uFFlashcardList);
        List<Flashcard> flashcards = flashlet.getFlashcards();
        for (int i = 0; i < flashcards.size(); i++) {
            View updateFlashcardView = LayoutInflater.from(UpdateFlashlet.this).inflate(R.layout.create_flashlet_newflashcard, null, false);
            EditText keywordInput = updateFlashcardView.findViewById(R.id.newFlashcardKeywordInput);
            EditText definitionInput = updateFlashcardView.findViewById(R.id.newFlashcardDefinitionInput);

            // Set Hint Text of Inputs
            keywordInput.setHint(flashcards.get(i).getKeyword());
            definitionInput.setHint(flashcards.get(i).getDefinition());

            // Set onChange Listener of Input
            implementTextChangedListener(flashcards.get(i), keywordInput, definitionInput);

            // Add Flashcard View to Container
            flashcardListView.addView(updateFlashcardView);

            // Add Empty Padding
            View spacerView = new View(UpdateFlashlet.this);
            LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    20
            );
            flashcardListView.addView(spacerView, spacerParams);
        }

        /// Create new Flashcard View when button clicked
        addNewFlashcard = findViewById(R.id.uFAddNewFlashcardBtn);
        addNewFlashcard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View updateFlashcardView = LayoutInflater.from(UpdateFlashlet.this).inflate(R.layout.create_flashlet_newflashcard, null, false);
                EditText keywordInput = updateFlashcardView.findViewById(R.id.newFlashcardKeywordInput);
                EditText definitionInput = updateFlashcardView.findViewById(R.id.newFlashcardDefinitionInput);
                Flashcard flashcard = new Flashcard("", "");

                // Set onChange Listener of Input
                implementTextChangedListener(flashcard, keywordInput, definitionInput);

                // Add Flashcard View to Container
                flashcardListView.addView(updateFlashcardView);

                // Add Empty Padding
                View spacerView = new View(UpdateFlashlet.this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        20
                );
                flashcardListView.addView(spacerView, spacerParams);
            }
        });

        /// Update Flashcard on Update Button Press
        updateFlashletBtn = findViewById(R.id.uFUpdateFlashletButton);
        updateFlashletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implement update database logic
            }
        });
    }

    void implementTextChangedListener(Flashcard flashcard, EditText keywordInput, EditText definitionInput) {
        keywordInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                flashcard.setKeyword(keywordInput.getText().toString());
            }
        });

        definitionInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                flashcard.setDefinition(definitionInput.getText().toString());
            }
        });
    }
}
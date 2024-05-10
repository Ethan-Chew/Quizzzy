package sg.edu.np.mad.quizzzy.Flashlets;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class CreateFlashlet extends AppCompatActivity {

    // Data Variables
    Flashlet newFlashlet;
    List<Flashcard> flashcards = new ArrayList<Flashcard>();

    // View Variables
    private Button addNewFlashcardBtn;
    private Button createFlashletBtn;
    private LinearLayout flashcardListView;
    private View newFlashcardView;
    private EditText createFlashletTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_flashlet);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Listen for onClick on 'Create Flashlet' button, then create the flashlet
        addNewFlashcardBtn = findViewById(R.id.cFAddNewFlashcardBtn);
        createFlashletBtn = findViewById(R.id.cFCreateNewFlashletButton);
        flashcardListView = findViewById(R.id.cFCreateNewFlashcardList);
        addNewFlashcardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create new Flashcard object, and add to ArrayList
                Flashcard newFlashcard = new Flashcard("", "");

                // Inflate the Item for a new Flashcard
                newFlashcardView = LayoutInflater.from(CreateFlashlet.this).inflate(R.layout.create_flashlet_newflashcard, null, false);

                // Listen for updates in the Flashcard Info
                EditText keywordEditText = newFlashcardView.findViewById(R.id.newFlashcardKeywordInput);
                keywordEditText.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        newFlashcard.setKeyword(keywordEditText.getText().toString());
                    }
                });
                EditText definitionEditText = newFlashcardView.findViewById(R.id.newFlashcardDefinitionInput);
                definitionEditText.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        newFlashcard.setDefinition(definitionEditText.getText().toString());
                    }
                });

                // Add Flashcard to List
                flashcards.add(newFlashcard);

                // Add Inflated View to LinearLayout Container
                flashcardListView.addView(newFlashcardView);

                // Add Spacer View
                View spacerView = new View(CreateFlashlet.this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        20
                );
                flashcardListView.addView(spacerView, spacerParams);
            }
        });

        // Handle Flashlet creation submission
        createFlashletTitle = findViewById(R.id.cFNewTitle);
        createFlashletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Validate that all fields are filled in
                String title = createFlashletTitle.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Enter a title before continuing!", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
    }
}
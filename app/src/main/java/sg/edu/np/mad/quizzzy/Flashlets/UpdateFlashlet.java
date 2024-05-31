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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.List;

import sg.edu.np.mad.quizzzy.HomeActivity;
import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;
import sg.edu.np.mad.quizzzy.StatisticsActivity;

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
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
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
                    startActivity(new Intent(getApplicationContext(), StatisticsActivity.class));
                    return true;
                }
                return false;
            }
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.uFToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateFlashlet.this.getOnBackPressedDispatcher().onBackPressed();
            }
        });

        // Get Flashlet from Intent
        Intent receiveIntent = getIntent();
        flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletJSON"), Flashlet.class);

        // Update UI Fields according to Loaded Flashlet
        updateFlashletTitle = findViewById(R.id.uFTitle);
        updateFlashletTitle.setText(flashlet.getTitle());

        /// Populate existing list of Flashcards in Flashlet
        flashcardListView = findViewById(R.id.uFFlashcardList);
        List<Flashcard> flashcards = flashlet.getFlashcards();
        for (int i = 0; i < flashcards.size(); i++) {
            View updateFlashcardView = LayoutInflater.from(UpdateFlashlet.this).inflate(R.layout.create_flashlet_newflashcard, null, false);
            EditText keywordInput = updateFlashcardView.findViewById(R.id.newFlashcardKeywordInput);
            EditText definitionInput = updateFlashcardView.findViewById(R.id.newFlashcardDefinitionInput);

            // Set Text of Inputs
            keywordInput.setText(flashcards.get(i).getKeyword());
            definitionInput.setText(flashcards.get(i).getDefinition());

            // Set onChange Listener of Input
            setTextEditWatcher(flashcards.get(i), keywordInput, definitionInput);

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
                setTextEditWatcher(flashcard, keywordInput, definitionInput);

                // Add Flashcard View to Container
                flashcardListView.addView(updateFlashcardView);

                // Add Empty Padding
                View spacerView = new View(UpdateFlashlet.this);
                LinearLayout.LayoutParams spacerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        20
                );
                flashcardListView.addView(spacerView, spacerParams);

                // Add Flashcard to Flashlet
                flashlet.addFlashcard(flashcard);
            }
        });

        /// Update Flashcard on Update Button Press
        updateFlashletBtn = findViewById(R.id.uFUpdateFlashletButton);
        updateFlashletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /// Disable Button
                updateFlashletBtn.setEnabled(false);
                updateFlashletBtn.setText("Loading...");
                flashlet.setLastUpdatedUnix(System.currentTimeMillis() / 1000L);
                db.collection("flashlets").document(flashlet.getId())
                        .set(flashlet)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                updateFlashletBtn.setText("Update Flashlet");
                                Toast.makeText(UpdateFlashlet.this, "Updated Successfully!", Toast.LENGTH_LONG).show();

                                startActivity(new Intent(UpdateFlashlet.this, FlashletList.class));
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                updateFlashletBtn.setEnabled(true);
                                updateFlashletBtn.setText("Update Flashlet");
                                Log.e("Update Flashlet", e.toString());
                                Toast.makeText(getApplicationContext(), "Failed to Update Flashlet", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });
    }

    private void setTextEditWatcher(Flashcard flashcard, EditText keywordEditText, EditText definitionEditText) {
        keywordEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                flashcard.setKeyword(keywordEditText.getText().toString());
            }
        });
        definitionEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                flashcard.setDefinition(definitionEditText.getText().toString());
            }
        });
    }
}
package sg.edu.np.mad.quizzzy.Flashlets;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.util.ArrayList;

import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class EditFlashcard extends AppCompatActivity {
    // Initialisation of Firebase Cloud Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private int arrayIndex;
    Gson gson = new Gson();
    Flashlet flashlet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_flashcard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Handle Back Navigation Toolbar
        Toolbar toolbar = findViewById(R.id.eFToolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Find Button
        Button btnUndo = findViewById(R.id.btnUndo);
        Button btnSave = findViewById(R.id.btnSave);

        //Find View
        TextView tvFLName = findViewById(R.id.tvFLName);
        EditText etKeyword = findViewById(R.id.etKeyword);
        EditText etDefinition = findViewById(R.id.etDefinition);

        // Retrieve the index and flashcard data from the Intent
        arrayIndex = getIntent().getIntExtra("Array_Index", -1);
        flashlet = gson.fromJson(getIntent().getStringExtra("flashletJson"), Flashlet.class);
        ArrayList<Flashcard> flashcardList = flashlet.getFlashcards();
        tvFLName.setText(flashlet.getTitle());

        // Set the EditTexts with the flashcard data
        if (arrayIndex != -1) {
            etKeyword.setText(flashcardList.get(arrayIndex).getKeyword());
            etDefinition.setText(flashcardList.get(arrayIndex).getDefinition());
        }

        //Save changes
        btnSave.setOnClickListener(v -> {
            Toast.makeText(EditFlashcard.this, "Updated!", Toast.LENGTH_LONG).show();
            flashcardList.get(arrayIndex).setKeyword(etKeyword.getText().toString());
            flashcardList.get(arrayIndex).setDefinition(etDefinition.getText().toString());

            flashlet.setFlashcards(flashcardList);

            db.collection("flashlets").document(flashlet.getId()).update("flashcards", flashcardList)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Intent backToFlashletDetails = new Intent(EditFlashcard.this, FlashletDetail.class);
                            backToFlashletDetails.putExtra("userId", "");
                            backToFlashletDetails.putExtra("flashletJSON", gson.toJson(flashlet));
                            backToFlashletDetails.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            backToFlashletDetails.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(backToFlashletDetails);
                        }
                    });

        });

        //Undo changes
        btnUndo.setOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
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
package sg.edu.np.mad.quizzzy.Flashlets;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.quizzzy.Models.Flashcard;
import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class UpdateFlashlet extends AppCompatActivity {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Data Variables
    Flashlet flashlet;

    // View Variables
    View newFlashcardView;
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

        // Get Flashlet from Firebase
        DocumentReference docRef = db.collection("flashlet").document("");

        // Update UI Fields according to Loaded Flashlet
        updateFlashletTitle = findViewById(R.id.uFTitle);
        updateFlashletTitle.setHint(flashlet.getTitle());

        /// Populate existing list of Flashcards in Flashlet
        List<Flashcard> flashcards = flashlet.getFlashcards();
        for (int i = 0; i < flashcards.size(); i++) {

        }
    }
}
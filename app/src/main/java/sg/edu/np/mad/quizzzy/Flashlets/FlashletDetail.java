package sg.edu.np.mad.quizzzy.Flashlets;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.quizzzy.Models.Flashlet;
import sg.edu.np.mad.quizzzy.R;

public class FlashletDetail extends AppCompatActivity {
    // Data Variables
    Flashlet flashlet;

    // View Variables
    TextView flashletTitleLbl;
    TextView flashletFlashcardCountLbl;
    Button studyFlashcardBtn;
    LinearLayout flashcardViewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flashlet_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Find View Components
        flashletTitleLbl = findViewById(R.id.fDFlashletTitle);
        flashletFlashcardCountLbl = findViewById(R.id.fDCounterLabel);
        studyFlashcardBtn = findViewById(R.id.fDStudyFlashcards);
        flashcardViewList = findViewById(R.id.fDFlashcardsContainer);

        // Get Flashlet from Intent
        
        // Update UI based on Flashlet Info
    }
}
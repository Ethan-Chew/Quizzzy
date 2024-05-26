package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.widget.Button;
import android.widget.EditText;

public class EditFlashcard extends AppCompatActivity {

    private int arrayIndex;

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

        Button btnUndo = findViewById(R.id.btnUndo);
        Button btnSave = findViewById(R.id.btnSave);

        EditText etKeyword = findViewById(R.id.etKeyword);
        EditText etDefinition = findViewById(R.id.etDefinition);

        // Retrieve the index and flashcard data from the Intent
        arrayIndex = getIntent().getIntExtra("Array_Index", -1);
        String keyword = getIntent().getStringExtra("Keyword");
        String definition = getIntent().getStringExtra("Definition");

        // Set the EditTexts with the flashcard data
        if (arrayIndex != -1) {
            etKeyword.setText(keyword);
            etDefinition.setText(definition);
        }

        btnSave.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("Array_Index", arrayIndex);
            resultIntent.putExtra("Keyword", etKeyword.getText().toString());
            resultIntent.putExtra("Definition", etDefinition.getText().toString());
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnUndo.setOnClickListener(v -> {
            // If you have specific undo logic, add it here
            // For now, we'll just finish the activity without saving changes
            setResult(RESULT_CANCELED);
            finish();
        });

    }
}
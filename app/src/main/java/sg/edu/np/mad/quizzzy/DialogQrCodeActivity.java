package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;

import sg.edu.np.mad.quizzzy.Models.Flashlet;


public class DialogQrCodeActivity extends AppCompatActivity {
    Gson gson = new Gson();

    // Data Variables
    Flashlet flashlet;
    TextView dqcInstructionText;
    ImageView dialogQrCodeImageView;
    TextView flashletNameTextView;
    ImageView dialogCloseButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.dialog_qr_code);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        dqcInstructionText = findViewById(R.id.dqcInstructionText);
        dialogQrCodeImageView = findViewById(R.id.dialogQrCodeImageView);
        flashletNameTextView = findViewById(R.id.flashletNameTextView);
        dialogCloseButton = findViewById(R.id.dialogCloseButton);

        Intent receiveIntent = getIntent();
        flashlet = gson.fromJson(receiveIntent.getStringExtra("flashletTitle"), Flashlet.class);

        flashletNameTextView.setText(flashlet.getTitle());
    }
}
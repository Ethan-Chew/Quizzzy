package sg.edu.np.mad.quizzzy;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class HomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView idView = findViewById(R.id.idView);
        TextView usernameView = findViewById(R.id.usernameView);
        TextView emailView = findViewById(R.id.emailView);

        String id = getIntent().getStringExtra("id");
        String email = getIntent().getStringExtra("email");
        String username = getIntent().getStringExtra("username");

        idView.setText(id);
        usernameView.setText(email);
        emailView.setText(username);
    }
}
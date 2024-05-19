package sg.edu.np.mad.quizzzy;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class HomeActivity extends AppCompatActivity {

    TextView idView;
    TextView usernameView;
    TextView emailView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
             v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(HomeActivity.this);

        // Get Screen Elements
        idView = findViewById(R.id.homeIdView);
        usernameView = findViewById(R.id.homeUsernameView);
        emailView = findViewById(R.id.homeEmailView);

        // Get User from Database
        UserWithRecents userWithRecents = localDB.getUser();
        User user = userWithRecents.getUser();

        // Set User Info
        idView.setText(user.getId());
        usernameView.setText(user.getUsername());
        emailView.setText(user.getEmail());
    }
}
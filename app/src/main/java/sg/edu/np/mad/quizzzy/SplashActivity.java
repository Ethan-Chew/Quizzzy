package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if the User is Signed in to Quizzzy
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(this);

        User user = localDB.getUser();
        if (user != null) {
            if (!user.getEmail().isEmpty() && !user.getUsername().isEmpty()) {
                // User is Logged in, send to Home Screen
                Intent homeScreenIntent = new Intent(SplashActivity.this, HomeActivity.class);
                startActivity(homeScreenIntent);
            } else {
                // Else, redirect to Landing Page
                Intent landingPageIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(landingPageIntent);
            }
        } else {
            // Else, redirect to Landing Page
            Intent landingPageIntent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(landingPageIntent);
        }
    }
}
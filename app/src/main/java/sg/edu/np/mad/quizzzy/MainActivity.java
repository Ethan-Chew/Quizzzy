package sg.edu.np.mad.quizzzy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        View loginBtn = findViewById(R.id.loginBtn);
        View signupBtn = findViewById(R.id.signupBtn);

        SharedPreferences sharedPref = MainActivity.this.getSharedPreferences("userLogin", Context.MODE_PRIVATE);
        String email = sharedPref.getString(getString(R.string.email), null);
        String username = sharedPref.getString(getString(R.string.username), null);

        if (email != null && username != null) {
            // User is Logged in, send to Home Screen
            Intent homeScreenIntent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(homeScreenIntent);
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = (new Intent(v.getContext(), LoginActivity.class));
                startActivity(intent);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = (new Intent(v.getContext(), SignupActivity.class));
                startActivity(intent);
            }
        });
    }
}
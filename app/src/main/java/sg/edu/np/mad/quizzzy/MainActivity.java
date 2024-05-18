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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;


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

        Log.println(Log.DEBUG, "DEBUG", loadDB().getId() + loadDB().getUsername() + loadDB().getEmail());
        View loginBtn = findViewById(R.id.loginBtn);
        View signupBtn = findViewById(R.id.signupBtn);


        if (!loadDB().getEmail().isEmpty() && !loadDB().getUsername().isEmpty()) {
            // User is Logged in, send to Home Screen
            Intent homeScreenIntent = new Intent(MainActivity.this, HomeActivity.class);
            homeScreenIntent.putExtra("id", loadDB().getId());
            homeScreenIntent.putExtra("email", loadDB().getEmail());
            homeScreenIntent.putExtra("username", loadDB().getUsername());
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

    private User loadDB() {
        SQLiteManager db = SQLiteManager.instanceOfDatabase(this);
        return db.populateUser();
    }

}


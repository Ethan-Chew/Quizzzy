package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class MainActivity extends AppCompatActivity {

    private String flashletId = null; // Stores flashlet ID obtained from intent data

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);


        View loginBtn = findViewById(R.id.loginBtn);
        View signupBtn = findViewById(R.id.signupBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                // If flashlet ID is available, pass it to LoginActivity
                if (flashletId != null) {
                    intent.putExtra("FLASHLET_ID", flashletId);
                }
                startActivity(intent);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignupActivity.class);
                // If flashlet ID is available, pass it to SignupActivity
                if (flashletId != null) {
                    intent.putExtra("FLASHLET_ID", flashletId);
                }
                startActivity(intent);
            }
        });

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    // Handle when the user scans the QR Code externally and direct back to MainActivity
    private void handleIntent(Intent intent) {
        if (intent.getBooleanExtra("LOGOUT", false)) {
            flashletId = null;
        }

        Uri data = intent.getData();
        if (data != null && data.isHierarchical()) {
            flashletId = data.getQueryParameter("id"); // Extract flashlet ID from query parameter

            if (flashletId != null) {
                // If user is authenticated, handle flashlet addition
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    handleFlashletAddition(flashletId);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == 1 || requestCode == 2) && resultCode == RESULT_OK) {
            // Retrieve the flashletId from the returned Intent data
            String flashletId = data.getStringExtra("FLASHLET_ID");
            if (flashletId != null) {
                handleFlashletAddition(flashletId);
            }
        }
    }

    private void handleFlashletAddition(String flashletId) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Log.e("MainActivity", "User is not authenticated.");
            return;
        }
        String userId = currentUser.getUid();
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(MainActivity.this);


        DocumentReference userRef = firebase.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Check if the flashlet ID already exists in the user's created flashlets
                    List<String> createdFlashlets = (List<String>) document.get("createdFlashlets");
                    // Check if the flashlet ID already exists in the user's created flashlets
                    if (createdFlashlets != null && createdFlashlets.contains(flashletId)) {
                        navigateToHome(flashletId);
                        Toast.makeText(MainActivity.this, "You already have this flashlet.", Toast.LENGTH_SHORT).show();
                    } else {
                        updateUserAndFlashlet(userRef, flashletId);
                        // Update the local SQLite database with the new flashlet ID
                        ArrayList<String> createdFlashletIds = localDB.getUser().getUser().getCreatedFlashlets();
                        createdFlashletIds.add(flashletId);
                        localDB.updateCreatedFlashcards(userId, createdFlashletIds);
                        Toast.makeText(MainActivity.this, "Flashlet added successfully.", Toast.LENGTH_SHORT).show();
                    }
                    MainActivity.this.flashletId = null;
                }
            } else {
                Log.e("MainActivity", "Error checking user document", task.getException());
            }
        });
    }


    private void updateUserAndFlashlet(DocumentReference userRef, String flashletId) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        DocumentReference flashletRef = firebase.collection("flashlets").document(flashletId);

        // Update the user's document with the new flashlet ID
        userRef.update("createdFlashlets", FieldValue.arrayUnion(flashletId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(MainActivity.this, "Flashlet added successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error updating user", e));

        // Update the flashlet's document with the user ID
        flashletRef.update("creatorID", FieldValue.arrayUnion(userRef.getId()))
                .addOnSuccessListener(aVoid -> navigateToHome(flashletId))
                .addOnFailureListener(e -> Log.e("MainActivity", "Error updating flashlet", e));
    }


    private void navigateToHome(String flashletId) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        // Pass the flashlet ID to the HomeActivity
        intent.putExtra("FLASHLET_ID", flashletId);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Check if the User is Signed in to Quizzzy
        SQLiteManager localDB = SQLiteManager.instanceOfDatabase(this);

        UserWithRecents userWithRecents = localDB.getUser();
        if (userWithRecents != null) {
            User user = userWithRecents.getUser();
            if (!user.getEmail().isEmpty() && !user.getUsername().isEmpty()) {
                // User is Logged in, send to Home Screen
                Intent homeScreenIntent = new Intent(MainActivity.this, HomeActivity.class);
                startActivity(homeScreenIntent);
            }
        }
    }
}


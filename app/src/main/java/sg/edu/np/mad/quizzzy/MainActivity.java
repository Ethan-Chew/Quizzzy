package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;


public class MainActivity extends AppCompatActivity {

    private String flashletId = null;

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

        View loginBtn = findViewById(R.id.loginBtn);
        View signupBtn = findViewById(R.id.signupBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), LoginActivity.class);
                if (flashletId != null) {
                    intent.putExtra("FLASHLET_ID", flashletId);
                }
                startActivityForResult(intent, 1);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignupActivity.class);
                if (flashletId != null) {
                    intent.putExtra("FLASHLET_ID", flashletId);
                }
                startActivityForResult(intent, 2);
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

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.isHierarchical()) {
            flashletId = data.getQueryParameter("id");

            if (flashletId != null) {
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

        DocumentReference userRef = firebase.collection("users").document(userId);
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<String> createdFlashlets = (List<String>) document.get("createdFlashlets");
                    if (createdFlashlets != null && createdFlashlets.contains(flashletId)) {
                        Log.d("MainActivity", "Flashlet already exists in user createdFlashlets.");
                        Toast.makeText(MainActivity.this, "You already have this flashlet.", Toast.LENGTH_SHORT).show();
                        navigateToHome(flashletId);
                    } else {
                        updateUserAndFlashlet(userRef, flashletId);
                    }
                }
            } else {
                Log.e("MainActivity", "Error checking user document", task.getException());
            }
        });
    }

    private void updateUserAndFlashlet(DocumentReference userRef, String flashletId) {
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();
        DocumentReference flashletRef = firebase.collection("flashlets").document(flashletId);

        userRef.update("createdFlashlets", FieldValue.arrayUnion(flashletId))
                .addOnSuccessListener(aVoid -> {
                    Log.d("MainActivity", "Flashlet added to user successfully.");
                    Toast.makeText(MainActivity.this, "Flashlet added successfully.", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Log.e("MainActivity", "Error updating user", e));

        flashletRef.update("creatorID", FieldValue.arrayUnion(userRef.getId()))
                .addOnSuccessListener(aVoid -> navigateToHome(flashletId))
                .addOnFailureListener(e -> Log.e("MainActivity", "Error updating flashlet", e));
    }


    private void navigateToHome(String flashletId) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("FLASHLET_ID", flashletId);
        startActivity(intent);
    }


}


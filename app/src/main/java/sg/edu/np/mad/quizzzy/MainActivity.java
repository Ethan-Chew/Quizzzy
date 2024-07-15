package sg.edu.np.mad.quizzzy;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;


public class MainActivity extends AppCompatActivity {

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
                startActivity(intent);
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SignupActivity.class);
                startActivity(intent);
            }
        });

        // Handle intent if the app is opened via a deep link
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the intent
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null && data.isHierarchical()) {
            String flashletId = data.getQueryParameter("id");

            if (flashletId != null) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    // User is already logged in, handle the flashlet addition directly
                    handleFlashletAddition(flashletId);
                } else {
                    // User is not logged in, redirect to LoginActivity
                    Intent loginIntent = new Intent(this, LoginActivity.class);
                    loginIntent.putExtra("FLASHLET_ID", flashletId);
                    startActivityForResult(loginIntent, 1); // Use startActivityForResult to get result from LoginActivity
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            // Get the FLASHLET_ID from the returned data
            String flashletId = data.getStringExtra("FLASHLET_ID");
            if (flashletId != null) {
                handleFlashletAddition(flashletId);
            }
        }
    }

    private void handleFlashletAddition(String flashletId) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore firebase = FirebaseFirestore.getInstance();

        DocumentReference flashletRef = firebase.collection("flashlets").document(flashletId);
        DocumentReference userRef = firebase.collection("users").document(userId);

        userRef.update("createdFlashlets", FieldValue.arrayUnion(flashletId))
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});

        flashletRef.update("creatorID", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class); // Navigate to HomeActivity
                    intent.putExtra("FLASHLET_ID", flashletId);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {});
    }
}


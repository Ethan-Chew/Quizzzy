package sg.edu.np.mad.quizzzy;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import sg.edu.np.mad.quizzzy.Flashlets.FlashletDetail;
import sg.edu.np.mad.quizzzy.Models.SQLiteManager;
import sg.edu.np.mad.quizzzy.Models.User;
import sg.edu.np.mad.quizzzy.Models.UserWithRecents;

public class LoginActivity extends AppCompatActivity {

    Gson gson = new Gson();
    FirebaseAuth mAuth;
    FirebaseFirestore firebase;
    String flashletId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splashTitle), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Configure Firebase Authenticator
        mAuth = FirebaseAuth.getInstance();
        firebase = FirebaseFirestore.getInstance();

        // Get the FLASHLET_ID from the intent if it exists
        flashletId = getIntent().getStringExtra("FLASHLET_ID");

        // Get Respective Text Items on screen
        View loginBtn = findViewById(R.id.loginBtnLoginAct);
        EditText usernameView = findViewById(R.id.usernameField);
        EditText passwordView = findViewById(R.id.passwordField);

        // Detect when the user has pressed the Login Button
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = usernameView.getText().toString();
                String password = passwordView.getText().toString();

                // Ensures that the user's email and password field is not empty
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Username or Password is blank.", Toast.LENGTH_SHORT).show();
                } else {
                    // Send an Authentication request to Firebase Authentication
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser currentUser = mAuth.getCurrentUser();
                                        DocumentReference docRef = firebase.collection("users").document(currentUser.getUid());
                                        // Get the User's Data from Firebase under the 'Users' collection
                                        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    // Save the User to our SQLite (Local) Database
                                                    DocumentSnapshot document = task.getResult();
                                                    SQLiteManager localDB = SQLiteManager.instanceOfDatabase(LoginActivity.this);
                                                    String userJson = gson.toJson(document.getData());
                                                    User user = gson.fromJson(userJson, User.class);
                                                    localDB.addUser(new UserWithRecents(user));

                                                    // Check if there's a FLASHLET_ID in the intent
                                                    String flashletId = getIntent().getStringExtra("FLASHLET_ID");
                                                    if (flashletId != null) {
                                                        handleFlashletAddition(flashletId, currentUser.getUid());
                                                    } else {
                                                        // Normal login, send user to Home Screen
                                                        Intent homeScreenIntent = new Intent(LoginActivity.this, HomeActivity.class);
                                                        startActivity(homeScreenIntent);
                                                        finish();
                                                    }
                                                } else {
                                                    Log.d(TAG, "Get user failed with ", task.getException());
                                                }
                                            }
                                        });
                                    } else {
                                        Log.d(TAG, "Login failed.", task.getException());
                                        Toast.makeText(LoginActivity.this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                }
            }
        });
    }

    private void handleFlashletAddition(String flashletId, String userId) {
        DocumentReference flashletRef = firebase.collection("flashlets").document(flashletId);
        DocumentReference userRef = firebase.collection("users").document(userId);

        userRef.update("createdFlashlets", FieldValue.arrayUnion(flashletId))
                .addOnSuccessListener(aVoid -> {})
                .addOnFailureListener(e -> {});

        flashletRef.update("creatorID", FieldValue.arrayUnion(userId))
                .addOnSuccessListener(aVoid -> {
                    // Return to MainActivity with the FLASHLET_ID
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("FLASHLET_ID", flashletId);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                })
                .addOnFailureListener(e -> {});
    }
}

